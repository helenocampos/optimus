/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import io.github.helenocampos.surefire.report.ExecutionData;
import io.github.helenocampos.surefire.report.TestExecution;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;

/**
 *
 * @author helenocampos
 */
public final class RawReport {

    public static void generateSummaryReport(List<ExecutionData> executionData, String projectName, File projectFolder){
        buildRawDataReport(executionData, projectName, projectFolder);
    }
    
    private RawReport() {
    }

    private static void buildRawDataReport(List<ExecutionData> executionData, String projectName, File projectFolder) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Execution data");

        createHeadings(sheet, workbook, projectName);
        int rowNum = 2;
        for (ExecutionData data : executionData) {
            proccessRowValues(data, rowNum++, sheet, workbook);
        }

        createExecutionLogs(workbook, executionData);

        try {
            FileOutputStream outputStream = new FileOutputStream(new File(projectFolder, "raw_data.xlsx"));
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createExecutionLogs(XSSFWorkbook wb, List<ExecutionData> executionData) {

        int executionNr = 1;
        for (ExecutionData data : executionData) {
            XSSFSheet sheet = wb.createSheet("Execution#" + executionNr++);
            int order = 1;
            int testNr = 2;
            createLogHeadings(sheet, data, wb);
            for (TestExecution execution : data.getExecutedTests()) {

                Row row = sheet.createRow(testNr++);
                Cell cell = row.createCell(0);
                cell.setCellValue(order++);

                cell = row.createCell(1);
                cell.setCellValue(execution.getTestName());

                cell = row.createCell(2);
                cell.setCellValue(execution.getTestResult());

                cell = row.createCell(3);
                double time = (double) execution.getExecutionTime() / 1000;
                cell.setCellValue(time);
            }
        }
    }

    private static void createLogHeadings(XSSFSheet sheet, ExecutionData data, XSSFWorkbook wb) {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Test logs for " + data.getTechnique() + " prioritization technique");
        createExecutionDataLink(row, wb);

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

    private static void createExecutionDataLink(Row row, XSSFWorkbook wb) {
        Cell cell = row.createCell(5);

        CreationHelper createHelper = wb.getCreationHelper();
        XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.DOCUMENT);

        link.setAddress("'Execution data'!A1");
        cell.setHyperlink(link);
        cell.setCellValue("Back to Execution data");
        cell.setCellStyle(getLinkStyle(wb));
    }

    private static void proccessRowValues(ExecutionData data, int rowNum, XSSFSheet sheet, XSSFWorkbook wb) {
        Row row = sheet.createRow(rowNum);
        int colNum = 0;

        for (String value : data.getValues()) {
            Cell cell = row.createCell(colNum++);
            cell.setCellValue(value);
        }
        createExecutionOrderLink(colNum, row, rowNum, wb);
    }

    private static XSSFCellStyle getLinkStyle(XSSFWorkbook wb) {
        XSSFCellStyle hlinkstyle = wb.createCellStyle();
        XSSFFont hlinkfont = wb.createFont();
        hlinkfont.setUnderline(XSSFFont.U_SINGLE);
        hlinkfont.setColor(HSSFColor.BLUE.index);
        hlinkstyle.setFont(hlinkfont);
        return hlinkstyle;
    }

    private static void createExecutionOrderLink(int colNum, Row row, int rowNum, XSSFWorkbook wb) {
        Cell cell = row.createCell(colNum++);

        CreationHelper createHelper = wb.getCreationHelper();
        XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.DOCUMENT);
        link.setAddress("'Execution#" + (rowNum - 1) + "'!A1");
        cell.setHyperlink(link);
        cell.setCellValue("Order");
        cell.setCellStyle(getLinkStyle(wb));
    }

    private static void createHeadings(XSSFSheet sheet, XSSFWorkbook wb, String projectName) {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        String timeStamp = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy").format(new Date());
        cell.setCellValue("Report for " + projectName + " build. Generated at: " + timeStamp);
        row = sheet.createRow(1);
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        cell = row.createCell(0);
        cell.setCellValue("Project");
        cell.setCellStyle(style);
        cell = row.createCell(1);
        cell.setCellValue("Prioritization technique");
        cell.setCellStyle(style);
        cell = row.createCell(2);
        cell.setCellValue("APFD");
        cell.setCellStyle(style);
        cell = row.createCell(3);
        cell.setCellValue("Failures");
        cell.setCellStyle(style);
        cell = row.createCell(4);
        cell.setCellValue("Executed tests");
        cell.setCellStyle(style);
        cell = row.createCell(5);
        cell.setCellValue("Test granularity");
        cell.setCellStyle(style);
        cell = row.createCell(6);
        cell.setCellValue("Execution time (seconds)");
        cell.setCellStyle(style);
        cell = row.createCell(7);
        cell.setCellValue("Timestamp");
        cell.setCellStyle(style);

        cell = row.createCell(8);
        cell.setCellValue("Execution order");
        cell.setCellStyle(style);
    }
}
