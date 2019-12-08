/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire.ordering.techniques;

import info.heleno.surefire.analyzer.coverage.CoverageAnalyzer;
import info.heleno.surefire.api.DefaultOrderer;
import info.heleno.surefire.ordering.Strategy;
import info.heleno.extractor.model.CoverageGranularity;
import info.heleno.testing.AbstractTest;

/**
 *
 * @author helenocampos
 */
public abstract class TotalCoverage extends DefaultOrderer<AbstractTest> {

    CoverageAnalyzer analyzer;

    public TotalCoverage() {
        analyzer = new CoverageAnalyzer();
    }

    public abstract CoverageGranularity getCoverageGranularity();

    @Override
    public int compare(AbstractTest o1, AbstractTest o2) {
        float thiz = analyzer.getTotalTestCoverage(o1, getCoverageGranularity());
        float that = analyzer.getTotalTestCoverage(o2, getCoverageGranularity());
        return Float.compare(thiz, that);
    }

    @Override
    public String getStrategy() {
        return Strategy.DEFAULT.getName();
    }
}
