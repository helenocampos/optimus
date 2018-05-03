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

import io.github.helenocampos.executiontraceanalyzer.ExecutionTraceAnalyzer;
import io.github.helenocampos.surefire.analyzer.coverage.CoverageAnalyzer;
import io.github.helenocampos.surefire.api.AdditionalOrderer;
import io.github.helenocampos.testing.AbstractTest;
import io.github.helenocampos.surefire.ordering.Strategy;
import java.util.List;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 *
 * @author helenocampos
 */
public class FarthestFirstOrderedSequence extends AdditionalOrderer<AbstractTest>
{

    private ExecutionTraceAnalyzer analyzer;
    private CoverageAnalyzer coverageAnalyzer;
    
    public FarthestFirstOrderedSequence()
    {
        analyzer = new ExecutionTraceAnalyzer();
        coverageAnalyzer = new CoverageAnalyzer();
    }

    @Override
    public String getStrategy()
    {
        return Strategy.ADDITIONAL.getName();
    }

    @Override
    public AbstractTest getNextTest(List<AbstractTest> tests, List<AbstractTest> alreadyOrderedTests)
    {
        if(alreadyOrderedTests.isEmpty()){ //select the test with greatest coverage
            return getHighestCoverageTest(tests);
        }else{
            //calculate edit distance for each test to the already selected tests
            //choose the minimum distance as representative of the already selected tests and use this distance
            // select the test with the greatest minimum distance to the already selected tests
            AbstractTest maxDistanceTest = null;
            int maxDistanceFromOrderedTests = Integer.MIN_VALUE;
            for(AbstractTest test: tests){
                int minimumDistance = Integer.MAX_VALUE;
                for(AbstractTest orderedTest: alreadyOrderedTests){
                    LevenshteinDistance distanceCalculator = new LevenshteinDistance();
                    String orderedSequenceTest = analyzer.getTestOrderedSequence(test);
                    String orderedSequenceSelectedTest = analyzer.getTestOrderedSequence(orderedTest);
                    int distance = 0;
                    if(!orderedSequenceTest.equals(orderedSequenceSelectedTest)){
                        distance = distanceCalculator.apply(orderedSequenceTest, orderedSequenceSelectedTest);
                    }
                    
                    if(distance<minimumDistance){
                        minimumDistance = distance;
                    }
                }
                if(minimumDistance>maxDistanceFromOrderedTests){
                    maxDistanceTest = test;
                    maxDistanceFromOrderedTests = minimumDistance;
                }
            }
            return maxDistanceTest;
        }
    }
    
    private AbstractTest getHighestCoverageTest(List<AbstractTest> tests){
        AbstractTest biggestCoverageTest = null;
        float biggestCoverage = Float.NEGATIVE_INFINITY;
        for(AbstractTest test: tests){
            float score =coverageAnalyzer.getTestScore(test, "statement", "total");
            if(score > biggestCoverage){
                biggestCoverage = score;
                biggestCoverageTest = test;
            }
        }
        return biggestCoverageTest;
    }
}
