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
import io.github.helenocampos.surefire.ordering.PrioritizationTechniques;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
        pomManager = new PomManager(this.getMavenProject().getBasedir().getAbsolutePath());
        addJacocoPlugin();

        if (getExperimentType().equals("mutation")) {
            executeMutationExperiment();
        } else if (getExperimentType().equals("versions")) {
            executeVersionsExperiment();
        } else if (getExperimentType().equals("local")) {
            executeLocalExperiment();
        }
    }
    
    private void executeLocalExperiment() throws MojoExecutionException {
        Model pom = pomManager.readPom(this.getMavenProject().getBasedir().getAbsolutePath());
        logMessage("Collecting coverage data");
        collectCoverageData(this.getMavenProject().getBasedir());
        executeTechniques(this.getMavenProject().getBasedir(), true);
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
                    logMessage("Initializing experiment for " + version.getName());

                    logMessage("Collecting coverage and faults data");
                    collectCoverageAndGenerateFaultsFile(version);
                    executeTechniques(version, false);
                }
            }
            ReportsController reports = new ReportsController(this.getMavenProject().getName(), versionsFolderFile, this.getReports(), "multiple");
        }
    }

    private List<File> getVersionsFolders(File versionFolder) {
        List<File> versions = new LinkedList<File>();
        if (versionFolder.isDirectory()) {
            versions.addAll(Arrays.asList(versionFolder.listFiles()));
        }
        return versions;
    }

    private void executeTechniques(File projectFolder, boolean generateFaultsFile) throws MojoExecutionException {
        if (this.getPrioritizationTechniques() != null) {
            for (String technique : this.getPrioritizationTechniques()) {
                invokePrioritization(technique, projectFolder, generateFaultsFile, true);
            }
        } else {
            if (this.getPrioritization().equals("all")) {
                for (String technique : PrioritizationTechniques.getAllTechniquesNames()) {
                    invokePrioritization(technique, projectFolder, generateFaultsFile, true);
                }
            } else {
                invokePrioritization(this.getPrioritization(), projectFolder, generateFaultsFile, true);
            }

        }
    }

    private void collectCoverageAndGenerateFaultsFile(File projectFolder) throws MojoExecutionException {
        invokePrioritization("default", projectFolder, true, false);
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
            collectCoverageData(outputExperimentFolder);
            executeTechniques(outputExperimentFolder, true);
        }
        logMessage("Generating reports");
        ReportsController reports = new ReportsController(this.getMavenProject().getName(), new File(getExperimentOutputDirectory(), timeStamp), this.getReports(), "multiple");
    }

    //executed each time when it is the first execution, so that coverage data can be gathered before prioritization
    private void collectCoverageData(File outputExperimentFolder) {
        pomManager.removeFramework(outputExperimentFolder.getAbsolutePath());
        pomManager.setupFirstRun(outputExperimentFolder.getAbsolutePath());
        Runtime rt = Runtime.getRuntime();
        invokeProcess(rt, outputExperimentFolder, true);
    }

    private void logMessage(String message) {
        System.out.println("-------------------------");
        System.out.println("[OPTIMUS]" + message);
        System.out.println("-------------------------");
    }

    private void invokePrioritization(String technique, File outputExperimentFolder, boolean generateFaultsFile, boolean calcAPFD) throws MojoExecutionException {
        int executionsAmount = 1;
        if(technique.equals("random")){
            executionsAmount = Integer.valueOf(randomRepeat);
        }
        for (int i = 0; i < executionsAmount; i++) {
            long start = System.currentTimeMillis();
            logMessage("Executing tests with " + technique + " prioritization technique");
            Runtime rt = Runtime.getRuntime();
            pomManager.removeFramework(outputExperimentFolder.getAbsolutePath());
            pomManager.setupPrioritizationPlugin(getPrioritizationConfig(technique, outputExperimentFolder.getAbsolutePath(), outputExperimentFolder.getName(), generateFaultsFile, calcAPFD));
            invokeProcess(rt, outputExperimentFolder, false);
            long finish = System.currentTimeMillis();
            double time = (double) (finish - start) / 1000;
            logMessage("Execution took " + time + " seconds.");
        }
    }

    private PrioritizationConfig getPrioritizationConfig(String technique, String projectFolder, String projectName, boolean generateFaultsFile, boolean calcAPFD){
        PrioritizationConfig config = new PrioritizationConfig();
        config.setGranularity(this.getGranularity());
        config.setTechnique(technique);
        config.setProjectFolder(projectFolder);
        config.setDbPath(this.getDbPath());
        config.setProjectName(projectName);
        config.setCalcAPFD(calcAPFD);
        config.setGenerateFaultsFile(generateFaultsFile);
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
                Process pr = rt.exec(mvnInvokation, new String[0], folder);
                pr.waitFor();
            }
        } catch (IOException ex) {
            Logger.getLogger(ExperimentMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ExperimentMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
