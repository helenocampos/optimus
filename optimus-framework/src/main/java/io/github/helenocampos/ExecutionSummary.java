/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import io.github.helenocampos.surefire.report.ExecutionData;

/**
 *
 * @author helenocampos
 */
public class ExecutionSummary
{

    public HashMap<String, ExecutionSummaryEntry> getExperimentData()
    {
        return experimentData;
    }

    private ExperimentContext experimentContext;
    private HashMap<String, ExecutionSummaryEntry> experimentData;

    public ExecutionSummary(List<ExecutionData> executionData)
    {
        experimentData = new HashMap<String, ExecutionSummaryEntry>();
        processExecutionData(executionData);
    }

    /*
     Assume all execution data from the list parameter are for the same technique. i.e: they are already filtered by technique
     */
    private void processExecutionData(List<ExecutionData> executionData)
    {
        boolean firstElement = true;

        for (ExecutionData data : executionData)
        {
            if (firstElement)
            {
                experimentSetup(data, executionData.size());
            }
            insertData(data.getTechnique(), data.getAPFD());
        }
        processStatsAPFD();
    }

    private void insertData(String technique, double apfd)
    {
        ExecutionSummaryEntry entry = getExperimentData().get(technique);

        if (entry == null)
        {
            entry = new ExecutionSummaryEntry(technique);
        }
        entry.addApfd(apfd);
        getExperimentData().put(technique, entry);
    }

    private void experimentSetup(ExecutionData data, int executionsAmount)
    {
        this.experimentContext = new ExperimentContext(executionsAmount, data.getSeededFaultsAmount(),
                data.getAmountExecutedTests(), data.getTestGranularity());
    }

    private void processStatsAPFD()
    {
        for (ExecutionSummaryEntry entry : getExperimentData().values())
        {
            calculateStatsAPFD(entry);
        }

    }

    private void calculateStatsAPFD(ExecutionSummaryEntry entry)
    {
        Double[] apfds = new Double[entry.getAmountApfds()];
        apfds = entry.getApfds().toArray(apfds);
        Median median = new Median();
        double[] unboxedApfd = ArrayUtils.toPrimitive(apfds);
        double medianValue = median.evaluate(unboxedApfd);

        Mean mean = new Mean();
        double meanValue = mean.evaluate(unboxedApfd);

        Max max = new Max();
        double maxValue = max.evaluate(unboxedApfd);

        Min min = new Min();
        double minValue = min.evaluate(unboxedApfd);

        StandardDeviation sd = new StandardDeviation();
        double sdValue = sd.evaluate(unboxedApfd);
        
        entry.setMaxAPFD(maxValue);
        entry.setMeanAPFD(meanValue);
        entry.setMedianAPFD(medianValue);
        entry.setMinAPFD(minValue);
        entry.setStandardDeviation(sdValue);
    }

    public ExperimentContext getExperimentContext()
    {
        return experimentContext;
    }

}
