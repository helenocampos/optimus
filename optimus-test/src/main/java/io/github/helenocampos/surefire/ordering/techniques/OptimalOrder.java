/*
 * Copyright 2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.helenocampos.surefire.ordering.techniques;

import io.github.helenocampos.testing.AbstractTest;
import io.github.helenocampos.surefire.ordering.Strategy;
import io.github.helenocampos.surefire.api.DefaultOrderer;
import io.github.helenocampos.surefire.junit4.FaultRevealingTest;
import io.github.helenocampos.testing.TestGranularity;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class OptimalOrder extends DefaultOrderer<AbstractTest>
{

    private List<FaultRevealingTest> faultRevealingTests;
    private boolean firstExecution = true;

    public OptimalOrder()
    {
        this.faultRevealingTests = new LinkedList<FaultRevealingTest>();
    }

    public int compare(AbstractTest o1, AbstractTest o2)
    {
        if (firstExecution)
        {
            readTestsFile(o1.getTestGranularity());
            orderFaultRevealingTests();
            firstExecution = false;
        }
        int o1TestOrder = getTestOrder(o1.getQualifiedName());
        int o2TestOrder = getTestOrder(o2.getQualifiedName());

        return Integer.compare(o2TestOrder, o1TestOrder);
    }

    private int getTestOrder(String testName)
    {
        for (FaultRevealingTest faultRevealingTest : this.faultRevealingTests)
        {
            if (faultRevealingTest.getTestName().equals(testName))
            {
                return faultRevealingTest.getExecutionOrder();
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public String getStrategy()
    {
        return Strategy.DEFAULT.getName();
    }

    private void readTestsFile(TestGranularity testGranularity)
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
            if (testGranularity.equals(TestGranularity.CLASS))
            {
                if (line.contains("."))
                {
                    String className = line.substring(0, line.lastIndexOf("."));
                    this.faultRevealingTests.add(new FaultRevealingTest(className, Integer.MAX_VALUE));
                }
            } else
            {
                this.faultRevealingTests.add(new FaultRevealingTest(line, Integer.MAX_VALUE));
            }

        }
    }

    private void orderFaultRevealingTests()
    {
        HashMap<FaultRevealingTest, Integer> frequency = getTestsFrequency(faultRevealingTests);
        int order = 1;
        while (!frequency.isEmpty())
        {
            FaultRevealingTest mostFrequentTest = getMostFrequentTest(frequency);
            if (mostFrequentTest != null)
            {
                mostFrequentTest.setExecutionOrder(order++);
                frequency.remove(mostFrequentTest);
            }
        }
    }

    private HashMap<FaultRevealingTest, Integer> getTestsFrequency(List<FaultRevealingTest> tests)
    {

        HashMap<FaultRevealingTest, Integer> frequency = new HashMap<FaultRevealingTest, Integer>();

        for (FaultRevealingTest occurrency : tests)
        {
            Integer test = frequency.get(occurrency);
            if (test != null)
            {
                frequency.put(occurrency, test + 1);
            } else
            {
                frequency.put(occurrency, 1);
            }
        }
        return frequency;
    }

    private FaultRevealingTest getMostFrequentTest(HashMap<FaultRevealingTest, Integer> tests)
    {
        int biggerFrequency = Integer.MIN_VALUE;
        FaultRevealingTest mostFrequentTest = null;
        for (FaultRevealingTest test : tests.keySet())
        {
            int testFrequency = tests.get(test);
            if (testFrequency > biggerFrequency)
            {
                biggerFrequency = testFrequency;
                mostFrequentTest = test;
            }
        }
        return mostFrequentTest;
    }
}
