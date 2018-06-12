package io.github.helenocampos.extractor.model;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author helenocampos
 */
public class ProjectData
{

    private String projectPath;
    private String sourceBackupPath;
    private HashMap<String, JavaSourceCodeClass> classes;
    private HashMap<String, JavaTestClass> tests;

    private static ProjectData INSTANCE = null;

    public ProjectData(String projectPath)
    {
        classes = new HashMap<String, JavaSourceCodeClass>();
        tests = new HashMap<String, JavaTestClass>();
        this.projectPath = projectPath;
        this.sourceBackupPath = "";
    }

    public List<JavaClass> getAllClasses()
    {
        List<JavaClass> allClasses = new LinkedList<JavaClass>();
        allClasses.addAll(classes.values());
        allClasses.addAll(tests.values());
        return allClasses;
    }

    public void addClass(JavaSourceCodeClass javaClass)
    {
        if (!classes.containsValue(javaClass))
        {
            this.classes.put(javaClass.getSimpleName(), javaClass);
        }
    }

    public void addTestClass(JavaTestClass testClass)
    {
        if (!tests.containsValue(testClass))
        {
            this.tests.put(testClass.getSimpleName(), testClass);
        }
    }

    public HashMap<String, JavaSourceCodeClass> getClasses()
    {
        return classes;
    }

    public void setClasses(HashMap<String, JavaSourceCodeClass> classes)
    {
        this.classes = classes;
    }

    public HashMap<String, JavaTestClass> getTests()
    {
        return tests;
    }

    public void setTests(HashMap<String, JavaTestClass> tests)
    {
        this.tests = tests;
    }

    public void writeProjectDataFile()
    {
//        normalizeCoverageData();
        XStream xstream = new XStream();
        List<String> lines = Arrays.asList(xstream.toXML(this));
        Path file = Paths.get(projectPath, "projectData.xml");
        try
        {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException ex)
        {
//            Logger.getLogger(LocalProjectCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ProjectData getProjectDataFromFile()
    {
        ProjectData loadedProjectData = null;
        Path file = Paths.get("projectData.xml");
        try
        {
            String firstLine = Files.lines(file).findFirst().get();
            if (validateProjectData(firstLine))
            {
                XStream xstream = new XStream();
                loadedProjectData = (ProjectData) xstream.fromXML(file.toFile());
            }
        } catch (IOException ex)
        {

        }
        return loadedProjectData;
    }

    public static ProjectData getProjectDataFromFile(String projectPath)
    {
        ProjectData loadedProjectData = null;
        Path file = Paths.get(projectPath, "projectData.xml");

        try
        {
            String firstLine = Files.lines(file).findFirst().get();
            if (validateProjectData(firstLine))
            {
                XStream xstream = new XStream();
                loadedProjectData = (ProjectData) xstream.fromXML(file.toFile());
            }
        } catch (IOException ex)
        {

        }
        return loadedProjectData;
    }

    public static ProjectData getProjectData()
    {
        if (INSTANCE == null)
        {
            INSTANCE = getProjectDataFromFile();
        }
        return INSTANCE;
    }

    private static boolean validateProjectData(String firstLine)
    {
        if (firstLine.equals("<io.github.helenocampos.extractor.model.ProjectData>"))
        {
            return true;
        }
        return false;
    }

    public JavaSourceCodeClass getClassByName(String className)
    {
        return classes.get(className);
    }

    public JavaSourceCodeClass getClassByCoverageName(String coverageName)
    {
        for (JavaSourceCodeClass clazz : classes.values())
        {
            if (clazz.getCoverageName().equals(coverageName))
            {
                return clazz;
            }
        }
        return null;
    }

    public void updateClasses(HashMap<String, JavaSourceCodeClass> classes)
    {
        // adds new classes if they didnt exist in previous build
        for (String key : classes.keySet())
        {
            this.classes.putIfAbsent(key, classes.get(key));
        }

        //removes classes that dont exist in the new build
        Iterator<String> keys = this.classes.keySet().iterator();
        while (keys.hasNext())
        {
            String key = keys.next();
            if (classes.get(key) == null)
            {
                keys.remove();
            }
        }
    }

    public void updateTests(HashMap<String, JavaTestClass> tests)
    {
        for (String key : tests.keySet())
        {
            JavaTestClass tcExistent = this.tests.get(key);
            if (tcExistent != null)
            {
                JavaTestClass tcNew = tests.get(key);
                updateMethods(tcNew, key);
            } else
            {
                this.tests.put(key, tests.get(key));
            }

        }
        //removes tests that dont exist in the new build
        Iterator<String> keys = this.tests.keySet().iterator();
        while (keys.hasNext())
        {
            String key = keys.next();
            if (tests.get(key) == null)
            {
                keys.remove();
            }
        }
    }

    public void updateMethods(JavaTestClass tcNew, String tcNewKey)
    {
        // adds new methods if they didnt exist in previous class
        JavaTestClass tcExistent = this.tests.get(tcNewKey);
        HashMap<String, TestMethod> newMethods = tcNew.getMethods();
        if (tcExistent != null)
        {
            HashMap<String, TestMethod> previousMethods = tcExistent.getMethods();
            for (String key : newMethods.keySet())
            {
                previousMethods.putIfAbsent(key, newMethods.get(key));
            }

            //removes method that dont exist in the new class
            Set<String> keys = previousMethods.keySet();
            for (String key : keys)
            {
                if (newMethods.get(key) == null)
                {
                    previousMethods.remove(key);
                }
            }
        }
    }

    public String getProjectPath()
    {
        return projectPath;
    }

    public void setProjectPath(String projectPath)
    {
        this.projectPath = projectPath;
    }

    public JavaTestClass getTestClassByName(String testClassName)
    {
        return tests.get(testClassName);
    }

    private void normalizeCoverageData()
    {
        for (JavaTestClass testClass : tests.values())
        {
            for (TestMethod testMethod : testClass.getMethods().values())
            {
                for (JavaClass clazz : classes.values())
                {
                    testMethod.getCoverage().includeRemainingClassesData(clazz);
                }
            }
        }
    }

    public String getSourceBackupPath()
    {
        return sourceBackupPath;
    }

    public void setSourceBackupPath(String sourceBackupPath)
    {
        this.sourceBackupPath = sourceBackupPath;
    }
}
