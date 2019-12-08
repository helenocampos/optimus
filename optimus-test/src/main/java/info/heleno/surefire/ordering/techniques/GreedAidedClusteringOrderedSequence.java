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

import info.heleno.executiontraceanalyzer.ExecutionTraceAnalyzer;
import info.heleno.surefire.api.AdditionalOrderer;
import info.heleno.testing.AbstractTest;
import info.heleno.surefire.ordering.Strategy;
import info.heleno.surefire.ordering.TestsSorter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 *
 * @author helenocampos
 */
public class GreedAidedClusteringOrderedSequence extends AdditionalOrderer<AbstractTest>
{

    private ExecutionTraceAnalyzer analyzer;
//    private CoverageAnalyzer coverageAnalyzer;
    private Integer clustersAmount;
    private List<List<AbstractTest>> testCaseClusters;
    private TestsSorter sorter;
    private String testGranularity;
    private int currentClusterIndex;

    public GreedAidedClusteringOrderedSequence()
    {
        analyzer = new ExecutionTraceAnalyzer();
        testCaseClusters = new LinkedList<>();
        sorter = new TestsSorter(null);
        testGranularity = "class";
//        coverageAnalyzer = new CoverageAnalyzer();
        Properties properties = System.getProperties();
        String clustersAmountString = properties.getProperty("clustersAmount");
        if (clustersAmountString != null && !clustersAmountString.equals(""))
        {
            clustersAmount = Integer.valueOf(clustersAmountString);
        }
    }

    @Override
    public String getStrategy()
    {
        return Strategy.ADDITIONAL.getName();
    }

    @Override
    public AbstractTest getNextTest(List<AbstractTest> tests, List<AbstractTest> alreadyOrderedTests)
    {
        if (alreadyOrderedTests.isEmpty())
        { //first call, setup clusters
            setupTestGranularity(tests);
            initializeClusters(tests);

            if (clustersAmount != null)
            {
                while (testCaseClusters.size() > clustersAmount && clustersAmount>1)
                {
                    Pair<List<AbstractTest>,List<AbstractTest>> minimumDistanceClusters = getClustersWithMinimumDistance();
                    testCaseClusters.remove(minimumDistanceClusters.getLeft());
                    testCaseClusters.remove(minimumDistanceClusters.getRight());
                    testCaseClusters.add(mergeClusters(minimumDistanceClusters.getLeft(), minimumDistanceClusters.getRight()));
                }
            }
            prioritizeIndividualClusters();
        } 
         // iteratively selects one test case from each cluster
         // select the first test case from cluster with current index
         if(currentClusterIndex>testCaseClusters.size()-1){
             currentClusterIndex = 0;
         }
         List<AbstractTest> currentCluster = testCaseClusters.get(currentClusterIndex++);
         AbstractTest selectedTestCase = currentCluster.remove(0);
         if(currentCluster.isEmpty()){
             testCaseClusters.remove(currentCluster);
             currentClusterIndex--;
         }
         return selectedTestCase;
    }
    
    private void setupTestGranularity(List<AbstractTest> tests){
        if(tests!=null && tests.size()>0){
            this.testGranularity = tests.get(0).getTestGranularity().getName();
        }
    }

    private void prioritizeIndividualClusters(){
        List<List<AbstractTest>> prioritizedClusters = new LinkedList<>();
        for(List<AbstractTest> cluster: testCaseClusters){
            prioritizedClusters.add(applyAdditionalPrioritization(cluster));
        }
        testCaseClusters = prioritizedClusters;
    }
    
    private List<AbstractTest> applyAdditionalPrioritization(List<AbstractTest> tests){
        return sorter.sort(tests, PrioritizationTechniques.ADDITIONAL_STATEMENT_COVERAGE.getName(), testGranularity);
    }
    
    //  creates 1 cluster for each test case (tests parameter)
    private void initializeClusters(List<AbstractTest> tests)
    {
        for (AbstractTest test : tests)
        {
            List<AbstractTest> cluster = new LinkedList<>();
            cluster.add(test);
            testCaseClusters.add(cluster);
        }
    }

    private int distanceBetweenClusters(List<AbstractTest> cluster1, List<AbstractTest> cluster2)
    {
        LevenshteinDistance distanceCalculator = new LevenshteinDistance();
        String orderedSequenceCluster1 = analyzer.getTestOrderedSequence(cluster1);
        String orderedSequenceCluster2 = analyzer.getTestOrderedSequence(cluster2);
        int distance = 0;
        if (!orderedSequenceCluster1.equals(orderedSequenceCluster2))
        {
            distance = distanceCalculator.apply(orderedSequenceCluster1, orderedSequenceCluster2);
        }
        return distance;
    }
    
    private Pair<List<AbstractTest>,List<AbstractTest>> getClustersWithMinimumDistance(){
        Pair<List<AbstractTest>,List<AbstractTest>> pair = null;
        int minimumDistance = Integer.MAX_VALUE;
        for(List<AbstractTest> cluster1: testCaseClusters){
            for(List<AbstractTest> cluster2: testCaseClusters){
                if(cluster1!=cluster2){
                    int distance = distanceBetweenClusters(cluster1, cluster2);
                    if(distance<minimumDistance){
                        pair = Pair.of(cluster1, cluster2);
                        minimumDistance = distance;
                    }
                }
            }
        }
        return pair;
    }
    
    private List<AbstractTest> mergeClusters(List<AbstractTest> cluster1, List<AbstractTest> cluster2){
        List<AbstractTest> newCluster = new LinkedList<>();
        newCluster.addAll(cluster1);
        newCluster.addAll(cluster2);
        return newCluster;
    }

}
