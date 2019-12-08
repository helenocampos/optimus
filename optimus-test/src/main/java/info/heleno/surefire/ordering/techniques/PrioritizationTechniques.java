/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire.ordering.techniques;

import info.heleno.surefire.api.Orderer;
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
    OPTIMAL("optimal", OptimalOrder.class),
    TOTAL_STATEMENT_COVERAGE("total statement coverage", TotalStatementCoverage.class),
    TOTAL_METHOD_COVERAGE("total method coverage", TotalMethodCoverage.class),
    TOTAL_BRANCH_COVERAGE("total branch coverage", TotalBranchCoverage.class),
    ADDITIONAL_STATEMENT_COVERAGE("additional statement coverage", AdditionalStatementCoverage.class),
    ADDITIONAL_METHOD_COVERAGE("additional method coverage", AdditionalMethodCoverage.class),
    ADDITIONAL_BRANCH_COVERAGE("additional branch coverage", AdditionalBranchCoverage.class),
    MOST_EXECUTED_LINES("most executed lines", MostExecutedLinesOrder.class),
    FOS("farthest first ordered sequence", FarthestFirstOrderedSequence.class),
    GOS("greed-aided-clustering ordered sequence", GreedAidedClusteringOrderedSequence.class),
    MAXMIN_STATEMENT_ART("maxmin statement ART", MaxMinStatementART.class),
    MAXMIN_BRANCH_ART("maxmin branch ART", MaxMinBranchART.class),
    MAXMIN_METHOD_ART("maxmin method ART", MaxMinMethodART.class),
    MAXMIN_STATEMENT_ART2("maxmin statement ART2", MaxMinStatementART2.class),
    MAXMIN_BRANCH_ART2("maxmin branch ART2", MaxMinBranchART2.class),
    MAXMIN_METHOD_ART2("maxmin method ART2", MaxMinMethodART2.class),
    MAXAVG_STATEMENT_ART("maxavg statement ART", MaxAvgStatementART.class),
    MAXAVG_BRANCH_ART("maxavg branch ART", MaxAvgBranchART.class),
    MAXAVG_METHOD_ART("maxavg method ART", MaxAvgMethodART.class),
    MAXMAX_STATEMENT_ART("maxmax statement ART", MaxMaxStatementART.class),
    MAXMAX_BRANCH_ART("maxmax branch ART", MaxMaxBranchART.class),
    MAXMAX_METHOD_ART("maxmax method ART", MaxMaxMethodART.class),
    MOST_FAILURES_FIRST("most failures first", MostFailuresFirstOrder.class),
    RECENT_FAILURES_FIRST("recent failures first", RecentFailuresFirstOrder.class),
    TOTAL_DIFF_METHOD_COVERAGE("total diff method coverage", TotalDiffMethodCoverage.class),
    ADDITIONAL_DIFF_METHOD_COVERAGE("additional diff method coverage", AdditionalDiffMethodCoverage.class),
    TOTAL_DIFF_CLASS_COVERAGE("total diff class coverage", TotalDiffClassCoverage.class),
    ADDITIONAL_DIFF_CLASS_COVERAGE("additional diff class coverage", AdditionalDiffClassCoverage.class);

    PrioritizationTechniques(String name, Class orderer)
    {
        this.name = name;
        this.orderer = orderer;
    }
    private String name;
    private Class orderer;

    public static Orderer getTechinqueByNameAndGranularity(String name, String granularity, ConsoleStream out)
    {
        for (PrioritizationTechniques technique : values())
        {
            if (technique.getName().equalsIgnoreCase(name))
            {
                if (out != null)
                {
                    out.println("Ordering tests at the " + granularity + " level, using the " + name + " prioritization technique. \n");
                }

                return technique.getOrderer();
            }
        }
        if (out != null)
        {
            out.println("Using default tests ordering at the " + granularity + " level. \n");
        }
        return null;
    }

    public String getName()
    {
        return this.name;
    }

    public Orderer getOrderer()
    {
        try
        {
            if (this != null)
            {
                if (this.orderer != null)
                {
                    return (Orderer) this.orderer.newInstance();
                }
            }

        } catch (InstantiationException ex)
        {
            Logger.getLogger(PrioritizationTechniques.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            Logger.getLogger(PrioritizationTechniques.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String[] getAllTechniquesNames()
    {
        String[] names = new String[values().length];
        int index = 0;
        for (PrioritizationTechniques technique : values())
        {
            names[index++] = technique.getName();
        }
        return names;
    }
}
