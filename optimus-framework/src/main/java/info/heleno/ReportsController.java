/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno;

import info.heleno.surefire.report.ExecutionData;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Heleno
 */
public class ReportsController {

    private List<ExecutionData> executionData;
    private String projectName;
    private File rootFolder;

    public ReportsController(String projectName, File rootFolder, List<String> reports, String reportType) {
        this.executionData = new LinkedList<ExecutionData>();
        this.projectName = projectName;
        this.rootFolder = rootFolder;
        if (reportType.equals("multiple")) {
            scanMultipleFolders();
        } else if (reportType.equals("single")) {
            scanSingleFolder();
        }
        generateReport(reports);
    }

    private void scanMultipleFolders() {
        //each folder in the root folder is a prioritization run
        if (this.rootFolder.isDirectory()) {
            File[] subFiles = this.rootFolder.listFiles();
            for (File subFile : subFiles) {
                if (subFile.isDirectory()) {
                    File runFolder;
                    if(PomManager.isMavenProject(subFile.getAbsolutePath())){
                        runFolder = subFile;
                    }else{
                        runFolder = new File(subFile, projectName);
                    }
                    ExecutionData data = new ExecutionData(runFolder.getAbsolutePath());
                    this.executionData.addAll(data.readExecutionData());
                }
            }
        }
    }

    private void scanSingleFolder() {
        if (this.rootFolder.isDirectory()) {
            ExecutionData data = new ExecutionData(rootFolder.getAbsolutePath());
            this.executionData.addAll(data.readExecutionData());

        }
    }

    private void generateReport(List<String> reports) {
        if (reports != null) {
            for (String report : reports) {
                if (report.equals("summary")) {
                    SummaryReport.generateSummaryReport(executionData, projectName, rootFolder);
                } else if (report.equals("raw")) {
                    RawReport.generateSummaryReport(executionData, projectName, rootFolder);
                }
            }
        } else {
            SummaryReport.generateSummaryReport(executionData, projectName, rootFolder);
            RawReport.generateSummaryReport(executionData, projectName, rootFolder);
        }

    }
}
