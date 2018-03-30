/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos;

import io.github.helenocampos.surefire.report.ExecutionData;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.desc;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.ImageScale;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.math3.util.Precision;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

/**
 *
 * @author Heleno
 */
public final class SummaryReport {

    public static void generateSummaryReport(List<ExecutionData> executionData, String projectName, File projectFolder) {
        ExecutionSummary summary = new ExecutionSummary(executionData);
        buildSummaryReport(summary, executionData, projectName, projectFolder);
    }

    private SummaryReport() {

    }

    private static void buildSummaryReport(ExecutionSummary summary, List<ExecutionData> executionData, String projectName, File projectFolder) {
        createBoxPlot(projectFolder, executionData);
        JasperReportBuilder table = DynamicReports.report();
        StyleBuilder boldStyle = stl.style().bold();
        StyleBuilder bigStyle = stl.style(boldStyle).setFontSize(20).setHorizontalAlignment(HorizontalAlignment.CENTER);

        table.title(cmp.text("Prioritization Experiment Report Summary").setStyle(bigStyle));
        String timeStamp = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy").format(new Date());
        table.title(cmp.text("Generated at: " + timeStamp));
        table.title(cmp.text(""));

        if (summary.getExperimentContext() != null) {
            table.title(cmp.text("Experiment context").setStyle(boldStyle));
            table.title(cmp.text("Amount of test cases in the experimented software: " + summary.getExperimentContext().getTestCasesAmount()).setStyle(boldStyle));
            table.title(cmp.text("Granularity of the test cases in the experimented software: " + summary.getExperimentContext().getTestGranularity()).setStyle(boldStyle));
        }
        table.title(cmp.text(""));

        table.addColumn(Columns.column("Technique", "technique", DataTypes.stringType()));
        table.addColumn(Columns.column("Executions", "amountApfds", DataTypes.integerType()));
        table.addColumn(Columns.column("Min APFD", "minAPFD", DataTypes.doubleType()).setPattern("#,##0.###"));
        TextColumnBuilder<Double> meanAPFDColumn = Columns.column("Mean APFD", "meanAPFD", DataTypes.doubleType()).setPattern("#,##0.###");
        table.addColumn(meanAPFDColumn);
        table.addColumn(Columns.column("Median APFD", "medianAPFD", DataTypes.doubleType()).setPattern("#,##0.###"));
        table.addColumn(Columns.column("Max APFD", "maxAPFD", DataTypes.doubleType()).setPattern("#,##0.###"));
        table.addColumn(Columns.column("Standard Deviation", "standardDeviation", DataTypes.doubleType()).setPattern("#,##0.###"));

        StyleBuilder boldCenteredStyle = stl.style(boldStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        StyleBuilder columnTitleStyle = stl.style(boldCenteredStyle)
                .setBorder(stl.pen1Point())
                .setBackgroundColor(Color.LIGHT_GRAY);

        table.setColumnTitleStyle(columnTitleStyle)
                .highlightDetailEvenRows()
                .title(cmp.text("Project: " + projectName).setStyle(boldCenteredStyle))
                .pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle));

        table.setDataSource(new JRBeanCollectionDataSource(summary.getExperimentData().values()));
        table.sortBy(desc(meanAPFDColumn));

        JasperReportBuilder reportImage = DynamicReports.report();
        reportImage.title(cmp.image(new File(projectFolder, "boxplot.png").getAbsolutePath()).setImageScale(ImageScale.REAL_SIZE));

        JasperReportBuilder report = DynamicReports.report();
        report.title(cmp.verticalList(cmp.subreport(table), cmp.subreport(reportImage)));

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            report.toPdf(buffer);
            try {
                OutputStream outputStream = new FileOutputStream(new File(projectFolder, "summary_report.pdf"));
                buffer.writeTo(outputStream);
                outputStream.close();
                buffer.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SummaryReport.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SummaryReport.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (DRException ex) {
            Logger.getLogger(SummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createBoxPlot(File projectFolder, List<ExecutionData> executionData) {

        final BoxAndWhiskerCategoryDataset dataset = getBoxPlotData(new DefaultBoxAndWhiskerCategoryDataset(), executionData);

        final CategoryAxis xAxis = new CategoryAxis("Technique");
        xAxis.setMaximumCategoryLabelLines(5);
        final NumberAxis yAxis = new NumberAxis("APFD");

        NumberFormat df = NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(3);
        df.setMinimumFractionDigits(3);
        yAxis.setNumberFormatOverride(df);

        yAxis.setAutoRangeIncludesZero(false);
        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setFillBox(true);
        renderer.setSeriesPaint(0, Color.WHITE);
        renderer.setSeriesPaint(1, Color.LIGHT_GRAY);
        renderer.setSeriesOutlinePaint(0, Color.BLACK);
        renderer.setSeriesOutlinePaint(1, Color.BLACK);
        renderer.setUseOutlinePaintForWhiskers(true);

        Font legendFont = new Font("SansSerif", Font.PLAIN, 16);
        renderer.setLegendTextFont(0, legendFont);
        renderer.setLegendTextFont(1, legendFont);
        renderer.setMedianVisible(true);
        renderer.setMeanVisible(false);

        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

        final JFreeChart chart = new JFreeChart(
                "APFD boxplot",
                new Font("SansSerif", Font.BOLD, 14),
                plot,
                false
        );

        try {
            ChartUtilities.saveChartAsPNG(new File(projectFolder, "boxplot.png"), chart, 600, 800);
        } catch (IOException ex) {
            Logger.getLogger(SummaryReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static DefaultBoxAndWhiskerCategoryDataset getBoxPlotData(DefaultBoxAndWhiskerCategoryDataset dataset, List<ExecutionData> executionData) {
        HashMap<String, List<Double>> values = new HashMap<String, List<Double>>();
        for (ExecutionData entry : executionData) {
            List<Double> techniqueValues = values.get(entry.getTechnique());
            if (techniqueValues == null) {
                techniqueValues = new LinkedList<Double>();
            }
            techniqueValues.add(Precision.round(entry.getAPFD(), 3));
            values.put(entry.getTechnique(), techniqueValues);
        }

        for (Map.Entry<String, List<Double>> entry : values.entrySet()) {
            dataset.add(entry.getValue(), "Technique", entry.getKey());
        }
        return dataset;
    }
}
