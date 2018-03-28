package io.github.helenocampos.surefire.extractor;

import io.github.helenocampos.surefire.extractor.model.JavaSourceCodeClass;
import io.github.helenocampos.surefire.extractor.model.JavaTestClass;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import io.github.helenocampos.surefire.extractor.model.ProjectData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author helenocampos
 */
public class LocalProjectCrawler
{
    
    private HashMap<String, JavaSourceCodeClass> javaFiles;
    private HashMap<String, JavaTestClass> testFiles;
    private HashMap<String, String> classFilesPaths;
    private String projectPath;
    
    public LocalProjectCrawler(String projectPath)
    {
        javaFiles = new HashMap<String, JavaSourceCodeClass>();
        testFiles = new HashMap<String, JavaTestClass>();
        classFilesPaths = new HashMap<String, String>();
        this.projectPath = projectPath;
        crawl(new File(projectPath), "");
        resolveClassFilesLocation();
        ProjectData data = ProjectData.getProjectDataFromFile();
        if (data != null)
        {
            data.updateClasses(javaFiles);
            data.updateTests(testFiles);
        } else
        {
            data = new ProjectData(projectPath);
            data.setClasses(javaFiles);
            data.setTests(testFiles);
        }
        
        data.writeProjectDataFile();
    }
    
    private void resolveClassFilesLocation()
    {
        for (JavaSourceCodeClass file : javaFiles.values())
        {
            String path = classFilesPaths.get(file.getQualifiedName());
            if (path != null)
            {
                file.setClassFilePath(path);
            }
        }
        
        for (JavaTestClass file : testFiles.values())
        {
            String path = classFilesPaths.get(file.getQualifiedName());
            if (path != null)
            {
                file.setClassFilePath(path);
            }
        }
        
    }
    
    private void crawl(File f, String packageName)
    {
        if (f.isDirectory())
        {
            File[] subFiles = f.listFiles();
            for (File subFile : subFiles)
            {
                if (subFile.isDirectory())
                {
                    if (packageName.equals(""))
                    {
                        packageName = subFile.getName();
                    } else
                    {
                        packageName = packageName.concat("." + subFile.getName());
                    }
                }
                crawl(subFile, packageName);
            }
        } else
        {
            if (f.getName().contains(".java"))
            {
                String packageNameSource = getPackageFromSourceFile(f.getPath());
                if (isTestClass(f.getAbsolutePath()))
                {
                    JavaTestClass cls = new JavaTestClass(f.getName(), f.getPath(), this.projectPath, packageNameSource);
                    testFiles.put(cls.getQualifiedName(), cls);
                } else
                {
                    if (f.getName().endsWith(".java"))
                    {
                        JavaSourceCodeClass cls = new JavaSourceCodeClass(f.getName(), f.getPath(), this.projectPath, packageNameSource);
                        javaFiles.put(cls.getQualifiedName(), cls);
                    }
                }
            } else if (f.getName().contains(".class"))
            {
                try
                {
                    ClassParser parser = new ClassParser(f.getPath());
                    JavaClass cls = parser.parse();
                    String qualifiedName = f.getName().replace(".class", "");
                    qualifiedName = cls.getPackageName() + "." + qualifiedName;
                    classFilesPaths.put(qualifiedName, f.getPath());
                } catch (IOException ex)
                {
                    Logger.getLogger(LocalProjectCrawler.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        }
    }
    
    private String getPackageFromSourceFile(String file)
    {
        FileInputStream in = null;
        CompilationUnit cu = null;
        try
        {
            in = new FileInputStream(file);
            cu = JavaParser.parse(in);
        } catch (FileNotFoundException ex)
        {
//            Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
        }  catch(Exception ex){
             Logger.getLogger(LocalProjectCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (IOException ex)
                {
//                    Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (cu != null)
        {
            return cu.getPackageDeclaration().get().getNameAsString();
        } else
        {
            return "";
        }
    }
    
    private boolean isTestClass(String classPath)
    {
        FileInputStream in = null;
        CompilationUnit cu = null;
        try
        {
            in = new FileInputStream(classPath);
            cu = JavaParser.parse(in);
        } catch (FileNotFoundException ex)
        {
//            Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex){
             Logger.getLogger(LocalProjectCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (IOException ex)
                {
//                    Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (cu != null)
        {
            List<ImportDeclaration> imports = cu.getImports();
            for (ImportDeclaration i : imports)
            {
                if (i.getName().toString().equals("org.junit.Test")
                        || i.getNameAsString().equals("junit.framework.TestCase"))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    private List<JavaSourceCodeClass> getJavaFiles()
    {
        final List<JavaSourceCodeClass> files = new ArrayList();
        files.addAll(this.javaFiles.values());
        return files;
    }
    
    private List<JavaTestClass> getTestFiles()
    {
        final List<JavaTestClass> javaTestClasses = new ArrayList();
        javaTestClasses.addAll(this.testFiles.values());
        return javaTestClasses;
    }
}
