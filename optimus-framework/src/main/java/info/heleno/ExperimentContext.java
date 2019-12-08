/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno;

/**
 *
 * @author helenocampos
 */
public class ExperimentContext
{
    private int executionsAmount;
    private int seededFaultsAmount;
    private int testCasesAmount;
    private String testGranularity;

    public ExperimentContext(int executionsAmount, int seededFaultsAmount, int testCasesAmount, String testGranularity)
    {
        this.executionsAmount = executionsAmount;
        this.seededFaultsAmount = seededFaultsAmount;
        this.testCasesAmount = testCasesAmount;
        this.testGranularity = testGranularity;
    }

    public int getExecutionsAmount()
    {
        return executionsAmount;
    }

    public int getSeededFaultsAmount()
    {
        return seededFaultsAmount;
    }

    public int getTestCasesAmount()
    {
        return testCasesAmount;
    }

    public String getTestGranularity()
    {
        return testGranularity;
    }
    
    
}
