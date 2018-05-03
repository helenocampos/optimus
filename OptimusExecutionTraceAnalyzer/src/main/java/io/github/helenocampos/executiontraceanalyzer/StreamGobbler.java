/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.executiontraceanalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 *
 * @author Heleno
 */
public class StreamGobbler extends Thread {

    private InputStream is;
    private String type;
    private boolean printLogs;

    StreamGobbler(InputStream is, String type, boolean printLogs) {
        this.is = is;
        this.type = type;
        this.printLogs = printLogs;
    }

    // empties buffers
    public void run() {
        try {
            Logger log = Logger.getLogger(StreamGobbler.class.getName());
            log.addHandler(new StreamHandler(System.out, new SimpleFormatter()));
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if(printLogs){
                    log.log(Level.INFO, "[DEBUG - INNER PROCESS] [" +this.type+"]"+  line);
//                    System.out.println("[DEBUG - INNER PROCESS] [" +this.type+"]"+  line);
                }
            }
            isr.close();
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}



