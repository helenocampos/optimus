/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.junit4;

import io.github.helenocampos.surefire.report.TestExecution;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 *
 * @author helenocampos
 */
public class MethodAPFDListener extends APFDListener {

    String lastExecutedTest = "";

    public MethodAPFDListener(String granularity) {
        super(granularity);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);

        String testName = description.getClassName() + "." + description.getMethodName();
        this.getExecutedTests().put(testName, new TestExecution(testName));
        this.lastExecutedTest = testName;
        if (this.getFaultRevealingTests().containsKey(testName)) {
            this.getFaultRevealingTests().put(testName, getExecutedTests().size());
        }
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result);
        if (getExecutedTests().size() >= 1) {
            TestExecution execution = getExecutedTests().get(this.lastExecutedTest);
            execution.setTestResult(result.wasSuccessful());
            if(!result.wasSuccessful()){
                this.incrementFaultsAmount();
            }
            execution.setExecutionTime(result.getRunTime());
        }
    }
}
