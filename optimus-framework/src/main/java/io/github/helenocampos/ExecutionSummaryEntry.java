/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class ExecutionSummaryEntry
{

    private String technique;
    private int amountApfds;
    private List<Double> apfds;

    private double minAPFD;
    private double meanAPFD;
    private double medianAPFD;
    private double maxAPFD;

    public ExecutionSummaryEntry(String technique)
    {
        this.technique = technique;
        this.apfds = new LinkedList<Double>();
    }

    public String getTechnique()
    {
        return technique;
    }

    public void addApfd(double value)
    {
        this.amountApfds++;
        this.getApfds().add(value);
    }

    public double getMinAPFD()
    {
        return minAPFD;
    }

    public void setMinAPFD(double minAPFD)
    {
        this.minAPFD = minAPFD;
    }

    public double getMeanAPFD()
    {
        return meanAPFD;
    }

    public void setMeanAPFD(double meanAPFD)
    {
        this.meanAPFD = meanAPFD;
    }

    public double getMedianAPFD()
    {
        return medianAPFD;
    }

    public void setMedianAPFD(double medianAPFD)
    {
        this.medianAPFD = medianAPFD;
    }

    public double getMaxAPFD()
    {
        return maxAPFD;
    }

    public void setMaxAPFD(double maxAPFD)
    {
        this.maxAPFD = maxAPFD;
    }

    public int getAmountApfds()
    {
        return amountApfds;
    }

    public void setAmountApfds(int amountApfds)
    {
        this.amountApfds = amountApfds;
    }

    public List<Double> getApfds()
    {
        return apfds;
    }

}
