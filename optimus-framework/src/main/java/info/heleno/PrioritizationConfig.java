/*
 * To change this license header; choose License Headers in Project Properties.
 * To change this template file; choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno;

/**
 *
 * @author Heleno
 */
public class PrioritizationConfig {

    private String granularity;
    private String technique;
    private String projectFolder;
    private String dbPath;
    private String clustersAmount;
    private String projectName;
    private boolean generateFaultsFile;
    private boolean collectCoverageData;
    private boolean calcAPFD;
    private boolean simulateExecution;
    private boolean firstVersionExecution;
    
    public PrioritizationConfig(String granularity, String technique, String projectFolder, String dbPath, String projectName, boolean generateFaultsFile, boolean calcAPFD, boolean simulateExecution) {
        this.granularity = granularity;
        this.technique = technique;
        this.projectFolder = projectFolder;
        this.dbPath = dbPath;
        this.projectName = projectName;
        this.generateFaultsFile = generateFaultsFile;
        this.calcAPFD = calcAPFD;
        this.simulateExecution = simulateExecution;
    }

    public PrioritizationConfig() {
    }

    /**
     * @return the granularity
     */
    public String getGranularity() {
        return granularity;
    }

    /**
     * @param granularity the granularity to set
     */
    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    /**
     * @return the technique
     */
    public String getTechnique() {
        return technique;
    }

    /**
     * @param technique the technique to set
     */
    public void setTechnique(String technique) {
        this.technique = technique;
    }

    /**
     * @return the projectFolder
     */
    public String getProjectFolder() {
        return projectFolder;
    }

    /**
     * @param projectFolder the projectFolder to set
     */
    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    /**
     * @return the dbPath
     */
    public String getDbPath() {
        return dbPath;
    }

    /**
     * @param dbPath the dbPath to set
     */
    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    /**
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return the generateFaultsFile
     */
    public boolean isGenerateFaultsFile() {
        return generateFaultsFile;
    }

    /**
     * @param generateFaultsFile the generateFaultsFile to set
     */
    public void setGenerateFaultsFile(boolean generateFaultsFile) {
        this.generateFaultsFile = generateFaultsFile;
    }

    /**
     * @return the calcAPFD
     */
    public boolean isCalcAPFD() {
        return calcAPFD;
    }

    /**
     * @param calcAPFD the calcAPFD to set
     */
    public void setCalcAPFD(boolean calcAPFD) {
        this.calcAPFD = calcAPFD;
    }

    public String getClustersAmount()
    {
        return clustersAmount;
    }

    public void setClustersAmount(String clustersAmount)
    {
        this.clustersAmount = clustersAmount;
    }

    public boolean isCollectCoverageData()
    {
        return collectCoverageData;
    }

    public void setCollectCoverageData(boolean collectCoverageData)
    {
        this.collectCoverageData = collectCoverageData;
    }

    public boolean isSimulateExecution()
    {
        return simulateExecution;
    }

    public void setSimulateExecution(boolean simulateExecution)
    {
        this.simulateExecution = simulateExecution;
    }

    public boolean isFirstVersionExecution()
    {
        return firstVersionExecution;
    }

    public void setFirstVersionExecution(boolean firstVersionExecution)
    {
        this.firstVersionExecution = firstVersionExecution;
    }
}
