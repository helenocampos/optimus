/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.junit4;

/**
 *
 * @author helenocampos
 */
public class FaultRevealingTest
{

    private String testName;
    private int executionOrder;

    public FaultRevealingTest(String testName, int executionOrder)
    {
        this.testName = testName;
        this.executionOrder = executionOrder;
    }

    public int getExecutionOrder()
    {
        return executionOrder;
    }

    public void setExecutionOrder(int executionOrder)
    {
        this.executionOrder = executionOrder;
    }

    public String getTestName()
    {
        return testName;
    }

    public void setTestName(String testName)
    {
        this.testName = testName;
    }
    
    public String toString(){
        return this.getTestName();
    }
}
