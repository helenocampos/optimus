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

import info.heleno.extractor.model.Coverage;
import info.heleno.extractor.model.CoverageGranularity;
import info.heleno.extractor.model.ModificationsGranularity;
import info.heleno.optimusmodificationsanalyzer.ModificationsAnalyzer;
import info.heleno.testing.AbstractTest;
import info.heleno.surefire.analyzer.coverage.CoverageAnalyzer;
import info.heleno.surefire.api.AdditionalCoverageOrderer;
import info.heleno.surefire.ordering.Strategy;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author helenocampos
 */
public abstract class AdditionalDiffCoverage extends AdditionalCoverageOrderer<AbstractTest>
{

    private CoverageAnalyzer analyzer;
    private Set<String> modifiedElements;

    public AdditionalDiffCoverage()
    {
        this.analyzer = new CoverageAnalyzer();
        ModificationsAnalyzer diffAnalyzer = new ModificationsAnalyzer(this.analyzer.getProjectData());
        this.modifiedElements = diffAnalyzer.getModifiedElements(getModificationsGranularity());
    }

    @Override
    public String getStrategy()
    {
        return Strategy.ADDITIONAL.getName();
    }

    public abstract ModificationsGranularity getModificationsGranularity();

    @Override
    public AbstractTest getNextTest(List<AbstractTest> tests)
    {
        if(this.modifiedElements.isEmpty()){
            return getNextAdditionalTest(tests);
        }
        
        List<AbstractTest> candidateTests = new LinkedList<>();
        AbstractTest highestCoverageTest = null;
        float highestCoverageScore = Integer.MIN_VALUE;
        Coverage alreadyCoveredCode = analyzer.getCoverageFromTests(getCurrentCoverageSet());
        for (AbstractTest test : tests)
        {
            float testScore = analyzer.getAdditionalDiffTestCoverage(test, getModificationsGranularity(), alreadyCoveredCode, this.modifiedElements);
            if (testScore > highestCoverageScore)
            {
                highestCoverageTest = test;
                highestCoverageScore = testScore;
                candidateTests = new LinkedList<>();
            }
            if (testScore == highestCoverageScore)
            {
                candidateTests.add(test);
            }
        }
        if (highestCoverageScore <= 0)
        {
            // no tests in the list add any coverage to current covered set
            //in this case, default additional coverage is applied
            return getNextAdditionalTest(tests);
        } else
        {
            if (candidateTests.size() > 1)
            {
                highestCoverageTest = resolveTies(candidateTests);
            }
        }
        return highestCoverageTest;
    }

    public AbstractTest getNextAdditionalTest(List<AbstractTest> tests)
    {
        List<AbstractTest> candidateTests = new LinkedList<>();
        AbstractTest highestCoverageTest = null;
        float highestCoverageScore = Integer.MIN_VALUE;
        Coverage alreadyCoveredCode = analyzer.getCoverageFromTests(getCurrentCoverageSet());
        for (AbstractTest test : tests)
        {
            float testScore = analyzer.getAdditionalTestCoverage(test, CoverageGranularity.METHOD, alreadyCoveredCode);
            if (testScore > highestCoverageScore)
            {
                highestCoverageTest = test;
                highestCoverageScore = testScore;
                candidateTests = new LinkedList<>();
            }
            if (testScore == highestCoverageScore  && testScore!=0)
            {
                candidateTests.add(test);
            }
        }
        if (highestCoverageScore <= 0)
        {
            // no tests in the list add any coverage to current covered set
            //in this case, we reset coverage data and start again
            if (!isRecursiveLocked())
            {
                resetCurrentCoverage();
                setRecursiveLocked(true);
                return getNextAdditionalTest(tests);
            } else
            {
                highestCoverageTest = resolveTies(tests);
            }
        } else
        {
            if (candidateTests.size() > 1)
            {
                highestCoverageTest = resolveTies(candidateTests);
            }
        }

        return highestCoverageTest;
    }

}
