/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire.junit4;

import info.heleno.optimushistoricalanalyzer.HistoricalAnalyzer;
import info.heleno.optimushistoricalanalyzer.domain.TestCaseExecution;
import info.heleno.optimushistoricalanalyzer.domain.TestSetExecution;
import info.heleno.testing.TestGranularity;
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
    private TestGranularity granualarity;
    private String projectName;
    private String lastClass;
    private TestCaseExecution currentTest;
    private TestSetExecution testSet;

    public HistoricalDataListener(String granularity, String dbPath, String projectName)
    {
        super();
        this.analyzer = new HistoricalAnalyzer(dbPath);
        this.granualarity = TestGranularity.getGranularityByName(granularity);
        this.testSet = new TestSetExecution(System.currentTimeMillis(),projectName);
        this.lastClass = "";
        this.currentTest = null;
        this.projectName = projectName;
    }

    @Override
    public void testStarted(Description description) throws Exception
    {
        super.testStarted(description);
        if (this.granualarity.equals(TestGranularity.CLASS))
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
        this.testSet.addExecutedTest(currentTest);
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
        if (this.granualarity.equals(TestGranularity.METHOD))
        {
            testName = testName + "." + description.getMethodName();
        }
        return testName;
    }

    private TestCaseExecution instantiateTestExecution(String testName)
    {
        TestCaseExecution execution = new TestCaseExecution();
        execution.setTestName(testName);
        execution.setResult(true);
        setGranularity(execution);
        return execution;
    }

    private void setGranularity(TestCaseExecution execution)
    {
        if (this.granualarity.equals(TestGranularity.METHOD))
        {
            execution.setGranularity(TestGranularity.METHOD);
        } else
        {
            execution.setGranularity(TestGranularity.CLASS);
        }
    }
    
    public void registerExecution(){
        this.analyzer.registerTestSetExecution(testSet);
    }

}
