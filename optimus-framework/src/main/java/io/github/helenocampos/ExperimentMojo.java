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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "experiment", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresDependencyResolution = ResolutionScope.TEST)

public class ExperimentMojo
        extends OptimusMojo {

    @Parameter(defaultValue = "1", readonly = true)
    private String executionTimes;

    public void execute()
            throws MojoExecutionException {
        addJacocoPlugin();
        String timeStamp = new SimpleDateFormat("HHmmss-ddMMyyyy").format(new Date());

        int execTimes = Integer.valueOf(executionTimes);
        for (int x = 1; x <= execTimes; x++) {
            runPitestPlugin();
            runFaultInjectionPlugin(timeStamp, Integer.toString(x));

            File outputExperimentFolder = new File(new File(getExperimentOutputDirectory(), timeStamp).getAbsolutePath(), Integer.toString(x));
            outputExperimentFolder = new File(outputExperimentFolder, getMavenProject().getName());
            collectCoverageData(outputExperimentFolder);
            if (this.getPrioritizationTechniques() != null) {
                for (String technique : this.getPrioritizationTechniques()) {
                    invokePrioritization(technique, outputExperimentFolder);
                }
            } else {
                if (this.getPrioritization().equals("all")) {
                    for (String technique : PrioritizationTechniques.getAllTechniquesNames()) {
                        invokePrioritization(technique, outputExperimentFolder);
                    }
                } else {
                    invokePrioritization(this.getPrioritization(), outputExperimentFolder);
                }

            }
        }
        ExperimentReport report = new ExperimentReport(this.getMavenProject().getName(), new File(getExperimentOutputDirectory(), timeStamp), this.getReports());
    }

    //executed each time when it is the first execution, so that coverage data can be gathered before prioritization
    private void collectCoverageData(File outputExperimentFolder) {
        PomManager.removeFramework(outputExperimentFolder.getAbsolutePath());
        PomManager.setupFirstRun(outputExperimentFolder.getAbsolutePath());
        Runtime rt = Runtime.getRuntime();
        invokeProcess(rt, outputExperimentFolder);
    }

    private void invokePrioritization(String technique, File outputExperimentFolder) throws MojoExecutionException {
        Runtime rt = Runtime.getRuntime();
        PomManager.removeFramework(outputExperimentFolder.getAbsolutePath());
        PomManager.setupPrioritizationPlugin(this.getGranularity(), technique, outputExperimentFolder.getAbsolutePath());
        invokeProcess(rt, outputExperimentFolder);
    }

    private void invokeProcess(Runtime rt, File folder) {
        try {
            if (PlatformUtil.isWindows()) {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "mvn test");
                pb.directory(folder);
                Process p = pb.start();
                StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
                errorGobbler.start();
                outputGobbler.start();
                p.waitFor();
            } else {
                Process pr = rt.exec("mvn test", new String[0], folder);
                pr.waitFor();
            }
        } catch (IOException ex) {
            Logger.getLogger(ExperimentMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ExperimentMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
