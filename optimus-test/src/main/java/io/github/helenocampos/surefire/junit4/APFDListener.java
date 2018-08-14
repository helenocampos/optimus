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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author helenocampos
 */
public abstract class APFDListener extends RunListener {

    private int faultsAmount;
    private HashMap<String, Integer> faultRevealingTests;
    private LinkedHashMap<String, TestExecution> executedTests;
    private String testGranularity;

    public APFDListener(String testGranularity) {
        super();
        this.testGranularity = testGranularity;
        resetListener();
    }

    protected void readTestsFile() {
        Path file = Paths.get("TestsRevealingFaults");
        List<String> fileLines = new LinkedList<String>();
        try {
            fileLines = Files.readAllLines(file, Charset.forName("UTF-8"));
        } catch (IOException ex) {
//            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (String line : fileLines) {
            String testName = line;
            if (this.faultRevealingTests.get(testName) == null) {
                this.faultRevealingTests.put(testName, 0);
            }
        }
//        this.setFaultsAmount(fileLines.size());
    }

    protected String getTestName(String testName) {

        if (this.testGranularity.equals("class")) {
            if (testName.contains(".")) {
                testName = testName.substring(0, testName.lastIndexOf("."));

            }
        }
        return testName;
    }

    public ExecutionData calculateAPFD() {
        double n = this.getExecutedTests().size();
        double m = this.getFaultsAmount();
        double APFD = 1 - (sum(this.getFaultRevealingTests().values()) / (n * m)) + (1 / (2 * n));

        System.out.println("Achieved APFD=" + APFD);
        return getExecutionData(APFD);
    }

    private ExecutionData getExecutionData(double APFD) {
        ExecutionData data = new ExecutionData();
        data.setAmountExecutedTests(this.getExecutedTests().size());
        data.setSeededFaultsAmount(this.getFaultsAmount());
        data.setAPFD(APFD);
        data.setExecutedTests(new LinkedList<>(this.getExecutedTests().values()));
        List<String> faultList = new LinkedList<String>();
        faultList.addAll(faultRevealingTests.keySet());
        data.setFaultRevealingTests(faultList);
        return data;
    }

    public int sum(Collection<Integer> list) {
        int sum = 0;
        for (Integer value : list) {
            sum += value;
        }
        return sum;
    }

    protected int getFaultsAmount() {
        return faultsAmount;
    }

    protected void setFaultsAmount(int faultsAmount) {
        this.faultsAmount = faultsAmount;
    }

    protected HashMap<String, Integer> getFaultRevealingTests() {
        return faultRevealingTests;
    }

    protected void setFaultRevealingTests(HashMap<String, Integer> faultRevealingTests) {
        this.faultRevealingTests = faultRevealingTests;
    }

    protected HashMap<String, TestExecution> getExecutedTests() {
        return executedTests;
    }

    protected void setExecutedTests(LinkedHashMap<String, TestExecution> executedTests) {
        this.executedTests = executedTests;
    }
    
    protected void incrementFaultsAmount(){
        this.faultsAmount++;
    }

    public void resetListener() {
        this.faultsAmount = 0;
        this.faultRevealingTests = new HashMap<String, Integer>();
        this.executedTests = new LinkedHashMap<String, TestExecution>();
        readTestsFile();
    }
}
