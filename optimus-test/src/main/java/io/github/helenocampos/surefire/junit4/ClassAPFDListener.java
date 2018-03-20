/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.junit4;

import io.github.helenocampos.surefire.report.ExecutionData;
import io.github.helenocampos.surefire.report.TestExecution;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Result;

/**
 *
 * @author helenocampos
 */
public class ClassAPFDListener extends APFDListener
{

    private String lastExecutedClass;

    public ClassAPFDListener(String granularity)
    {
        super(granularity);
        this.lastExecutedClass = "";
    }

    @Override
    public void testFinished(Description description) throws Exception
    {
        super.testFinished(description);
        String testName = description.getClassName();
        if (!lastExecutedClass.equals(testName))
        {
            this.getExecutedTests().add(new TestExecution(testName));
            if (this.getFaultRevealingTests().containsKey(testName))
            {
                this.getFaultRevealingTests().put(testName, getExecutedTests().size());
            }
            this.lastExecutedClass = testName;
        }
    }
    
    @Override
    public void testRunFinished(Result result) throws Exception
    {
        super.testRunFinished(result);
        if(getExecutedTests().size()>=1){
            TestExecution execution = getExecutedTests().get(getExecutedTests().size()-1);
            execution.setTestResult(result.wasSuccessful());
            execution.setExecutionTime(result.getRunTime());
        }
    }
}
