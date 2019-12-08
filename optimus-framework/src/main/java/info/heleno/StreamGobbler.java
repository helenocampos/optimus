/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if(printLogs){
                    System.out.println("[DEBUG - INNER PROCESS] [" +this.type+"]"+  line);
                }
            }
            isr.close();
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}



