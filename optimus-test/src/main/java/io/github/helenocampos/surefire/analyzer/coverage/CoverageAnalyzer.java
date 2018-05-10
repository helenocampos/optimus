/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.analyzer.coverage;

import io.github.helenocampos.extractor.model.TestMethod;
import io.github.helenocampos.extractor.model.Coverage;
import io.github.helenocampos.extractor.model.CoverageGranularity;
import io.github.helenocampos.extractor.model.JavaTestClass;
import io.github.helenocampos.extractor.model.ProjectData;
import io.github.helenocampos.testing.AbstractTest;
import io.github.helenocampos.testing.Granularity;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class CoverageAnalyzer
{

    private ProjectData projectData;

    public CoverageAnalyzer()
    {
        this.projectData = ProjectData.getProjectDataFromFile();
    }

    public float getTotalTestCoverage(AbstractTest test, CoverageGranularity granularity)
    {
        float score = 0;

        if (this.projectData != null)
        {
            JavaTestClass testClass = this.projectData.getTestClassByName(test.getTestClass().getName());
            if (testClass != null)
            {
                if (test.getTestGranularity().equals(Granularity.METHOD))
                {
                    TestMethod method = testClass.getMethodByName(test.getTestName());
                    score = getTestMethodTotalCoverage(method, granularity);
                } else if (test.getTestGranularity().equals(Granularity.CLASS))
                {
                    for (TestMethod method : testClass.getMethods().values())
                    {
                        score += getTestMethodTotalCoverage(method, granularity);
                    }
                }
            }
        }

        return score;
    }

    public float getAdditionalTestCoverage(AbstractTest test, CoverageGranularity granularity, List<AbstractTest> currentCandidateTests)
    {
        float score = 0;

        if (this.projectData != null)
        {
            JavaTestClass testClass = this.projectData.getTestClassByName(test.getTestClass().getName());
            if (testClass != null)
            {
                Coverage coveredCode = getCoverageFromTests(currentCandidateTests);
                if (test.getTestGranularity().equals(Granularity.METHOD))
                {
                    TestMethod method = testClass.getMethodByName(test.getTestName());
                    score = getTestMethodAdditionalCoverage(method, granularity, coveredCode);
                } else if (test.getTestGranularity().equals(Granularity.CLASS))
                {
                    for (TestMethod method : testClass.getMethods().values())
                    {
                        score += getTestMethodAdditionalCoverage(method, granularity, coveredCode);
                    }
                }
            }
        }

        return score;
    }

    private float getTestMethodTotalCoverage(TestMethod method, CoverageGranularity granularity)
    {
        float score = 0;
        if (method != null)
        {
            if (method.getCoverage() != null)
            {
                for (String data : method.getCoverage().getCoverage(granularity).values())
                {
                    for (int x = 0; x < data.length(); x++)
                    {
                        if (data.charAt(x) == '1')
                        {
                            score++;
                        }
                    }
                }
            }

        }
        return score;
    }

    private float getTestMethodAdditionalCoverage(TestMethod method, CoverageGranularity granularity, Coverage candidatesCoveredCode)
    {
        float additionalScore = 0;
        if (method != null)
        {
            if (method.getCoverage() != null)
            {
                Coverage coverage = method.getCoverage();
                for (String className : coverage.getCoverage(granularity).keySet())
                {
                    String coveredElements = coverage.getCoverage(granularity).get(className); //statements really covered by the test contained in className
                    String candidatesCoveredElements = candidatesCoveredCode.getCoverage(granularity).get(className);

                    if (candidatesCoveredElements == null)
                    {
                        candidatesCoveredElements = getClearedCoverage(coveredElements.length());
                    }

                    for (int x = 0; x < coveredElements.length(); x++)
                    {
                        if (coveredElements.charAt(x) == '1')
                        {
                            if (candidatesCoveredElements.charAt(x) == '0')
                            {
                                //element not yet covered by candidates tests, compute one Score
                                candidatesCoveredElements = replaceCoverage(x, candidatesCoveredElements, '1');
                                additionalScore++;

                            }
                        }
                    }
                    candidatesCoveredCode.getCoverage(granularity).put(className, candidatesCoveredElements);
                }
            }
        }
        return additionalScore;
    }

    private String getClearedCoverage(int size)
    {
        String clearedCoverage = "";
        for (int i = 0; i < size; i++)
        {
            clearedCoverage += "0";
        }
        return clearedCoverage;
    }

    private String replaceCoverage(int index, String coverageString, char newValue)
    {
        char[] stringChars = coverageString.toCharArray();
        stringChars[index] = newValue;
        return String.valueOf(stringChars);
    }

    private Coverage getCoverageFromTests(List<AbstractTest> tests)
    {
        Coverage testsCoverage = new Coverage();
        for (AbstractTest test : tests)
        {
            Coverage testCoverage = new Coverage();
            testCoverage.setStatements(getTestCoverage(test, CoverageGranularity.STATEMENT));
            testCoverage.setMethods(getTestCoverage(test, CoverageGranularity.METHOD));
            testCoverage.setBranches(getTestCoverage(test, CoverageGranularity.BRANCH));
            testsCoverage = Coverage.merge(testsCoverage, testCoverage);
        }
        return testsCoverage;
    }

    public LinkedHashMap<String, String> getTestCoverage(AbstractTest test, CoverageGranularity granularity)
    {
        JavaTestClass testClass = this.projectData.getTestClassByName(test.getTestClass().getName());
        LinkedHashMap<String, String> testCoverage = new LinkedHashMap<>();
        if (testClass != null)
        {
            if (test.getTestGranularity().equals(Granularity.METHOD))
            {
                TestMethod method = testClass.getMethodByName(test.getTestName());
                if (method != null)
                {
                    testCoverage.putAll(method.getCoverage().getCoverage(granularity));
                }
            } else if (test.getTestGranularity().equals(Granularity.CLASS))
            {
                for (TestMethod method : testClass.getMethods().values())
                {
                    LinkedHashMap<String,String> methodCoverage = method.getCoverage().getCoverage(granularity);
                    testCoverage = Coverage.merge(methodCoverage, testCoverage);
                }
            }
        }
        return testCoverage;
    }

    public String getTestCoverageString(AbstractTest test, CoverageGranularity coverageGranularity)
    {
        String coverageString = "";
        JavaTestClass testClass = projectData.getTestClassByName(test.getTestClass().getCanonicalName());
        if (testClass != null)
        {
            if (test.getTestGranularity().equals(Granularity.CLASS))
            {
                LinkedHashMap<String, String> testClassCoverage = new LinkedHashMap<>();
                for (TestMethod method : testClass.getMethods().values())
                {
                    LinkedHashMap<String,String> testMethodCoverage = method.getCoverage().getCoverage(coverageGranularity);
                    testClassCoverage = Coverage.merge(testMethodCoverage, testClassCoverage);
                }
                coverageString = Coverage.getCoverageString(coverageGranularity, testClassCoverage);
            } else
            {
                TestMethod method = testClass.getMethodByName(test.getTestName());
                if (method != null && method.getCoverage() != null)
                {
                    coverageString = method.getCoverage().getCoverageString(coverageGranularity);
                }
            }
        }

        return coverageString;
    }
}
