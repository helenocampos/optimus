/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.executiontraceanalyzer.cobertura;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author helenocampos
 */
@XStreamAlias("coverage")
public class CoberturaCoverage
{

    @XStreamAlias("sources")
    List<CoberturaSource> sources = new ArrayList<>();

    @XStreamAlias("packages")
    List<CoberturaPackage> packages = new ArrayList<>();

    String linesValid;

    private HashMap<String, List<CoberturaLine>> classesIndex;

    public void indexClasses()
    {
        if(classesIndex==null){
            classesIndex = new HashMap<>();
        }
        for (CoberturaPackage packageEntry : packages)
        {
            for(CoberturaClass classEntry: packageEntry.classes){
                this.getClassesIndex().put(classEntry.name, classEntry.lines);
            }
        }
    }

    public HashMap<String, List<CoberturaLine>> getClassesIndex()
    {
        return classesIndex;
    }

    public void setClassesIndex(HashMap<String, List<CoberturaLine>> classesIndex)
    {
        this.classesIndex = classesIndex;
    }
}
