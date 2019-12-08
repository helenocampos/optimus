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
package info.heleno.surefire.ordering.techniques;

import info.heleno.surefire.analyzer.coverage.CoverageAnalyzer;
import info.heleno.surefire.api.AdditionalOrderer;
import info.heleno.surefire.ordering.Strategy;
import info.heleno.executiontraceanalyzer.ExecutionTraceAnalyzer;
import info.heleno.extractor.model.CoverageGranularity;
import info.heleno.testing.AbstractTest;
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
    private TestsDistances testsDistances;
    
    public FarthestFirstOrderedSequence()
    {
        analyzer = new ExecutionTraceAnalyzer();
        coverageAnalyzer = new CoverageAnalyzer();
        this.testsDistances = new TestsDistances();
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
            initializeTestsDistances(tests);
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
                    int distance = testsDistances.getDistance(test.getQualifiedName(), orderedTest.getQualifiedName()).intValue();
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
            float score =coverageAnalyzer.getTotalTestCoverage(test, CoverageGranularity.STATEMENT);
            if(score > biggestCoverage){
                biggestCoverage = score;
                biggestCoverageTest = test;
            }
        }
        return biggestCoverageTest;
    }
    
     private void initializeTestsDistances(List<AbstractTest> tests) {
        LevenshteinDistance distanceCalculator = new LevenshteinDistance();
        for (AbstractTest source : tests) {
            String orderedSequenceSource = analyzer.getTestOrderedSequence(source);
            for (AbstractTest target : tests) {
                if (!source.equals(target)) {
                    String orderedSequenceTarget = analyzer.getTestOrderedSequence(target);
                    Integer distance = distanceCalculator.apply(orderedSequenceSource, orderedSequenceTarget);
                    testsDistances.addDistance(source.getQualifiedName(), target.getQualifiedName(), distance.doubleValue());
                    testsDistances.addDistance(target.getQualifiedName(), source.getQualifiedName(), distance.doubleValue());
                } else {
                    testsDistances.addDistance(source.getQualifiedName(), target.getQualifiedName(), new Double(0));
                    testsDistances.addDistance(target.getQualifiedName(), source.getQualifiedName(), new Double(0));
                }
            }
        }
    }
}
