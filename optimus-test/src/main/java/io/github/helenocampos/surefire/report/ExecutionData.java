/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.report;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class ExecutionData
{

    /**
     * @return the executionTime
     */
    public float getExecutionTime() {
        return executionTime;
    }

    /**
     * @param executionTime the executionTime to set
     */
    public void setExecutionTime(float executionTime) {
        this.executionTime = executionTime;
    }

    private double APFD;
    private String technique;
    private int seededFaultsAmount;
    private int amountExecutedTests;
    private String testGranularity;
    private String projectPath;
    private List<TestExecution> executedTests;
    private List<String> faultRevealingTests;
    private String executionDate;
    private float executionTime;

    public ExecutionData()
    {
        this.executedTests = new LinkedList<TestExecution>();
    }
    
    public ExecutionData(String projectPath)
    {
        this.projectPath = projectPath;
        this.executedTests = new LinkedList<TestExecution>();
    }

    public double getAPFD()
    {
        return APFD;
    }

    public void setAPFD(double APFD)
    {
        this.APFD = APFD;
    }

    public String getTechnique()
    {
        return technique;
    }

    public void setTechnique(String technique)
    {
        this.technique = technique;
    }

    public int getSeededFaultsAmount()
    {
        return seededFaultsAmount;
    }

    public void setSeededFaultsAmount(int seededFaultsAmount)
    {
        this.seededFaultsAmount = seededFaultsAmount;
    }

    public int getAmountExecutedTests()
    {
        return amountExecutedTests;
    }

    public void setAmountExecutedTests(int amountExecutedTests)
    {
        this.amountExecutedTests = amountExecutedTests;
    }

    public String getTestGranularity()
    {
        return testGranularity;
    }

    public void setTestGranularity(String testGranularity)
    {
        this.testGranularity = testGranularity;
    }

    //   /target/optimus-reports/data
    public void writeExecutionData()
    {

        List<ExecutionData> existentData = readExecutionData();
        existentData.add(this);
        XStream xstream = new XStream();
        xstream.alias("ExecutionData", ExecutionData.class);
        xstream.alias("TestExecution", TestExecution.class);
        List<String> lines = Arrays.asList(xstream.toXML(existentData));

        try
        {
            Files.write(getExecutionDataPath(), lines, Charset.forName("UTF-8"));
        } catch (IOException ex)
        {
            System.out.println("could not write: " + ex.getMessage());
        }
    }

    private Path getExecutionDataPath()
    {
        Path filePath = Paths.get(this.projectPath, "target", "optimus-reports", "data", "executionData.xml");
        File file = filePath.toFile();
        file.getParentFile().mkdirs();
        try
        {
            file.createNewFile();
        } catch (IOException ex)
        {
            System.out.println("could not create file: " + ex.getMessage());
        }

        return filePath;
    }

    public List<ExecutionData> readExecutionData()
    {
        XStream xstream = new XStream();
        xstream.alias("ExecutionData", ExecutionData.class);
        xstream.alias("TestExecution", TestExecution.class);
        xstream.alias("list", List.class);
        List<ExecutionData> dataExecutions = new ArrayList<ExecutionData>();
        try
        {
            File file = getExecutionDataPath().toFile();
            if (file.length() > 0)
            {
                dataExecutions = (List<ExecutionData>) xstream.fromXML(getExecutionDataPath().toFile());
            }
        } catch (Exception e)
        {
            System.out.println("exception: " + e.getMessage());
        }

        return dataExecutions;
    }

    public String getProjectPath()
    {
        return projectPath;
    }

    public void setProjectPath(String projectPath)
    {
        this.projectPath = projectPath;
    }

    public List<TestExecution> getExecutedTests()
    {
        return executedTests;
    }

    public void setExecutedTests(List<TestExecution> executedTests)
    {
        this.executedTests = executedTests;
    }

    public List<String> getFaultRevealingTests()
    {
        return faultRevealingTests;
    }

    public void setFaultRevealingTests(List<String> faultRevealingTests)
    {
        this.faultRevealingTests = faultRevealingTests;
    }
    
     public String[] getValues()
    {
        String[] values = new String[8];
        values[0] = getProjectName();
        values[1] = this.technique;
        values[2] = Double.toString(this.APFD);
        values[3] = Integer.toString(this.seededFaultsAmount);
        values[4] = Integer.toString(this.amountExecutedTests);
        values[5] = this.testGranularity;
        values[6] = Float.toString(this.executionTime);
        values[7] = this.executionDate;
        return values;
    }
     
     private String getProjectName(){
         String[] folders;
         if(projectPath.contains("/")){
             folders = projectPath.split("/");
         }else{
             folders = projectPath.split("\\\\");
         }
         if(folders!=null){
             return folders[folders.length-1];
         }else{
             return projectPath;
         }
     }

    /**
     * @return the executionDate
     */
    public String getExecutionDate() {
        return executionDate;
    }

    /**
     * @param executionDate the executionDate to set
     */
    public void setExecutionDate(String executionDate) {
        this.executionDate = executionDate;
    }
}
