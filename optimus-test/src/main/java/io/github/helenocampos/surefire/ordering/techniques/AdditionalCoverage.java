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

import io.github.helenocampos.extractor.model.CoverageGranularity;
import io.github.helenocampos.testing.AbstractTest;
import io.github.helenocampos.surefire.analyzer.coverage.CoverageAnalyzer;
import io.github.helenocampos.surefire.api.AdditionalCoverageOrderer;
import io.github.helenocampos.surefire.ordering.Strategy;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public abstract class AdditionalCoverage extends AdditionalCoverageOrderer<AbstractTest>
{

    private CoverageAnalyzer analyzer;

    public AdditionalCoverage()
    {
        this.analyzer = new CoverageAnalyzer();
    }

    @Override
    public String getStrategy()
    {
        return Strategy.ADDITIONAL.getName();
    }
    
    public abstract CoverageGranularity getCoverageGranularity();

    @Override
    public AbstractTest getNextTest(List<AbstractTest> tests)
    {
        AbstractTest highestCoverageTest = null;
        float highestCoverageScore = Integer.MIN_VALUE;
        for (AbstractTest test : tests)
        {
            float testScore = analyzer.getAdditionalTestCoverage(test, getCoverageGranularity(), getCurrentCoverageSet());
            if (testScore > highestCoverageScore)
            {
                highestCoverageTest = test;
                highestCoverageScore = testScore;
            }
        }
        if (highestCoverageScore <= 0)
        {
            // no tests in the list add any coverage to current covered set
            //in this case, we reset coverage data and start again
            resetCurrentCoverage();
            return getNextTest(tests);
        }

        return highestCoverageTest;
    }

}
