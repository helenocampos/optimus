/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.extractor.model;

import java.util.HashMap;

/**
 *
 * @author helenocampos
 */
public class Coverage
{

    private HashMap<String, String> statements;
    private HashMap<String, String> methods;
    private HashMap<String, String> branches;
    private HashMap<String, String> coveredLines;
    private HashMap<String,String> linesHit;
    
    public Coverage(){
        this.statements = new HashMap<String, String>();
        this.methods = new HashMap<String, String>();
        this.branches = new HashMap<String, String>();
        this.coveredLines = new HashMap<String, String>();
        this.linesHit = new HashMap<String, String>();
    }
    
    public HashMap<String, boolean[]> getStatements()
    {
        
        return stringToArrayCoverage(statements);
    }

    private HashMap<String,boolean[]> stringToArrayCoverage(HashMap<String,String> stringCoverageMap){
        HashMap<String,boolean[]> booleanCoverageMap = new HashMap<String, boolean[]>();
        for(String coverageKey: stringCoverageMap.keySet()){
            String coverageString = stringCoverageMap.get(coverageKey);
            if(coverageString!=null){
                boolean[] coverageArray = new boolean[coverageString.length()];
                for(int a=0; a<coverageString.length(); a++){
                    char coverage = coverageString.charAt(a);
                    if(coverage == '1'){
                        coverageArray[a] = true;
                    }else if(coverage=='0'){
                        coverageArray[a] = false;
                    }
                }
                booleanCoverageMap.put(coverageKey, coverageArray);
            }
        }
        return booleanCoverageMap;
    }
    
    public void setStatements(HashMap<String, String> statements)
    {
        this.statements = statements;
    }

    public HashMap<String, boolean[]> getMethods()
    {
        
        return stringToArrayCoverage(methods);
    }

    public void setMethods(HashMap<String, String> methods)
    {
        this.methods = methods;
    }

    public HashMap<String, boolean[]> getBranches()
    {
        return stringToArrayCoverage(branches);
    }

    public void setBranches(HashMap<String, String> branches)
    {
        this.branches = branches;
    }
    
    private String getCoverageString(boolean[] coverageArray){
        String coverageString = "";
        for(int a=0; a<coverageArray.length; a++){
            String coverage = "0";
            if(coverageArray[a]){
                coverage = "1";
            }
            coverageString = coverageString + coverage;
        }
        return coverageString;
    }

    public void addLinesCovered(String className, String coveredLinesString){
        this.getCoveredLines().put(className, coveredLinesString);
    }
        
    public void addStatementCoverage(String className, boolean[] coverageData)
    {
        this.statements.put(className, getCoverageString(coverageData));
    }
    
    public void addLinesHit(String className, String lines)
    {
        if(this.linesHit==null){
            this.linesHit = new HashMap<>();
        }
        this.linesHit.put(className, lines);
    }
    
    public String getLinesHit(String className){
        if(this.linesHit==null){
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
}