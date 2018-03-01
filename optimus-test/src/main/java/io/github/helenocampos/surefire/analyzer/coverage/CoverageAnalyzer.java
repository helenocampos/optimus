/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.analyzer.coverage;

import io.github.helenocampos.surefire.AbstractTest;
import io.github.helenocampos.surefire.api.Analyzer;
import io.github.helenocampos.surefire.extractor.model.ClassMethod;
import io.github.helenocampos.surefire.extractor.model.Coverage;
import io.github.helenocampos.surefire.extractor.model.JavaTestClass;
import io.github.helenocampos.surefire.extractor.model.ProjectData;
import io.github.helenocampos.surefire.ordering.Granularity;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author helenocampos
 */
public class CoverageAnalyzer implements Analyzer
{

    private HashMap<String, boolean[]> coveredStatements;
    private HashMap<String, boolean[]> coveredBranches;
    private HashMap<String, boolean[]> coveredMethods;
    private ProjectData projectData;

    public CoverageAnalyzer()
    {
        this.projectData = ProjectData.getProjectDataFromFile();
        initializeCoveredCode();
    }

    private void initializeCoveredCode()
    {
        this.setCoveredStatements(new HashMap<String, boolean[]>());
        this.setCoveredMethods(new HashMap<String, boolean[]>());
        this.setCoveredBranches(new HashMap<String, boolean[]>());
        if (this.projectData != null)
        {
            for (JavaTestClass testClass : projectData.getTests().values())
            {
                for (ClassMethod testMethod : testClass.getMethods().values())
                {
                    this.getCoveredStatements().putAll(testMethod.getCoverage().getStatements());
                    this.getCoveredMethods().putAll(testMethod.getCoverage().getMethods());
                    this.getCoveredBranches().putAll(testMethod.getCoverage().getBranches());
                }
            }

        }
        clearCoveredCode();
    }

    //set all coveredCode to false
    private void clearCoveredCode()
    {
        this.getCoveredStatements().putAll(getClearStatementCoverage());
        this.getCoveredMethods().putAll(getClearMethodCoverage());
        this.getCoveredBranches().putAll(getClearBranchCoverage());
    }

    public HashMap<String, boolean[]> getClearStatementCoverage()
    {
        HashMap<String, boolean[]> statementCoverage = new HashMap<String, boolean[]>();
        for (Map.Entry<String, boolean[]> coveredClass : this.getCoveredStatements().entrySet())
        {
            statementCoverage.put(coveredClass.getKey(), new boolean[coveredClass.getValue().length]);
        }
        return statementCoverage;
    }

    public HashMap<String, boolean[]> getClearMethodCoverage()
    {
        HashMap<String, boolean[]> methodCoverage = new HashMap<String, boolean[]>();
        for (Map.Entry<String, boolean[]> coveredClass : this.getCoveredMethods().entrySet())
        {
            methodCoverage.put(coveredClass.getKey(), new boolean[coveredClass.getValue().length]);
        }
        return methodCoverage;
    }

    public HashMap<String, boolean[]> getClearBranchCoverage()
    {
        HashMap<String, boolean[]> branchCoverage = new HashMap<String, boolean[]>();
        for (Map.Entry<String, boolean[]> coveredClass : this.getCoveredBranches().entrySet())
        {
            branchCoverage.put(coveredClass.getKey(), new boolean[coveredClass.getValue().length]);
        }
        return branchCoverage;
    }

    //argument 0 = coverage granularity (method, branch, statement)
    //argument 1 = coverage variant (total, additional)
    @Override
    public float getTestScore(AbstractTest test, String... arguments)
    {
        float score = 0;
        if (arguments.length == 2)
        {
            String granularity = arguments[0];
            String variant = arguments[1];
            score = getTestScore(test, granularity, variant);
        }
        return score;
    }

