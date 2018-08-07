/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.ordering.techniques;

import io.github.helenocampos.extractor.model.CoverageGranularity;
import io.github.helenocampos.extractor.model.ModificationsGranularity;
import io.github.helenocampos.optimusmodificationsanalyzer.ModificationsAnalyzer;
import io.github.helenocampos.surefire.analyzer.coverage.CoverageAnalyzer;
import io.github.helenocampos.surefire.api.DefaultOrderer;
import io.github.helenocampos.surefire.ordering.Strategy;
import io.github.helenocampos.testing.AbstractTest;
import java.util.Set;

/**
 *
 * @author helenocampos
 */
public abstract class TotalDiffCoverage extends DefaultOrderer<AbstractTest> {

    private CoverageAnalyzer analyzer;
    private Set<String> modifiedElements;

    public TotalDiffCoverage() {
        this.analyzer = new CoverageAnalyzer();
        ModificationsAnalyzer diffAnalyzer = new ModificationsAnalyzer(this.analyzer.getProjectData());
        this.modifiedElements = diffAnalyzer.getModifiedElements(getModificationsGranularity());
    }

    public abstract ModificationsGranularity getModificationsGranularity();

    @Override
    public int compare(AbstractTest o1, AbstractTest o2) {
        float thiz;
        float that;
        if (this.modifiedElements.isEmpty()) {
            thiz = analyzer.getTotalTestCoverage(o1, CoverageGranularity.METHOD);
            that = analyzer.getTotalTestCoverage(o2, CoverageGranularity.METHOD);
        } else {
            thiz = analyzer.getTotalDiffTestCoverage(o1, getModificationsGranularity(), this.modifiedElements);
            that = analyzer.getTotalDiffTestCoverage(o2, getModificationsGranularity(), this.modifiedElements);
        }
        return Float.compare(thiz, that);
    }

    @Override
    public String getStrategy() {
        return Strategy.DEFAULT.getName();
    }
}
