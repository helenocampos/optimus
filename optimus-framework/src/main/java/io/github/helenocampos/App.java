/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class App
{
    public static void main(String[] args){
        File experimentFolder = new File("/Users/helenocampos/Documents/out/143059-28022018");
        String projectName = "MockProject2";
        List<String> reports = new ArrayList<String>();
        ExperimentReport report = new ExperimentReport(projectName, experimentFolder, null);
    }
}
