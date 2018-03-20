/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.report;

/**
 *
 * @author helenocampos
 */
public class TestExecution
{
    private String testName;
    private boolean testResult;
    private long executionTime;
    
    public TestExecution(String testName){
        this.testName = testName;
        this.testResult = true;
    }

    public String getTestName()
    {
        return testName;
    }

    public void setTestName(String testName)
    {
        this.testName = testName;
    }

    public boolean getTestResult()
    {
        return testResult;
    }

    public void setTestResult(boolean testResult)
    {
        this.testResult = testResult;
    }

    public long getExecutionTime()
    {
        return executionTime;
    }

    public void setExecutionTime(long executionTime)
    {
        this.executionTime = executionTime;
    }
    
    
}
