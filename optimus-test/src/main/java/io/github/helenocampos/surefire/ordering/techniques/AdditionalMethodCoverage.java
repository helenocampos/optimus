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
import io.github.helenocampos.surefire.analyzer.coverage.CoverageAnalyzer;
import io.github.helenocampos.surefire.api.AdditionalOrderer;
import io.github.helenocampos.surefire.ordering.Strategy;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class AdditionalMethodCoverage extends AdditionalOrderer<AbstractTest>
{

    private CoverageAnalyzer analyzer;

    //prevent getHighestCoverageTest from getting into infite loop when no tests adds coverage to test set
    private boolean loopLock;

    public AdditionalMethodCoverage()
    {
        this.analyzer = new CoverageAnalyzer();
        this.setCoveredCode(analyzer.getCoveredMethods());
        loopLock = false;
    }

    @Override
    public String getStrategy()
    {
        return Strategy.ADDITIONAL.getName();
    }

    //considering already covered code, returns which test from the list has the highest coverage
    //coveredCode has methods that are already covered by selected tests
    @Override
    public AbstractTest getHighestCoverageTest(List<AbstractTest> tests)
    {
        AbstractTest highestCoverageTest = null;
        float highestCoverageScore = 0;

        //save initial state of covered methods before selecting the highest coverage test
        HashMap<String, boolean[]> coveredCode = CoverageAnalyzer.copyCoverageData(analyzer.getCoveredMethods());

        //initialize highest test coverage with already selected tests coverage
        HashMap<String, boolean[]> coveredCodeByHighestScoreTest = CoverageAnalyzer.copyCoverageData(analyzer.getCoveredMethods());

        for (AbstractTest test : tests)
        {
            float testScore = analyzer.getTestScore(test, "method", "additional");
            if (testScore > highestCoverageScore)
            {
                //found a new test with highest coverage
                coveredCodeByHighestScoreTest = new HashMap<String, boolean[]>();
                //merge temporary selected test with the saved state before selecting
                coveredCodeByHighestScoreTest.putAll(CoverageAnalyzer.copyCoverageData(coveredCode));
                coveredCodeByHighestScoreTest.putAll(analyzer.getTestCoverage(test, "method"));
                highestCoverageTest = test;
                highestCoverageScore = testScore;
            }

            // return coveredmethods state to saved state before selecting 
            analyzer.setCoveredMethods(CoverageAnalyzer.copyCoverageData(coveredCode));

        }
        if (highestCoverageTest != null)
        {
            //merge selected test coverage with previously selected tests coverage
            coveredCode.putAll(coveredCodeByHighestScoreTest);
            this.analyzer.setCoveredMethods(coveredCode);
            this.updateCoveredCode(coveredCode);
        } else if (!loopLock)
        { // no tests in the list add any coverage to already covered code by already selected tests
            //in this case, we reset coverage data and start again
            HashMap<String, boolean[]> clearMethodCoverage = this.analyzer.getClearMethodCoverage();
            this.setCoveredCode(clearMethodCoverage);
            this.analyzer.setCoveredMethods(clearMethodCoverage);
            loopLock = true;
            return getHighestCoverageTest(tests);
        } else
        {
            if (tests.size() > 0)
            {
                return tests.get(0);
            }

        }
        loopLock = false;
        return highestCoverageTest;
    }

}
