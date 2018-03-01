/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.extractor.model;

import java.util.HashMap;

/**
 *
 * @author helenocampos
 */
public class Coverage
{

    private HashMap<String, boolean[]> statements;
    private HashMap<String, boolean[]> methods;
    private HashMap<String, boolean[]> branches;

    public Coverage(){
        this.statements = new HashMap<String, boolean[]>();
        this.methods = new HashMap<String, boolean[]>();
        this.branches = new HashMap<String, boolean[]>();
    }
    
    public HashMap<String, boolean[]> getStatements()
    {
        return statements;
    }

    public void setStatements(HashMap<String, boolean[]> statements)
    {
        this.statements = statements;
    }

    public HashMap<String, boolean[]> getMethods()
    {
        return methods;
    }

    public void setMethods(HashMap<String, boolean[]> methods)
    {
        this.methods = methods;
    }

    public HashMap<String, boolean[]> getBranches()
    {
        return branches;
    }

    public void setBranches(HashMap<String, boolean[]> branches)
    {
        this.branches = branches;
    }

    public void addStatementCoverage(String className, boolean[] coverageData)
    {
        this.statements.put(className, coverageData);
    }

    public void addMethodCoverage(String className, boolean[] coverageData)
    {
        this.methods.put(className, coverageData);
    }

    public void addBranchCoverage(String className, boolean[] coverageData)
    {
        this.branches.put(className, coverageData);
    }
}
