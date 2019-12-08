/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno;

import java.io.File;

/**
 *
 * @author Heleno
 */
public class ReportsGenerator {

    public static void main(String[] args) {

        String projectName = "CoreNLP";
        File rootFolder = new File("C:\\Users\\Heleno\\Documents\\dissertation\\coreNLP_clean_method");

        ReportsController reports = new ReportsController(projectName, rootFolder, null, "multiple");

    }
}
