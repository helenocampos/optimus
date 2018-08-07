/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.ordering.techniques;

import io.github.helenocampos.extractor.model.CoverageGranularity;
import io.github.helenocampos.surefire.analyzer.coverage.CoverageAnalyzer;
import io.github.helenocampos.surefire.api.DefaultOrderer;
import io.github.helenocampos.surefire.ordering.Strategy;
import io.github.helenocampos.testing.AbstractTest;
import java.io.FileWriter;
import java.io.IOException;

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
