/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.extractor.model;

import java.util.HashMap;
import java.util.LinkedHashMap;

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
        String coverageString = "";
        for (int a = 0; a < coverageArray.length; a++)
        {
            String coverage = "0";
            if (coverageArray[a])
            {
                coverage = "1";
            }
            coverageString = coverageString + coverage;
        }
        return coverageString;
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

    public String getCoverageString(CoverageGranularity coverageGranularity){
        String coverageString = "";
        for (String coverage : this.getCoverage(coverageGranularity).values())
        {
            coverageString += coverage;
        }
        return coverageString;
    }
    
    public static String getCoverageString(CoverageGranularity coverageGranularity, LinkedHashMap<String,String> coverageData){
        String coverageString = "";
        for (String coverage : coverageData.values())
        {
            coverageString += coverage;
        }
        return coverageString;
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
            String coverage1String = coverage1.get(key);
            String coverage2String = coverage2.get(key);
            if (coverage1String == null)
            { //coverage1 doesn't have the element from 2. just add it to the merged map
                merged.put(key, coverage2String);
            } else
            {
                merged.put(key, merge(coverage1String, coverage2String));
            }
        }

        return merged;
    }

    public static String merge(String coverageString1, String coverageString2)
    {
        String mergedString = "";
        if (coverageString1.length() == coverageString2.length())
        {
            for (int i = 0; i < coverageString1.length(); i++)
            {
                char coverage1 = coverageString1.charAt(i);
                char coverage2 = coverageString2.charAt(i);
                if (coverage1 == '1' || coverage2 == '1')
                {
                    mergedString += "1";
                } else
                {
                    mergedString += "0";
                }
            }
        }

        return mergedString;
    }
    
    public LinkedHashMap<String, String> getCoverage(CoverageGranularity coverageGranularity){
        switch(coverageGranularity){
            case STATEMENT: return this.statements;
            case BRANCH: return this.branches;
            case METHOD: return this.methods;
            default: return new LinkedHashMap<String, String>();
        }
    }
    
    public static String getEmptyCoverageString(int size){
        String coverageString = "";
        for(int i=0; i<size; i++){
            coverageString+="0";
        }
        return coverageString;
    }
    
    public void includeRemainingClassesData(JavaClass clazz){
        includeClassData(clazz.getCoverageName(), statements, clazz.getExecutableLines());
        includeClassData(clazz.getCoverageName(), branches, clazz.getExecutableBranches());
        includeClassData(clazz.getCoverageName(), methods, clazz.getExecutableMethods());
    }
    
    private void includeClassData(String className, HashMap<String,String> coverageData, int coverageSize){
        String coverage = coverageData.get(className);
        if(coverage==null){
            coverageData.put(className, getEmptyCoverageString(coverageSize));
        }
    }
    
    
}