    private float getTestScore(AbstractTest test, String granularity, String variant)
    {
        float score = 0;

        if (this.projectData != null)
        {
            JavaTestClass testClass = this.projectData.getTestClassByName(test.getTestClass().getName());
            if (testClass != null)
            {
                if (test.getTestGranularity().equals(Granularity.METHOD))
                {
                    ClassMethod method = testClass.getMethodByName(test.getTestName());
                    score = getTestMethodScore(method, granularity, variant);
                } else if (test.getTestGranularity().equals(Granularity.CLASS))
                {
                    score = getTestClassScore(testClass, granularity, variant);
                }
            }
        }

        return score;
    }

    private float getTestMethodScore(ClassMethod method, String granularity, String variant)
    {
        float score = 0;

        if (method != null)
        {
            switch (variant)
            {
                case "total":
                    switch (granularity)
                    {
                        case "statement":
                            score = getTotalStatementCoverageScore(method.getCoverage());
                            break;
                        case "method":
                            score = getTotalMethodCoverageScore(method.getCoverage());
                            break;
                        case "branch":
                            score = getTotalBranchCoverageScore(method.getCoverage());
                            break;
                    }
                    break;
                case "additional":
                    switch (granularity)
                    {
                        case "statement":
                            score = getAdditionalStatementCoverageScore(method.getCoverage());
                            break;
                        case "method":
                            score = getAdditionalMethodCoverageScore(method.getCoverage());
                            break;
                        case "branch":
                            score = getAdditionalBranchCoverageScore(method.getCoverage());
                            break;
                    }
                    break;
            }
        }

        return score;
    }

    private float getTestClassScore(JavaTestClass test, String granularity, String variant)
    {
        float score = 0;
        if (test != null)
        {
            for (ClassMethod method : test.getMethods().values())
            {
                score += getTestMethodScore(method, granularity, variant);
            }
        }
        return score;
    }

    //compares current coveredCode (coveredCode attribute) with this test coverage to return notYetCoveredScore
    private float getAdditionalStatementCoverageScore(Coverage coverage)
    {
        float additionalScore = 0;
        for (String className : coverage.getStatements().keySet())
        {
            boolean[] coveredStatements = coverage.getStatements().get(className); //statements covered by the test contained in className
            boolean[] alreadyCoveredStatements = this.getCoveredStatements().get(className);
            for (int x = 0; x < coveredStatements.length; x++)
            {
                if (coveredStatements[x])
                {
                    //check if it is already covered by other tests in className
                    if (alreadyCoveredStatements != null)
                    {
                        if (!alreadyCoveredStatements[x])
                        {
                            //statement not yet covered by other tests, compute one Score
                            alreadyCoveredStatements[x] = true;
                            additionalScore++;

                        }
                    }
                }
            }
            this.getCoveredStatements().put(className, alreadyCoveredStatements);
        }

        return additionalScore;
    }
    
    private float getAdditionalBranchCoverageScore(Coverage coverage)
    {
        float additionalScore = 0;
        for (String className : coverage.getBranches().keySet())
        {
            boolean[] coveredBranches = coverage.getBranches().get(className); //branches covered by the test contained in className
            boolean[] alreadyCoveredBranches = this.getCoveredBranches().get(className);
            for (int x = 0; x < coveredBranches.length; x++)
            {
                if (coveredBranches[x])
                {
                    //check if it is already covered by other tests in className
                    if (alreadyCoveredBranches != null)
                    {
                        if (!alreadyCoveredBranches[x])
                        {
                            //branch not yet covered by other tests, compute one Score
                            alreadyCoveredBranches[x] = true;
                            additionalScore++;

                        }
                    }
                }
            }
            this.getCoveredBranches().put(className, alreadyCoveredBranches);
        }

        return additionalScore;
    }

    private float getAdditionalMethodCoverageScore(Coverage coverage)
    {
        float additionalScore = 0;
        for (String className : coverage.getMethods().keySet())
        {
            boolean[] coveredMethods = coverage.getMethods().get(className); //methods covered by the test contained in className
            boolean[] alreadyCoveredMethods = this.getCoveredMethods().get(className);
            for (int x = 0; x < coveredMethods.length; x++)
            {
                if (coveredMethods[x])
                {
                    //check if it is already covered by other tests in className
                    if (alreadyCoveredMethods != null)
                    {
                        if (!alreadyCoveredMethods[x])
                        {
                            //method not yet covered by other tests, compute one Score
                            alreadyCoveredMethods[x] = true;
                            additionalScore++;

                        }
                    }
                }
            }
            this.getCoveredMethods().put(className, alreadyCoveredMethods);
        }

        return additionalScore;
    }

