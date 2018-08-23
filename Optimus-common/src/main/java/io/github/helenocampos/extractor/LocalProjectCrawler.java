package io.github.helenocampos.extractor;

import io.github.helenocampos.extractor.model.JavaSourceCodeClass;
import io.github.helenocampos.extractor.model.JavaTestClass;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import io.github.helenocampos.extractor.model.ProjectData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author helenocampos
 */
public class LocalProjectCrawler {

    private HashMap<String, JavaSourceCodeClass> javaFiles;
    private HashMap<String, JavaTestClass> testFiles;
    private HashMap<String, String> classFilesPaths;
    private String projectPath;
    private ProjectData projectData;

    public LocalProjectCrawler(String projectPath) {
        javaFiles = new HashMap<String, JavaSourceCodeClass>();
        testFiles = new HashMap<String, JavaTestClass>();
        classFilesPaths = new HashMap<String, String>();
        this.projectPath = projectPath;
        crawl(new File(projectPath));
        resolveClassFilesLocation();
        projectData = ProjectData.getProjectDataFromFile();
        if (projectData != null) {
            projectData.updateClasses(javaFiles);
            projectData.updateTests(testFiles);
        } else {
            projectData = new ProjectData(projectPath);
            projectData.setClasses(javaFiles);
            projectData.setTests(testFiles);
        }
    }

    private void resolveClassFilesLocation() {
        for (JavaSourceCodeClass file : javaFiles.values()) {
            String path = classFilesPaths.get(file.getQualifiedName());
            if (path != null) {
                file.setClassFilePath(path);
            }
        }

        for (JavaTestClass file : testFiles.values()) {
            String path = classFilesPaths.get(file.getQualifiedName());
            if (path != null) {
                file.setClassFilePath(path);
            }
        }

    }

    private void crawl(File f) {
        Stack<File> fileStack = new Stack<>();
        fileStack.push(f);
        while (!fileStack.isEmpty()) {
            File child = fileStack.pop();
            if (child.isDirectory()) {
                for (File subFile : child.listFiles()) {
                    fileStack.push(subFile);
                }
            } else {
                processFile(child);
            }
        }
    }

    private void processFile(File f) {
        String fileExtension = FilenameUtils.getExtension(f.getName());
        if (fileExtension.equals("java") && f.getPath().contains("src")) {
            String packageNameSource = getPackageFromSourceFile(f.getPath());
            if (isTestClass(f.getAbsolutePath())) {
                JavaTestClass cls = new JavaTestClass(f.getName(), f.getPath(), packageNameSource);
                testFiles.put(cls.getQualifiedName(), cls);
            } else {
                if (fileExtension.equals("java")) {
                    JavaSourceCodeClass cls = new JavaSourceCodeClass(f.getName(), f.getPath(), packageNameSource);
                    javaFiles.put(cls.getQualifiedName(), cls);
                }
            }
        } else if (fileExtension.equals("class") && f.getPath().contains("target")) {
            try {
                ClassParser parser = new ClassParser(f.getPath());
                JavaClass cls = parser.parse();
                String qualifiedName = f.getName().replace(".class", "");
                qualifiedName = cls.getPackageName() + "." + qualifiedName;
                classFilesPaths.put(qualifiedName, f.getPath());
            } catch (IOException ex) {
                Logger.getLogger(LocalProjectCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private String getPackageFromSourceFile(String file) {
        FileInputStream in = null;
        CompilationUnit cu = null;
        try {
            in = new FileInputStream(file);
            cu = JavaParser.parse(in);
        } catch (FileNotFoundException ex) {
//            Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseProblemException ex) {
            Logger.getLogger(LocalProjectCrawler.class.getName()).log(Level.WARNING, null, ex + " file: " + file);
        } catch (Exception ex) {
            Logger.getLogger(LocalProjectCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
//                    Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (cu != null) {
            Optional<PackageDeclaration> declaration = cu.getPackageDeclaration();
            if (declaration.isPresent()) {
                return cu.getPackageDeclaration().get().getNameAsString();
            } else {
                return "";
            }

        } else {
            return "";
        }
    }

    private boolean isTestClass(String classPath) {
        FileInputStream in = null;
        CompilationUnit cu = null;
        try {
            in = new FileInputStream(classPath);
            cu = JavaParser.parse(in);
        } catch (FileNotFoundException ex) {
//            Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseProblemException ex) {
            Logger.getLogger(LocalProjectCrawler.class.getName()).log(Level.WARNING, null, ex + " file: " + classPath);
        } catch (Exception ex) {
            Logger.getLogger(LocalProjectCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
//                    Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (cu != null) {
            List<ImportDeclaration> imports = cu.getImports();
            for (ImportDeclaration i : imports) {
                if (i.getName().toString().equals("org.junit.Test")
                        || i.getNameAsString().equals("junit.framework.TestCase")
                        || i.getNameAsString().equals("org.junit.Assert")) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<JavaSourceCodeClass> getJavaFiles() {
        final List<JavaSourceCodeClass> files = new ArrayList();
        files.addAll(this.javaFiles.values());
        return files;
    }

    private List<JavaTestClass> getTestFiles() {
        final List<JavaTestClass> javaTestClasses = new ArrayList();
        javaTestClasses.addAll(this.testFiles.values());
        return javaTestClasses;
    }

    public ProjectData getProjectData() {
        return projectData;
    }
}
