/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.ordering;

import io.github.helenocampos.surefire.api.Orderer;
import io.github.helenocampos.surefire.ordering.techniques.AdditionalBranchCoverage;
import io.github.helenocampos.surefire.ordering.techniques.AdditionalMethodCoverage;
import io.github.helenocampos.surefire.ordering.techniques.AdditionalStatementCoverage;
import io.github.helenocampos.surefire.ordering.techniques.AlphabeticalOrder;
import io.github.helenocampos.surefire.ordering.techniques.RandomPrioritization;
import io.github.helenocampos.surefire.ordering.techniques.TotalBranchCoverage;
import io.github.helenocampos.surefire.ordering.techniques.TotalMethodCoverage;
import io.github.helenocampos.surefire.ordering.techniques.TotalStatementCoverage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.surefire.report.ConsoleStream;

/**
 *
 * @author helenocampos
 */
public enum PrioritizationTechniques
{
    DEFAULT("default", null),
    ALPHABETICAL("alphabetical", AlphabeticalOrder.class),
    RANDOM("random", RandomPrioritization.class),
    TOTAL_STATEMENT_COVERAGE("total statement coverage", TotalStatementCoverage.class),
    TOTAL_METHOD_COVERAGE("total method coverage", TotalMethodCoverage.class),
    TOTAL_BRANCH_COVERAGE("total branch coverage", TotalBranchCoverage.class),
    ADDITIONAL_STATEMENT_COVERAGE("additional statement coverage", AdditionalStatementCoverage.class),
    ADDITIONAL_METHOD_COVERAGE("additional method coverage", AdditionalMethodCoverage.class),
    ADDITIONAL_BRANCH_COVERAGE("additional branch coverage", AdditionalBranchCoverage.class);

    PrioritizationTechniques(String name, Class orderer)
    {
        this.name = name;
        this.orderer = orderer;
    }
    private String name;
    private Class orderer;
    
    public static Orderer getTechinqueByNameAndGranularity(String name, String granularity, ConsoleStream out){
        for(PrioritizationTechniques technique: values()){
            if(technique.getName().equals(name)){
                    out.println("Ordering tests at the "+granularity+" level, using the "+name+" prioritization technique. \n");
                    return technique.getOrderer();
            }
        }
        out.println("Using default tests ordering at the "+granularity+" level. \n");
        return null;
    }
    
    public String getName(){
        return this.name;
    }
    
    
    public Orderer getOrderer(){
        try
        {
            return (Orderer) this.orderer.newInstance();
        } catch (InstantiationException ex)
        {
            Logger.getLogger(PrioritizationTechniques.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            Logger.getLogger(PrioritizationTechniques.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static String[] getAllTechniquesNames(){
        String[] names = new String[values().length];
        int index  = 0;
        for(PrioritizationTechniques technique: values()){
            names[index++] = technique.getName();
        }
        return names;
    }
}
