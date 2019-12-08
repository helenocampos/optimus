/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire.ordering.techniques;

import info.heleno.extractor.model.Coverage;
import info.heleno.extractor.model.CoverageGranularity;
import info.heleno.surefire.analyzer.coverage.CoverageAnalyzer;
import info.heleno.surefire.api.AdditionalOrderer;
import info.heleno.surefire.ordering.Strategy;
import info.heleno.surefire.util.SimilarityMeasures;
import info.heleno.testing.AbstractTest;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author helenocampos //reference for the algorithms used is the original
 * paper Adaptive Random Test Case Prioritization by Jiang et. al 2009
 */
public abstract class ARTMod extends AdditionalOrderer<AbstractTest> {

    public enum SelectionFunction {
        MAXMIN, MAXAVG, MAXMAX;
    }

    protected CoverageAnalyzer coverageAnalyzer;
        
    public ARTMod() {
        coverageAnalyzer = new CoverageAnalyzer();
    }

    public abstract CoverageGranularity getCoverageGranularity();

    public abstract SelectionFunction getSelectionFunction();

    @Override
    public String getStrategy() {
        return Strategy.ADDITIONAL.getName();
    }

    @Override
    public AbstractTest getNextTest(List<AbstractTest> tests, List<AbstractTest> alreadyOrderedTests) {
        List<AbstractTest> candidateSet = getCandidateSet(tests);
        AbstractTest nextTest = selectOneTestCase(candidateSet, alreadyOrderedTests);
        if (nextTest == null) {
            nextTest = tests.get(0);
        }
        return nextTest;
    }

    protected List<AbstractTest> getCandidateSet(List<AbstractTest> notYetSelectedTests) {
        List<AbstractTest> candidateSet = new LinkedList<>();
        List<AbstractTest> remainingTests = new LinkedList<>();
        remainingTests.addAll(notYetSelectedTests);
        Random randomizer = new Random();
        if (!notYetSelectedTests.isEmpty()) {
            float additionalCoverageScore = -1;
            while (additionalCoverageScore != 0 && remainingTests.size() > 0) {
                AbstractTest randomTest = remainingTests.get(randomizer.nextInt(remainingTests.size()));
                Coverage candidateSetCoveredCode = this.coverageAnalyzer.getCoverageFromTests(candidateSet);
                additionalCoverageScore = this.coverageAnalyzer.getAdditionalTestCoverage(randomTest, getCoverageGranularity(), candidateSetCoveredCode);
                if (additionalCoverageScore != 0) {
                    candidateSet.add(randomTest);
                    remainingTests.remove(randomTest);
                }
            }
        }
        return candidateSet;
    }

    protected AbstractTest selectOneTestCase(List<AbstractTest> candidateSet, List<AbstractTest> alreadySelectedTests) {
        return getMaxDistanceTest(candidateSet, alreadySelectedTests);
    }

    private double getTestDistanceFromSet(AbstractTest test, List<AbstractTest> testSet) {
        switch (getSelectionFunction()) {
            case MAXMIN:
                return getMinJaccardDistance(test, testSet);
            case MAXMAX:
                return getMaxJaccardDistance(test, testSet);
            case MAXAVG:
                return getAvgJaccardDistance(test, testSet);
            default:
                return 0;
        }
    }

    protected AbstractTest getMaxDistanceTest(List<AbstractTest> candidateSet, List<AbstractTest> alreadySelectedTests) {
        // select the test with the greatest minimum distance to the already selected tests
        AbstractTest maxDistanceTest = null;
        Double maxDistanceFromOrderedTests = Double.NEGATIVE_INFINITY;
        for (AbstractTest test : candidateSet) {
            Double distance = getTestDistanceFromSet(test, candidateSet);
            if (distance > maxDistanceFromOrderedTests) {
                maxDistanceTest = test;
                maxDistanceFromOrderedTests = distance;
            }
        }

        return maxDistanceTest;
    }

    private double getMinJaccardDistance(AbstractTest test, List<AbstractTest> testSet) {

        Double minimumDistance = Double.POSITIVE_INFINITY;
        for (AbstractTest orderedTest : testSet) {
            double distance = getTestsDistance(test, orderedTest);
            if (distance < minimumDistance) {
                minimumDistance = distance;
            }
        }
        if (testSet.isEmpty()) {
            return getTestsDistance(test, null);
        }

        return minimumDistance;
    }

    private double getAvgJaccardDistance(AbstractTest test, List<AbstractTest> testSet) {
        Double acummulatedDistance = 0.0;
        for (AbstractTest orderedTest : testSet) {
            acummulatedDistance += getTestsDistance(test, orderedTest);
        }
        if (testSet.isEmpty()) {
            return getTestsDistance(test, null);
        }

        return acummulatedDistance;
    }

    private double getMaxJaccardDistance(AbstractTest test, List<AbstractTest> testSet) {

        Double maximumDistance = Double.NEGATIVE_INFINITY;
        for (AbstractTest orderedTest : testSet) {
            double distance = getTestsDistance(test, orderedTest);
            if (distance > maximumDistance) {
                maximumDistance = distance;
            }
        }
        if (testSet.isEmpty()) {
            return getTestsDistance(test, null);
        }

        return maximumDistance;
    }

    private double getTestsDistance(AbstractTest test1, AbstractTest test2) {
        String testCoverage = coverageAnalyzer.getTestCoverageString(test1, getCoverageGranularity());
        String orderedTestCoverage = "";
        if (test2 != null) {
            orderedTestCoverage = coverageAnalyzer.getTestCoverageString(test2, getCoverageGranularity());
        }
        return SimilarityMeasures.getJaccardDistance(testCoverage, orderedTestCoverage);
    }
}
