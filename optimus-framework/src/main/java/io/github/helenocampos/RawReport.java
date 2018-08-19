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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;

/**
 *
 * @author helenocampos
 */
public final class RawReport {

    public static void generateSummaryReport(List<ExecutionData> executionData, String projectName, File projectFolder) {
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
            proccessRowValues(data, rowNum++, sheet, workbook, projectFolder);
        }
        createExecutionLogs(executionData, createExecutionsFolder(projectFolder));
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

    private static File createExecutionsFolder(File projectFolder) {
        File executionsFolder = new File(projectFolder, "executionsFolder");
        if (!executionsFolder.exists()) {
            executionsFolder.mkdir();
        }
        return executionsFolder;
    }

    private static void createExecutionLogs(List<ExecutionData> executionData, File executionLogsFolder) {
        int executionNr = 1;
        for (ExecutionData data : executionData) {
            Path file = Paths.get(executionLogsFolder.getAbsolutePath(), "Execution#" + executionNr++ +".txt");
            int order = 1;
            StringBuilder sb = new StringBuilder();
            createLogHeadings(sb, data);
            
            for (TestExecution execution : data.getExecutedTests()) {
                double time = (double) execution.getExecutionTime() / 1000;
                sb.append(String.format("%s %50s %20s %20s \r\n", order++, execution.getTestName(), execution.getTestResult(), time));
            }
            try {
                Files.write(file, Arrays.asList(sb.toString()), Charset.forName("UTF-8"));
            } catch (IOException ex) {
                Logger.getLogger(RawReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void createLogHeadings(StringBuilder sb, ExecutionData data) {
        sb.append("Test logs for ");
        sb.append(data.getTechnique());
        sb.append(" prioritization technique \r\n\n");
        sb.append(String.format("%s %50s %20s %20s \r\n", "Order", "Test name", "Test passed?", "Execution time(seconds)"));
    }

    private static void proccessRowValues(ExecutionData data, int rowNum, XSSFSheet sheet, XSSFWorkbook wb, File projectFolder) {
        Row row = sheet.createRow(rowNum);
        int colNum = 0;

        for (String value : data.getValues()) {
            Cell cell = row.createCell(colNum++);
            cell.setCellValue(value);
        }
        createExecutionOrderLink(colNum, row, rowNum, wb, projectFolder);
    }

    private static XSSFCellStyle getLinkStyle(XSSFWorkbook wb) {
        XSSFCellStyle hlinkstyle = wb.createCellStyle();
        XSSFFont hlinkfont = wb.createFont();
        hlinkfont.setUnderline(XSSFFont.U_SINGLE);
        hlinkfont.setColor(HSSFColor.BLUE.index);
        hlinkstyle.setFont(hlinkfont);
        return hlinkstyle;
    }

    private static void createExecutionOrderLink(int colNum, Row row, int rowNum, XSSFWorkbook wb, File projectFolder) {
        Cell cell = row.createCell(colNum++);

        CreationHelper createHelper = wb.getCreationHelper();
        XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.FILE);
        
        link.setAddress("executionsFolder/Execution%23" + (rowNum - 1)+".txt");
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
