/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.executiontraceanalyzer;

import com.sun.javafx.PlatformUtil;
import io.github.helenocampos.executiontraceanalyzer.cobertura.CoberturaCoverage;
import io.github.helenocampos.executiontraceanalyzer.cobertura.CoberturaParser;
import io.github.helenocampos.extractor.model.ClassMethod;
import io.github.helenocampos.extractor.model.JavaTestClass;
import io.github.helenocampos.extractor.model.ProjectData;
import io.github.helenocampos.testing.AbstractTest;
import io.github.helenocampos.testing.Granularity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author helenocampos
 */
public class ExecutionTraceAnalyzer
{

    private ProjectData projectData;
    private CoberturaCoverage coberturaData;
    private HashMap<String, TestExecutionProfile> testsProfiles;

    public ExecutionTraceAnalyzer()
    {
        this.projectData = ProjectData.getProjectDataFromFile();
        this.testsProfiles = new HashMap<>();
        if (this.projectData != null)
        {
            //execute cobertura plugin
            executeCoberturaPlugin(new File(projectData.getProjectPath()));

            parseCoberturaReport();
            coberturaData.indexClasses();
            HashMap<String, JavaTestClass> tests = projectData.getTests();
            for(String testClassName: tests.keySet()){
                JavaTestClass testClass = tests.get(testClassName);
                HashMap<String,ClassMethod> testMethods = testClass.getMethods();
                for(String testMethodName: testMethods.keySet()){
                    ClassMethod classMethod = testMethods.get(testMethodName);
                    TestExecutionProfile executionProfile = new TestExecutionProfile(classMethod, coberturaData.getClassesIndex());
                    testsProfiles.put(testClassName+"."+testMethodName, executionProfile);
                }
            }
        }
    }
    
    public float getTestExecutionScore(AbstractTest test){
        float score = 0;
        if(test.getTestGranularity().equals(Granularity.METHOD)){
            score = getTestExecutionCount(test.getQualifiedName());
        }else if(test.getTestGranularity().equals(Granularity.CLASS)){
            JavaTestClass testClass = projectData.getTestClassByName(test.getQualifiedName());
            for(ClassMethod method: testClass.getMethods().values()){
                score+=getTestExecutionCount(testClass.getQualifiedName()+"."+method.getName());
            }
        }
        return score;
    }
    
    private float getTestExecutionCount(String testName){
        TestExecutionProfile testProfile = testsProfiles.get(testName);
        return testProfile.getTotalLinesExecuted();
    }

    //parse cobertura report  /target/site/cobertura/coverage.xml
    private void parseCoberturaReport()
    {
        Path coverageReport = Paths.get(projectData.getProjectPath(), "target", "site", "cobertura", "coverage.xml");
        File reportFile = coverageReport.toFile();
        if (reportFile.exists())
        {
            this.coberturaData = CoberturaParser.parse(reportFile);
        }
    }

    private void executeCoberturaPlugin(File projectFolder)
    {
        deleteExistingCoberturaFile();
        Runtime rt = Runtime.getRuntime();
        invokeProcess(rt, projectFolder);
    }
    
    private void deleteExistingCoberturaFile(){
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
            String mvnInvokation = "mvn cobertura:cobertura -Dcobertura.report.format=xml";

            if (PlatformUtil.isWindows())
            {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", mvnInvokation);
                pb.directory(folder);
                Process p = pb.start();
                StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", true);
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", true);
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
}
