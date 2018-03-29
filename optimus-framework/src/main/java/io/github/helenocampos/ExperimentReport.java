/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos;

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
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.ImageScale;
import org.apache.commons.math3.util.Precision;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import io.github.helenocampos.surefire.report.ExecutionData;
import io.github.helenocampos.surefire.report.TestExecution;

/**
 *
 * @author helenocampos
 */
public class ExperimentReport
{

    private List<ExecutionData> executionData;
    private String projectName;
    private File experimentFolder;

    public ExperimentReport(String projectName, File experimentFolder, List<String> reports)
    {
        this.executionData = new LinkedList<ExecutionData>();
        this.projectName = projectName;
        this.experimentFolder = experimentFolder;
        scanExperimentFolder();
        generateReport(reports);
    }

    private void scanExperimentFolder()
    {
        //each folder in the experiment folder is a prioritization run
        if (this.experimentFolder.isDirectory())
        {
            File[] subFiles = this.experimentFolder.listFiles();
            for (File subFile : subFiles)
            {
                if (subFile.isDirectory())
                {
                    File runFolder = new File(subFile, projectName);
                    ExecutionData data = new ExecutionData(runFolder.getAbsolutePath());
                    this.executionData.addAll(data.readExecutionData());
                }
            }
        }
    }

    private void generateReport(List<String> reports)
    {
        if (reports != null)
        {
            for (String report : reports)
            {
                if (report.equals("summary"))
                {
                    ExecutionSummary summary = new ExecutionSummary(this.executionData);
                    buildSummaryReport(summary);
                } else if (report.equals("raw"))
                {
                    buildRawDataReport();
                }
            }
        } else
        {
            ExecutionSummary summary = new ExecutionSummary(this.executionData);
            buildSummaryReport(summary);
            buildRawDataReport();
        }

    }

    private void buildSummaryReport(ExecutionSummary summary)
    {
        createBoxPlot();
        JasperReportBuilder table = DynamicReports.report();
        StyleBuilder boldStyle = stl.style().bold();
        StyleBuilder bigStyle = stl.style(boldStyle).setFontSize(20).setHorizontalAlignment(HorizontalAlignment.CENTER);

        table.title(cmp.text("Prioritization Experiment Report Summary").setStyle(bigStyle));
        String timeStamp = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy").format(new Date());
        table.title(cmp.text("Generated at: " + timeStamp));
        table.title(cmp.text(""));

        if (summary.getExperimentContext() != null)
        {
            table.title(cmp.text("Experiment context").setStyle(boldStyle));
            table.title(cmp.text("Amount of mutation faults seeded into each execution: " + summary.getExperimentContext().getSeededFaultsAmount()).setStyle(boldStyle));
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
        reportImage.title(cmp.image(new File(experimentFolder, "boxplot.png").getAbsolutePath()).setImageScale(ImageScale.REAL_SIZE));

        JasperReportBuilder report = DynamicReports.report();
        report.title(cmp.verticalList(cmp.subreport(table), cmp.subreport(reportImage)));

        try
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            report.toPdf(buffer);
            try
            {
                OutputStream outputStream = new FileOutputStream(new File(experimentFolder, "summary_report.pdf"));
                buffer.writeTo(outputStream);
                outputStream.close();
                buffer.close();
            } catch (FileNotFoundException ex)
            {
                Logger.getLogger(ExperimentReport.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex)
            {
                Logger.getLogger(ExperimentReport.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (DRException ex)
        {
            Logger.getLogger(ExperimentReport.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void buildRawDataReport()
    {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Experiment data");

        createHeadings(sheet, workbook);
        int rowNum = 2;
        for (ExecutionData data : this.executionData)
        {
            proccessRowValues(data, rowNum++, sheet);
        }

        createExecutionLogs(workbook);

        try
        {
            FileOutputStream outputStream = new FileOutputStream(new File(experimentFolder, "raw_data.xls"));
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void createExecutionLogs(XSSFWorkbook wb)
    {

        int executionNr = 1;
        for (ExecutionData data : executionData)
        {
            XSSFSheet sheet = wb.createSheet("Execution #" + executionNr++);
            int order = 1;
            int testNr = 2;
            createLogHeadings(sheet, data);
            for (TestExecution execution : data.getExecutedTests())
            {

                Row row = sheet.createRow(testNr++);
                Cell cell = row.createCell(0);
                cell.setCellValue(order++);

                cell = row.createCell(1);
                cell.setCellValue(execution.getTestName());

                cell = row.createCell(2);
                cell.setCellValue(execution.getTestResult());

                cell = row.createCell(3);
                double time = (double)execution.getExecutionTime()/1000;
                cell.setCellValue(time);
            }
        }
    }

    private void createLogHeadings(XSSFSheet sheet, ExecutionData data)
    {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Test logs for " + data.getTechnique() + " prioritization technique");

        row = sheet.createRow(1);
        cell = row.createCell(0);
        cell.setCellValue("Order");

        cell = row.createCell(1);
        cell.setCellValue("Test name");

        cell = row.createCell(2);
        cell.setCellValue("Test passed?");

        cell = row.createCell(3);
        cell.setCellValue("Execution time (seconds)");
    }

    private void proccessRowValues(ExecutionData data, int rowNum, XSSFSheet sheet)
    {
        Row row = sheet.createRow(rowNum);
        int colNum = 0;

        for (String value : data.getValues())
        {
            Cell cell = row.createCell(colNum++);
            cell.setCellValue(value);
        }
    }

    private void createHeadings(XSSFSheet sheet, XSSFWorkbook wb)
    {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        String timeStamp = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy").format(new Date());
        cell.setCellValue("Report for " + this.projectName + " experiment. Generated at: " + timeStamp);
        row = sheet.createRow(1);
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        cell = row.createCell(0);
        cell.setCellValue("Technique");
        cell.setCellStyle(style);
        cell = row.createCell(1);
        cell.setCellValue("APFD");
        cell.setCellStyle(style);
        cell = row.createCell(2);
        cell.setCellValue("Faults amount");
        cell.setCellStyle(style);
        cell = row.createCell(3);
        cell.setCellValue("Executed tests");
        cell.setCellStyle(style);
        cell = row.createCell(4);
        cell.setCellValue("Test granularity");
        cell.setCellStyle(style);
        cell = row.createCell(5);
        cell.setCellValue("Execution time (seconds)");
        cell.setCellStyle(style);

    }

    private void createBoxPlot()
    {

        final BoxAndWhiskerCategoryDataset dataset = getBoxPlotData(new DefaultBoxAndWhiskerCategoryDataset());

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

        try
        {
            ChartUtilities.saveChartAsPNG(new File(experimentFolder, "boxplot.png"), chart, 600, 800);
        } catch (IOException ex)
        {
            Logger.getLogger(ExperimentReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private DefaultBoxAndWhiskerCategoryDataset getBoxPlotData(DefaultBoxAndWhiskerCategoryDataset dataset)
    {
        HashMap<String, List<Double>> values = new HashMap<String, List<Double>>();
        for (ExecutionData entry : executionData)
        {
            List<Double> techniqueValues = values.get(entry.getTechnique());
            if (techniqueValues == null)
            {
                techniqueValues = new LinkedList<Double>();
            }
            techniqueValues.add(Precision.round(entry.getAPFD(), 3));
            values.put(entry.getTechnique(), techniqueValues);
        }

        for (Map.Entry<String, List<Double>> entry : values.entrySet())
        {
            dataset.add(entry.getValue(), "Technique", entry.getKey());
        }
        return dataset;
    }

}
