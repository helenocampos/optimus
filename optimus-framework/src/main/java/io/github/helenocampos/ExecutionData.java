package io.github.helenocampos;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    private static File getExecutionDataPath(File projectFolder)
    {
        Path filePath = Paths.get(projectFolder.getAbsolutePath(), "target", "optimus-reports", "data", "executionData.xml");
        File file = filePath.toFile();
        file.getParentFile().mkdirs();
        try
        {
            file.createNewFile();
        } catch (IOException ex)
        {
            System.out.println("could not create file: " + ex.getMessage());
        }

        return file;
    }

    public static List<ExecutionData> readExecutionData(File projectFolder)
    {
        XStream xstream = new XStream();
        xstream.alias("ExecutionData", ExecutionData.class);
        xstream.alias("list", List.class);
        List<ExecutionData> dataExecutions = new ArrayList<ExecutionData>();
        try
        {
            File file = getExecutionDataPath(projectFolder);
            if (file.length() > 0)
            {
                dataExecutions = (List<ExecutionData>) xstream.fromXML(file);
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

    /*
        return all values in a String array
     */
    public String[] getValues()
    {
        String[] values = new String[6];
        values[0] = this.technique;
        values[1] = Double.toString(this.APFD);
        values[2] = Double.toString(this.optimalAPFD);
        values[3] = Integer.toString(this.seededFaultsAmount);
        values[4] = Integer.toString(this.amountExecutedTests);
        values[5] = this.testGranularity;
        
        return values;
    }
}
