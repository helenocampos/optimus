/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos;

import com.sun.javafx.PlatformUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author helenocampos
 */
public class App {

    public static void main(String[] args) {
        File experimentFolder = new File("C:\\Users\\Heleno\\Documents\\out\\132527-07032018\\1\\MockProject2");
        String projectName = "gameoflife-core";
//        List<String> reports = new ArrayList<String>();
//        ExperimentReport report = new ExperimentReport(projectName, experimentFolder, null);

        Runtime rt = Runtime.getRuntime();
        try {
            if (PlatformUtil.isWindows()) {

                String command = "mvn test";
                String changeDirectoryCommand = "cd";
                String projectFolder = "";
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
                pb.directory(experimentFolder);
                Process p = pb.start();

//                Process pr = rt.exec("cmd /C java -version", new String[0], experimentFolder);
                StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
                errorGobbler.start();
                outputGobbler.start();
                System.out.println("Exit code: " + p.waitFor());
            } else {
                Process pr = rt.exec("mvn test", new String[0], experimentFolder);
                pr.waitFor();
            }
        } catch (IOException ex) {
            Logger.getLogger(ExperimentMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ExperimentMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
