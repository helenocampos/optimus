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
public class ClassAPFDListener2 extends APFDListener {

    private String lastExecutedClass;

    public ClassAPFDListener2(String granularity) {
        super(granularity);
        this.lastExecutedClass = "";
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);
        String testName = description.getClassName() + "." + description.getMethodName();
        this.getExecutedTests().put(testName, new TestExecution(testName));
        if (this.getFaultRevealingTests().containsKey(testName)) {
            this.getFaultRevealingTests().put(testName, getExecutedTests().size());
        }
//        this.lastExecutedClass = testName;

    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result);
        if (getExecutedTests().size() >= 1) {
            for (Failure failure : result.getFailures()) {
                Description failledTest = failure.getDescription();
                String testName = failledTest.getClassName() + "." + failledTest.getMethodName();
                TestExecution execution = getExecutedTests().get(testName);
                execution.setTestResult(false);
            }
        }
    }
}
