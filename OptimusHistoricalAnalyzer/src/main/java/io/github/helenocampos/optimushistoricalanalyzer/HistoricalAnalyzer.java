/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.optimushistoricalanalyzer;

import io.github.helenocampos.optimushistoricalanalyzer.dao.TestExecutionDAO;
import io.github.helenocampos.optimushistoricalanalyzer.domain.TestCaseExecution;
import io.github.helenocampos.optimushistoricalanalyzer.domain.TestSetExecution;
import java.io.File;
import java.util.List;

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
        this.dbURL = checkForPreviousVersionDB(dbURL);
        this.dao = new TestExecutionDAO(dbURL);
    }

    private String checkForPreviousVersionDB(String path)
    {
        File f = new File(path + ".backup");
        if (f.exists() && !f.isDirectory()){
            return path + ".backup";
        }else{
            return path;
        }
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
            return failureAmount / (float) executedAmount;
        }
    }

    public float getRecentTestFailureScore(String testName, String projectName)
    {
        List<TestSetExecution> testSetExecutions = this.dao.getTestSetExecutions(testName, projectName);
        int freshnessOfExecution = 1;
        int score = 0;
        for (TestSetExecution testSetExecution : testSetExecutions)
        {
            for (TestCaseExecution testCaseExecution : testSetExecution.getExecutedTests())
            {
                if (!testCaseExecution.isResult())
                {
                    score += freshnessOfExecution;
                }
            }
            freshnessOfExecution++;
        }
        return score;
    }

    public void registerTestSetExecution(TestSetExecution execution)
    {
        this.dao.insertTestSetExecution(execution);
    }

}
