/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.executiontraceanalyzer;

import io.github.helenocampos.extractor.model.TestMethod;
import io.github.helenocampos.extractor.model.JavaTestClass;
import io.github.helenocampos.extractor.model.ProjectData;
import io.github.helenocampos.testing.AbstractTest;
import io.github.helenocampos.testing.TestGranularity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author helenocampos
 */
public class ExecutionTraceAnalyzer
{

    private ProjectData projectData;
    private HashMap<String, TestExecutionProfile> testsProfiles;

    public ExecutionTraceAnalyzer()
    {
        initializeAnalyzer();
    }

    private void initializeAnalyzer()
    {
        this.projectData = ProjectData.getProjectDataFromFile();
        this.testsProfiles = new HashMap<>();
        if (this.projectData != null)
        {
            executeCoberturaPlugin(new File(projectData.getProjectPath()));
            this.projectData = ProjectData.getProjectDataFromFile();
            HashMap<String, JavaTestClass> tests = projectData.getTests();
            for (String testClassName : tests.keySet())
            {
                JavaTestClass testClass = tests.get(testClassName);
                HashMap<String, TestMethod> testMethods = testClass.getMethods();
                for (String testMethodName : testMethods.keySet())
                {
                    TestMethod classMethod = testMethods.get(testMethodName);
                    TestExecutionProfile executionProfile = new TestExecutionProfile(classMethod, projectData.getClasses());
                    testsProfiles.put(testClassName + "." + testMethodName, executionProfile);
                }
            }
        }
    }

    public float getTestExecutionScore(AbstractTest test)
    {
        float score = 0;
        if (test.getTestGranularity().equals(TestGranularity.METHOD))
        {
            score = getTestExecutionCount(test.getQualifiedName());
        } else if (test.getTestGranularity().equals(TestGranularity.CLASS))
        {
            JavaTestClass testClass = projectData.getTestClassByName(test.getQualifiedName());
            for (TestMethod method : testClass.getMethods().values())
            {
                score += getTestExecutionCount(testClass.getQualifiedName() + "." + method.getName());
            }
        }
        return score;
    }

    private float getTestExecutionCount(String testName)
    {
        float score = 0;
        TestExecutionProfile testProfile = testsProfiles.get(testName);
        if (testProfile != null)
        {
            score = testProfile.getTotalLinesExecuted();
        }
        return score;
    }

    private void executeCoberturaPlugin(File projectFolder)
    {
        deleteExistingCoberturaFile();
        Runtime rt = Runtime.getRuntime();
        invokeProcess(rt, projectFolder);
    }

    private void deleteExistingCoberturaFile()
    {
        Path coberturaFilePath = Paths.get(projectData.getProjectPath(), "target", "cobertura", "cobertura.ser");
        File coberturaFile = coberturaFilePath.toFile();
        if (coberturaFile.exists())
        {
            coberturaFile.delete();
        }
    }

    private void invokeProcess(Runtime rt, File folder)
    {
        try
        {
            String mvnInvokation = "mvn cobertura:cobertura -Dcobertura.report.format=xml -Dprioritization=default -Dgranularity=method -Dmaven.test.skip=false -DskipTests=false -Dskip=false -Dmaven.surefire.skip=false -DcollectCoverageData=false -DcollectCoberturaData=true";

            if (System.getProperty("os.name").indexOf("win") >= 0)
            {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", mvnInvokation);
                pb.directory(folder);
                Process p = pb.start();
                StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", false);
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", false);
                errorGobbler.start();
                outputGobbler.start();
                p.waitFor();
            } else
            {
                Process pr = rt.exec(mvnInvokation, new String[0], folder);
                pr.waitFor();
            }
        } catch (IOException ex)
        {
            Logger.getLogger(ExecutionTraceAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex)
        {
            Logger.getLogger(ExecutionTraceAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getTestOrderedSequence(List<AbstractTest> tests)
    {
        String frequencyProfile = "";
        for (AbstractTest test : tests)
        {
            if (test.getTestGranularity().equals(TestGranularity.METHOD))
            {
                frequencyProfile += getTestFrequencyProfile(test.getQualifiedName());
            } else if (test.getTestGranularity().equals(TestGranularity.CLASS))
            {
                JavaTestClass testClass = projectData.getTestClassByName(test.getQualifiedName());
                
                for (TestMethod method : testClass.getMethods().values())
                {
                    frequencyProfile += getTestFrequencyProfile(testClass.getQualifiedName() + "." + method.getName());
                }
            }
        }
        return TestExecutionProfile.getOrderedSequence(frequencyProfile);
    }

    public String getTestOrderedSequence(AbstractTest test)
    {
        String orderedSequence = "";
        if (test.getTestGranularity().equals(TestGranularity.METHOD))
        {
            orderedSequence = getTestOrderedSequence(test.getQualifiedName());
        } else if (test.getTestGranularity().equals(TestGranularity.CLASS))
        {
            JavaTestClass testClass = projectData.getTestClassByName(test.getQualifiedName());
            String frequencyProfile = "";
            for (TestMethod method : testClass.getMethods().values())
            {
                frequencyProfile += getTestFrequencyProfile(testClass.getQualifiedName() + "." + method.getName());
            }
            orderedSequence = TestExecutionProfile.getOrderedSequence(frequencyProfile);
        }
        return orderedSequence;
    }

    private String getTestOrderedSequence(String testName)
    {
        String orderedSequence = "";
        TestExecutionProfile testProfile = testsProfiles.get(testName);
        if (testProfile != null)
        {
            orderedSequence = testProfile.getOrderedSequence();
        }
        return orderedSequence;
    }

    private String getTestFrequencyProfile(String testName)
    {
        String frequencyProfile = "";
        TestExecutionProfile testProfile = testsProfiles.get(testName);
        if (testProfile != null)
        {
            frequencyProfile = testProfile.getFrequencyProfile();
        }
        return frequencyProfile;
    }
}
