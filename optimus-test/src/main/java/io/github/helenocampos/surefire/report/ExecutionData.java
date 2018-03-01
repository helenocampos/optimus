/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.report;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class ExecutionData
{

    private double APFD;
    private double optimalAPFD;
    private String technique;
    private int seededFaultsAmount;
    private int amountExecutedTests;
    private String testGranularity;
    private String projectPath;

    public ExecutionData()
    {

    }

    public double getAPFD()
    {
        return APFD;
    }

    public void setAPFD(double APFD)
    {
        this.APFD = APFD;
    }

    public double getOptimalAPFD()
    {
        return optimalAPFD;
    }

    public void setOptimalAPFD(double optimalAPFD)
    {
        this.optimalAPFD = optimalAPFD;
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
}
