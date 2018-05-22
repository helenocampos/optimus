/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.extractor.model;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author helenocampos
 */
public class Coverage
{

    private LinkedHashMap<String, String> statements;
    private LinkedHashMap<String, String> methods;
    private LinkedHashMap<String, String> branches;
    private HashMap<String, String> coveredLines;
    private HashMap<String, String> linesHit;

    public Coverage()
    {
        this.statements = new LinkedHashMap<String, String>();
        this.methods = new LinkedHashMap<String, String>();
        this.branches = new LinkedHashMap<String, String>();
        this.coveredLines = new HashMap<String, String>();
        this.linesHit = new HashMap<String, String>();
    }

    public void setStatements(LinkedHashMap<String, String> statements)
    {
        this.statements = statements;
    }

    public void setMethods(LinkedHashMap<String, String> methods)
    {
        this.methods = methods;
    }

    public void setBranches(LinkedHashMap<String, String> branches)
    {
        this.branches = branches;
    }

    private String getCoverageString(boolean[] coverageArray)
    {
        StringBuilder coverageString = new StringBuilder();
        for (int a = 0; a < coverageArray.length; a++)
        {
            if (coverageArray[a])
            {
                coverageString.append("1");
            } else
            {
                coverageString.append("0");
            }
        }
        return coverageString.toString();
    }

    public void addLinesCovered(String className, String coveredLinesString)
    {
        this.getCoveredLines().put(className, coveredLinesString);
    }

    public void addStatementCoverage(String className, boolean[] coverageData)
    {
        this.statements.put(className, getCoverageString(coverageData));
    }

    public void addLinesHit(String className, String lines)
    {
        if (this.linesHit == null)
        {
            this.linesHit = new HashMap<>();
        }
        this.linesHit.put(className, lines);
    }

    public String getLinesHit(String className)
    {
        if (this.linesHit == null)
        {
            this.linesHit = new HashMap<>();
        }
        return this.linesHit.get(className);
    }

    public void addMethodCoverage(String className, boolean[] coverageData)
    {
        this.methods.put(className, getCoverageString(coverageData));
    }

    public void addBranchCoverage(String className, boolean[] coverageData)
    {
        this.branches.put(className, getCoverageString(coverageData));
    }

    public HashMap<String, String> getCoveredLines()
    {
        return coveredLines;
    }

    public void setCoveredLines(HashMap<String, String> coveredLines)
    {
        this.coveredLines = coveredLines;
    }

    public String getCoverageString(CoverageGranularity coverageGranularity)
    {
        StringBuilder coverageString = new StringBuilder();
        for (String coverage : this.getCoverage(coverageGranularity, true).values())
        {
            coverageString.append(coverage);
        }
        return coverageString.toString();
    }

    public static String getCoverageString(LinkedHashMap<String, String> coverageData)
    {
        StringBuilder coverageString = new StringBuilder();
        for (String coverage : coverageData.values())
        {
            coverageString.append(coverage);
        }
        return coverageString.toString();
    }

    public static Coverage merge(Coverage coverage1, Coverage coverage2)
    {
        Coverage newCoverage = new Coverage();
        newCoverage.statements = merge(coverage1.statements, coverage2.statements);
        newCoverage.methods = merge(coverage1.methods, coverage2.methods);
        newCoverage.branches = merge(coverage1.branches, coverage2.branches);
        return newCoverage;
    }

    public static LinkedHashMap<String, String> merge(LinkedHashMap<String, String> coverage1, LinkedHashMap<String, String> coverage2)
    {
        LinkedHashMap<String, String> merged = new LinkedHashMap<>();
        for (String key : coverage1.keySet())
        {
            String coverage1String = coverage1.get(key);
            String coverage2String = coverage2.get(key);
            if (coverage2String == null)
            { //coverage2 doesn't have the element from 1. just add it to the merged map
                merged.put(key, coverage1String);
            } else
            {
                merged.put(key, merge(coverage1String, coverage2String));
            }
        }

        for (String key : coverage2.keySet())
        {
            if (merged.get(key) == null)
            { //coverage1 doesn't have the element from 2. just add it to the merged map
                merged.put(key, coverage2.get(key));
            }
        }
        return merged;
    }

    public static String merge(String coverageString1, String coverageString2)
    {
        StringBuilder mergedString = new StringBuilder(coverageString1);
        if (coverageString1.length() == coverageString2.length())
        {
            if (coverageString1.equals(coverageString2))
            {
                return coverageString1;
            }

            for (int i = 0; i < coverageString1.length(); i++)
            {
                if (coverageString2.charAt(i) == '1' && coverageString1.charAt(i) == '0')
                {
                    mergedString.setCharAt(i, '1');
                }
            }
        }
        return mergedString.toString();
    }

    public LinkedHashMap<String, String> getCoverage(CoverageGranularity coverageGranularity, boolean includeNotCoveredClasses)
    {
        LinkedHashMap<String, String> coverage = new LinkedHashMap();

        switch (coverageGranularity)
        {
            case STATEMENT:
                coverage = this.statements;
                break;
            case BRANCH:
                coverage = this.branches;
                break;
            case METHOD:
                coverage = this.methods;
                break;
        }

        if (includeNotCoveredClasses)
        {
            ProjectData projectData = ProjectData.getProjectData();
            for (String className : projectData.getClasses().keySet())
            {
                JavaSourceCodeClass clazz = projectData.getClassByName(className);
                if (!coverage.containsKey(clazz.getCoverageName()))
                {
                    coverage.put(clazz.getCoverageName(), getEmptyCoverageString(clazz.getExecutableElements(coverageGranularity)));
                }
            }
        }
        return coverage;
    }

    public static String getEmptyCoverageString(int size)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++)
        {
            builder.append("0");
        }
        return builder.toString();
    }

    public void includeRemainingClassesData(JavaClass clazz)
    {
        includeClassData(clazz.getCoverageName(), statements, clazz.getExecutableLines());
        includeClassData(clazz.getCoverageName(), branches, clazz.getExecutableBranches());
        includeClassData(clazz.getCoverageName(), methods, clazz.getExecutableMethods());
    }

    private void includeClassData(String className, HashMap<String, String> coverageData, int coverageSize)
    {
        String coverage = coverageData.get(className);
        if (coverage == null)
        {
            coverageData.put(className, getEmptyCoverageString(coverageSize));
        }
    }
}
