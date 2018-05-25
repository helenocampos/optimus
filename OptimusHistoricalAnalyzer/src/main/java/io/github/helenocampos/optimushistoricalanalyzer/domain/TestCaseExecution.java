/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.optimushistoricalanalyzer.domain;

import io.github.helenocampos.testing.TestGranularity;

/**
 *
 * @author helenocampos
 */
public class TestCaseExecution
{

    private int id;
    private String testName;
    private TestGranularity granularity;
    private double executionTime;
    private boolean result;
    private int testSetId;
    
    public TestCaseExecution(int id, String testName, TestGranularity granularity, double executionTime, boolean result, int testSetId)
    {
        this.id = id;
        this.testName = testName;
        this.granularity = granularity;
        this.executionTime = executionTime;
        this.result = result;
        this.testSetId = testSetId;
    }
    
    public TestCaseExecution()
    {
    }
   
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTestName()
    {
        return testName;
    }

    public void setTestName(String testName)
    {
        this.testName = testName;
    }

    public TestGranularity getGranularity()
    {
        return granularity;
    }

    public void setGranularity(TestGranularity granularity)
    {
        this.granularity = granularity;
    }

    public double getExecutionTime()
    {
        return executionTime;
    }

    public void setExecutionTime(double executionTime)
    {
        this.executionTime = executionTime;
    }

    public boolean isResult()
    {
        return result;
    }

    public void setResult(boolean result)
    {
        this.result = result;
    }

    public int getTestSetId()
    {
        return testSetId;
    }

    public void setTestSetId(int testSetId)
    {
        this.testSetId = testSetId;
    }
}