    private float getTotalStatementCoverageScore(Coverage coverage)
    {
        float coveredStatementsTotal = 0;
        for (boolean[] data : coverage.getStatements().values())
        {
            for (int x = 0; x < data.length; x++)
            {
                if (data[x])
                {
                    coveredStatementsTotal++;
                }
            }
        }
        return coveredStatementsTotal;
    }

    private float getTotalMethodCoverageScore(Coverage coverage)
    {
        float coveredStatementsTotal = 0;
        for (boolean[] data : coverage.getMethods().values())
        {
            for (int x = 0; x < data.length; x++)
            {
                if (data[x])
                {
                    coveredStatementsTotal++;
                }
            }
        }
        return coveredStatementsTotal;
    }

    private float getTotalBranchCoverageScore(Coverage coverage)
    {
        float coveredStatementsTotal = 0;
        for (boolean[] data : coverage.getBranches().values())
        {
            for (int x = 0; x < data.length; x++)
            {
                if (data[x])
                {
                    coveredStatementsTotal++;
                }
            }
        }
        return coveredStatementsTotal;
    }

    public HashMap<String, boolean[]> getTestCoverage(AbstractTest test, String granularity)
    {
        JavaTestClass testClass = this.projectData.getTestClassByName(test.getTestClass().getName());
        HashMap<String, boolean[]> testCoverage = new HashMap<String, boolean[]>();
        if (testClass != null)
        {
            if (test.getTestGranularity().equals(Granularity.METHOD))
            {
                ClassMethod method = testClass.getMethodByName(test.getTestName());
                switch (granularity)
                {
                    case "statement":
                        testCoverage.putAll(method.getCoverage().getStatements());
                        break;
                    case "method":
                        testCoverage.putAll(method.getCoverage().getMethods());
                        break;
                    case "branch":
                        testCoverage.putAll(method.getCoverage().getBranches());
                        break;
                }
            } else if (test.getTestGranularity().equals(Granularity.CLASS))
            {
                for (ClassMethod method : testClass.getMethods().values())
                {
                    switch (granularity)
                    {
                        case "statement":
                            testCoverage.putAll(method.getCoverage().getStatements());
                            break;
                        case "method":
                            testCoverage.putAll(method.getCoverage().getMethods());
                            break;
                        case "branch":
                            testCoverage.putAll(method.getCoverage().getBranches());
                            break;
                    }
                }
            }
        }
        return testCoverage;
    }

    public HashMap<String, boolean[]> getCoveredStatements()
    {
        return coveredStatements;
    }

    public void setCoveredStatements(HashMap<String, boolean[]> coveredStatements)
    {
        this.coveredStatements = coveredStatements;
    }

    public HashMap<String, boolean[]> getCoveredBranches()
    {
        return coveredBranches;
    }

    public void setCoveredBranches(HashMap<String, boolean[]> coveredBranches)
    {
        this.coveredBranches = coveredBranches;
    }

    public HashMap<String, boolean[]> getCoveredMethods()
    {
        return coveredMethods;
    }

    public void setCoveredMethods(HashMap<String, boolean[]> coveredMethods)
    {
        this.coveredMethods = coveredMethods;
    }

    public void updateCoveredStatements(HashMap<String, boolean[]> newCoveredCode)
    {
        this.coveredStatements.putAll(newCoveredCode);
    }

    public static HashMap<String, boolean[]> copyCoverageData(HashMap<String, boolean[]> oldData)
    {
        HashMap<String, boolean[]> newData = new HashMap<String, boolean[]>();
        for (Map.Entry<String, boolean[]> entry : oldData.entrySet())
        {
            boolean[] newProbe = new boolean[entry.getValue().length];
            System.arraycopy(entry.getValue(), 0, newProbe, 0, newProbe.length);
            newData.put(entry.getKey(), newProbe);
        }
        return newData;
    }

}
