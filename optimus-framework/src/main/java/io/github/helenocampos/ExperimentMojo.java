package io.github.helenocampos;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.sun.javafx.PlatformUtil;
import io.github.helenocampos.surefire.ordering.techniques.PrioritizationTechniques;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "experiment", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresDependencyResolution = ResolutionScope.TEST)

public class ExperimentMojo
        extends OptimusMojo {

    /**
     * @return the techniquesRepeat
     */
    public String getTechniquesRepeat() {
        return randomRepeat;
    }

    @Parameter(defaultValue = "1", readonly = true)
    private String executionTimes;

    @Parameter(defaultValue = "1", readonly = true)
    private String randomRepeat;

    private final String MVN_CLEAN_TEST = "mvn clean test";
    private final String MVN_TEST = "mvn test";

    public void execute()
            throws MojoExecutionException {
        this.pomManager = new PomManager(this.getMavenProject().getBasedir().getAbsolutePath());
        addJacocoPlugin();

        if (getExperimentType().equals("mutation")) {
            executeMutationExperiment();
            manageSourceCodeBackup(this.getMavenProject().getBasedir().getAbsolutePath());
        } else if (getExperimentType().equals("versions")) {
            executeVersionsExperiment();
        } else if (getExperimentType().equals("local")) {
            executeLocalExperiment();
            manageSourceCodeBackup(this.getMavenProject().getBasedir().getAbsolutePath());
        }
        
    }

    private void executeLocalExperiment() throws MojoExecutionException {
        Model pom = pomManager.readPom(this.getMavenProject().getBasedir().getAbsolutePath());
        logMessage("Collecting coverage data");
        collectCoverageAndGenerateFaultsFile(this.getMavenProject().getBasedir());
        executeTechniques(this.getMavenProject().getBasedir(), false);
        logMessage("Generating reports");
        ReportsController reports = new ReportsController(this.getMavenProject().getName(), this.getMavenProject().getBasedir(), this.getReports(), "single");
        pomManager.writePom(pom, this.getMavenProject().getBasedir().getAbsolutePath());
    }

    private void executeVersionsExperiment() throws MojoExecutionException {
        if (this.getVersionsFolder() != null) {

            File versionsFolderFile = new File(this.getVersionsFolder());
            List<File> versions = getVersionsFolders(versionsFolderFile);
            for (File version : versions) {
                if (version.isDirectory()) {
                    this.pomManager = new PomManager(version.getAbsolutePath());
                    logMessage("Initializing experiment for " + version.getName());

                    logMessage("Collecting coverage and faults data");
                    collectCoverageAndGenerateFaultsFile(version);
                    executeTechniques(version, false);
                }
                manageSourceCodeBackup(version.getAbsolutePath());
            }
            ReportsController reports = new ReportsController(this.getMavenProject().getName(), versionsFolderFile, this.getReports(), "multiple");
        }
    }

    private List<File> getVersionsFolders(File versionFolder) {
        List<File> versions = new LinkedList<File>();
        if (versionFolder.isDirectory()) {
            versions.addAll(Arrays.asList(versionFolder.listFiles()));
        }
        Collections.sort(versions, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;
                try {
                    int s = name.indexOf('-') + 1;
                    int e = name.length();
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch (Exception e) {
                    i = 0;
                }
                return i;
            }
        });
        return versions;
    }

    private void executeTechniques(File projectFolder, boolean generateFaultsFile) throws MojoExecutionException {
        boolean simulateExecution = Boolean.valueOf(getSimulateExecution());
        if (this.getPrioritizationTechniques() != null) {
            for (String technique : this.getPrioritizationTechniques()) {
                invokePrioritization(technique, projectFolder, generateFaultsFile, true, false, false, simulateExecution);
            }
        } else {
            if (this.getPrioritization().equals("all")) {
                for (String technique : PrioritizationTechniques.getAllTechniquesNames()) {
                    invokePrioritization(technique, projectFolder, generateFaultsFile, true, false, false, simulateExecution);
                }
            } else {
                invokePrioritization(this.getPrioritization(), projectFolder, generateFaultsFile, true, false, false, simulateExecution);
            }

        }
    }

    private void collectCoverageAndGenerateFaultsFile(File projectFolder) throws MojoExecutionException {
        invokePrioritization("default", projectFolder, true, false, true, true, false);
    }

    private void executeMutationExperiment() throws MojoExecutionException {
        String timeStamp = new SimpleDateFormat("HHmmss-ddMMyyyy").format(new Date());
        int execTimes = Integer.valueOf(executionTimes);
        for (int x = 1; x <= execTimes; x++) {
            logMessage("Experiment run #" + x);
            logMessage("Generating code mutants");
            runPitestPlugin();
            logMessage("Injecting faults");
            runFaultInjectionPlugin(timeStamp, Integer.toString(x));

            File outputExperimentFolder = new File(new File(getExperimentOutputDirectory(), timeStamp).getAbsolutePath(), Integer.toString(x));
            outputExperimentFolder = new File(outputExperimentFolder, getMavenProject().getName());
            logMessage("Collecting coverage data");
            collectCoverageAndGenerateFaultsFile(outputExperimentFolder);
            executeTechniques(outputExperimentFolder, false);
        }
        logMessage("Generating reports");
        ReportsController reports = new ReportsController(this.getMavenProject().getName(), new File(getExperimentOutputDirectory(), timeStamp), this.getReports(), "multiple");
    }

    private void logMessage(String message) {
        System.out.println("-------------------------");
        System.out.println("[OPTIMUS]" + message);
        System.out.println("-------------------------");
    }

    private void invokePrioritization(String technique, File outputExperimentFolder, boolean generateFaultsFile, boolean calcAPFD, boolean collectCoverage, boolean cleanProject, boolean simulateExecution) throws MojoExecutionException {
        int executionsAmount = 1;
        if (technique.equals("random")) {
            executionsAmount = Integer.valueOf(randomRepeat);
        }
        for (int i = 0; i < executionsAmount; i++) {
            long start = System.currentTimeMillis();
            String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logMessage("Executing tests with " + technique + " prioritization technique. Started at: " + timeStamp);
            Runtime rt = Runtime.getRuntime();
            pomManager.removeFramework(outputExperimentFolder.getAbsolutePath());
            pomManager.setupPrioritizationPlugin(getPrioritizationConfig(technique, outputExperimentFolder.getAbsolutePath(), generateFaultsFile, calcAPFD, collectCoverage, simulateExecution));
            invokeProcess(rt, outputExperimentFolder, cleanProject);
            long finish = System.currentTimeMillis();
            double time = (double) (finish - start) / 1000;
            logMessage("Execution took " + time + " seconds.");
        }
    }

    private PrioritizationConfig getPrioritizationConfig(String technique, String projectFolder, boolean generateFaultsFile, boolean calcAPFD, boolean collectCoverage, boolean simulateExecution) {
        PrioritizationConfig config = new PrioritizationConfig();
        config.setGranularity(this.getGranularity());
        config.setTechnique(technique);
        config.setProjectFolder(projectFolder);
        config.setDbPath(this.getDbPath());
        config.setProjectName(PomManager.getProjectId(projectFolder));
        config.setCalcAPFD(calcAPFD);
        config.setGenerateFaultsFile(generateFaultsFile);
        config.setClustersAmount(this.getClustersAmount());
        config.setCollectCoverageData(collectCoverage);
        config.setSimulateExecution(simulateExecution);
        return config;
    }

    private void invokeProcess(Runtime rt, File folder, boolean clean) {
        try {
            String mvnInvokation;
            if (clean) {
                mvnInvokation = MVN_CLEAN_TEST;
            } else {
                mvnInvokation = MVN_TEST;
            }
            if (PlatformUtil.isWindows()) {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", mvnInvokation);
                pb.directory(folder);
                Process p = pb.start();
                StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", Boolean.valueOf(getPrintLogs()));
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", Boolean.valueOf(getPrintLogs()));
                errorGobbler.start();
                outputGobbler.start();
                p.waitFor();
            } else {
//                Process pr = rt.exec(mvnInvokation, new String[0], folder);
//                pr.waitFor();
                ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", mvnInvokation);
                pb.directory(folder);
                Process p = pb.start();
                StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", Boolean.valueOf(getPrintLogs()));
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", Boolean.valueOf(getPrintLogs()));
                errorGobbler.start();
                outputGobbler.start();
                p.waitFor();
            }
        } catch (IOException ex) {
            Logger.getLogger(ExperimentMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ExperimentMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
