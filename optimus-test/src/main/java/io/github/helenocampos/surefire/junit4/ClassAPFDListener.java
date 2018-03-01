/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.junit4;

import io.github.helenocampos.surefire.report.ExecutionData;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author helenocampos
 */
public class ClassAPFDListener extends RunListener
{

    private int executedTests;
    private int faultsAmount;
    private String lastExecutedClass;
    private List<FaultRevealingTest> faultRevealingTests;

    public ClassAPFDListener()
    {
        super();
        this.executedTests = 0;
        this.faultsAmount = 0;
        this.lastExecutedClass = "";
        this.faultRevealingTests = new LinkedList<FaultRevealingTest>();
        readTestsFile();
    }

    private void readTestsFile()
    {
        Path file = Paths.get("TestsRevealingFaults");
        List<String> fileLines = new LinkedList<String>();
        try
        {
            fileLines = Files.readAllLines(file, Charset.forName("UTF-8"));
        } catch (IOException ex)
        {
//            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (String line : fileLines)
        {
            if (line.contains("."))
            {
                String className = line.substring(0, line.lastIndexOf("."));
                this.faultRevealingTests.add(new FaultRevealingTest(className, 0));
            }

        }
        this.faultsAmount = fileLines.size();
    }

    public void testFinished(Description description) throws Exception
    {
        super.testFinished(description);
        String testName = description.getClassName();
        if (!lastExecutedClass.equals(testName))
        {
            this.executedTests++;
            setExecutionOrder(testName);
            writeAPFDInfo();
            this.lastExecutedClass = testName;
        }
    }

    private void setExecutionOrder(String testName)
    {
        for (FaultRevealingTest test : this.faultRevealingTests)
        {
            if (test.getTestName().equals(testName))
            {
                test.setExecutionOrder(this.executedTests);
            }
        }
    }

    private void writeAPFDInfo()
    {
        List<String> lines = new LinkedList<String>();
        lines.add(Integer.toString(this.executedTests));
        lines.add(Integer.toString(this.faultsAmount));
        for (FaultRevealingTest test : this.faultRevealingTests)
        {
            lines.add(Integer.toString(test.getExecutionOrder()));
        }
        Path file = Paths.get("APFDInfo");
        try
        {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException ex)
        {
//            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readAPFDInfo()
    {
        Path file = Paths.get("APFDInfo");
        List<String> fileLines = new LinkedList<String>();
        try
        {
            fileLines = Files.readAllLines(file, Charset.forName("UTF-8"));
            if (fileLines.size() >= 2 + faultRevealingTests.size())
            {
                this.executedTests = Integer.valueOf(fileLines.get(0));
                this.faultsAmount = Integer.valueOf(fileLines.get(1));
                int index = 2;
                for (FaultRevealingTest test : this.faultRevealingTests)
                {
                    test.setExecutionOrder(Integer.valueOf(fileLines.get(index++)));
                }
            }
        } catch (IOException ex)
        {
//            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ExecutionData calculateAPFD()
    {
        ClassAPFDListener listener = new ClassAPFDListener();
        listener.readAPFDInfo();
        double n = listener.executedTests;
        double m = listener.faultsAmount;
        double APFD = 1 - (sumOrders(listener.faultRevealingTests) / (n * m)) + (1 / (2 * n));
        double optimalAPFD = getOptimalAPFD(n, m, listener.faultRevealingTests);
        System.out.println("Optimal APFD=" + optimalAPFD);
        System.out.println("Achieved APFD=" + APFD);
        return getExecutionData(listener, APFD, optimalAPFD);
    }

    private static ExecutionData getExecutionData(ClassAPFDListener listener, double APFD, double optimalAPFD)
    {
        ExecutionData data = new ExecutionData();
        data.setAmountExecutedTests(listener.executedTests);
        data.setSeededFaultsAmount(listener.faultsAmount);
        data.setAPFD(APFD);
        data.setOptimalAPFD(optimalAPFD);
        return data;
    }

    private static double getOptimalAPFD(double amountOfTests, double amountOfFaults, List<FaultRevealingTest> tests)
    {
        double APFD = 0;
        APFD = 1 - (sumOrders(getOptimalRun(tests)) / (amountOfTests * amountOfFaults)) + (1 / (2 * amountOfTests));
        return APFD;
    }

    //given a list of tests that reveal faults, count the frequency of each test and order tests according to this frequency
    private static List<FaultRevealingTest> getOptimalRun(List<FaultRevealingTest> list)
    {
        HashMap<String, Integer> frequency = getTestsFrequency(list);
        List<FaultRevealingTest> orderedTests = new LinkedList<FaultRevealingTest>();
        int order = 1;
        while (!frequency.isEmpty())
        {
            FaultRevealingTest mostFrequentTest = getMostFrequentTest(frequency);
            if (!mostFrequentTest.getTestName().equals(""))
            {
                mostFrequentTest.setExecutionOrder(order++);
                orderedTests.add(mostFrequentTest);
                frequency.remove(mostFrequentTest.getTestName());
            }
        }
        for (FaultRevealingTest test : list)
        {
            for (FaultRevealingTest test2 : orderedTests)
            {
                if (test2.getTestName().equals(test.getTestName()))
                {
                    test.setExecutionOrder(test2.getExecutionOrder());
                    break;
                }
            }
        }
        return list;
    }

    private static HashMap<String, Integer> getTestsFrequency(List<FaultRevealingTest> tests)
    {

        HashMap<String, Integer> frequency = new HashMap<String, Integer>();

        for (FaultRevealingTest occurrency : tests)
        {
            Integer test = frequency.get(occurrency.getTestName());
            if (test != null)
            {
                frequency.put(occurrency.getTestName(), test + 1);
            } else
            {
                frequency.put(occurrency.getTestName(), 1);
            }
        }
        return frequency;
    }

    private static FaultRevealingTest getMostFrequentTest(HashMap<String, Integer> tests)
    {
        int biggerFrequency = Integer.MIN_VALUE;
        String biggerFrequencyKey = "";
        for (String test : tests.keySet())
        {
            int testFrequency = tests.get(test);
            if (testFrequency > biggerFrequency)
            {
                biggerFrequencyKey = test;
                biggerFrequency = testFrequency;
            }
        }
        return new FaultRevealingTest(biggerFrequencyKey, biggerFrequency);
    }

    private static int sumOrders(List<FaultRevealingTest> list)
    {
        int sum = 0;
        for (FaultRevealingTest value : list)
        {
            sum += value.getExecutionOrder();
        }
        return sum;
    }
}
