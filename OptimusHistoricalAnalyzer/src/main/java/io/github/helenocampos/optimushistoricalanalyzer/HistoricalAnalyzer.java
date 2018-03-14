/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.optimushistoricalanalyzer;

import io.github.helenocampos.optimushistoricalanalyzer.dao.TestExecutionDAO;
import io.github.helenocampos.optimushistoricalanalyzer.domain.TestExecution;

/**
 *
 * @author helenocampos
 */
public class HistoricalAnalyzer
{

    private String dbURL;
    private TestExecutionDAO dao;

    public HistoricalAnalyzer(String dbURL)
    {
        this.dbURL = dbURL;
        this.dao = new TestExecutionDAO(dbURL);
    }

    public float getTestFailureRate(String testName, String projectName)
    {
        int failureAmount = this.dao.getTestFailureAmount(testName, projectName);
        int executedAmount = this.dao.getTestExecutionAmount(testName, projectName);
        if (executedAmount == 0)
        {
            return 0;
        } else
        {
            return failureAmount / (float)executedAmount;
        }
    }

    public void registerTestExecution(TestExecution execution)
    {
        this.dao.insertExecution(execution);
    }

}
