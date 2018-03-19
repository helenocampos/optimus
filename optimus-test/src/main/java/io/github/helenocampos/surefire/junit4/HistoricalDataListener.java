/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.junit4;

import io.github.helenocampos.optimushistoricalanalyzer.HistoricalAnalyzer;
import io.github.helenocampos.optimushistoricalanalyzer.domain.TestExecution;
import io.github.helenocampos.optimushistoricalanalyzer.domain.TestGranularity;
import io.github.helenocampos.surefire.ordering.Granularity;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author helenocampos
 */
public class HistoricalDataListener extends RunListener
{

    private HistoricalAnalyzer analyzer;
    private Granularity granualarity;
    private String projectName;
    private String lastClass;
    private TestExecution currentTest;

    public HistoricalDataListener(String granularity, String dbPath, String projectName)
    {
        super();
        this.analyzer = new HistoricalAnalyzer(dbPath);
        this.granualarity = Granularity.getGranularityByName(granularity);
        this.lastClass = "";
        this.currentTest = null;
        this.projectName = projectName;
    }

    @Override
    public void testStarted(Description description) throws Exception
    {
        super.testStarted(description);
        if (this.granualarity.equals(Granularity.CLASS))
        {
            if (!description.getClassName().equals(lastClass))
            {
                currentTest = instantiateTestExecution(getTestName(description));
                this.lastClass = description.getClassName();
            }
        } else
        {
            currentTest = instantiateTestExecution(getTestName(description));
        }

    }

    //called after very test (when class granularity, called after each class)
    @Override
    public void testRunFinished(Result result) throws Exception
    {
        super.testRunFinished(result); //To change body of generated methods, choose Tools | Templates.
        updateTestTime(result.getRunTime());
        this.analyzer.registerTestExecution(currentTest);
    }

    @Override
    public void testFailure(Failure failure) throws Exception
    {
        super.testFailure(failure);
        if (currentTest != null)
        {
            currentTest.setResult(false);
        }

    }

    private void updateTestTime(long timeMilis)
    {
        if (currentTest != null)
        {
            double timeSeconds = (double) timeMilis / (double) 1000;
            currentTest.setExecutionTime(timeSeconds);
        }
    }

    private String getTestName(Description description)
    {
        String testName = description.getClassName();
        if (this.granualarity.equals(Granularity.METHOD))
        {
            testName = testName + "." + description.getMethodName();
        }
        return testName;
    }

    private TestExecution instantiateTestExecution(String testName)
    {
        TestExecution execution = new TestExecution();
        execution.setTestName(testName);
        execution.setTimeStamp(System.currentTimeMillis());
        execution.setResult(true);
        execution.setProjectName(this.projectName);
        setGranularity(execution);
        return execution;
    }

    private void setGranularity(TestExecution execution)
    {
        if (this.granualarity.equals(Granularity.METHOD))
        {
            execution.setGranularity(TestGranularity.METHOD);
        } else
        {
            execution.setGranularity(TestGranularity.CLASS);
        }
    }

}
