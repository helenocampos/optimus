/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.executiontraceanalyzer.cobertura;

import java.io.File;

/**
 *
 * @author helenocampos
 */
public class App
{
    public static void main(String[] args){
        String file = "/Users/helenocampos/Dropbox/SHARED_PC_NENC/Mestrado/RTO/optimus/MockProject2/target/site/cobertura/coverage.xml";
        CoberturaParser.parse(new File(file));
    }
}
