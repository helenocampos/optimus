/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.executiontraceanalyzer.cobertura;

import com.thoughtworks.xstream.XStream;
import java.io.File;

/**
 *
 * @author helenocampos
 */
public class CoberturaParser
{

    public static CoberturaCoverage parse(File file)
    {
        CoberturaCoverage coverage = new CoberturaCoverage();
        if (file.exists())
        {
            XStream xstream = new XStream();
            xstream.alias("coverage", CoberturaCoverage.class);
            xstream.alias("source", CoberturaSource.class);
            xstream.alias("package", CoberturaPackage.class);
            xstream.alias("class", CoberturaClass.class);
            xstream.alias("method", CoberturaMethod.class);
            xstream.alias("line", CoberturaLine.class);
            xstream.alias("condition", CoberturaCondition.class);

            xstream.useAttributeFor(CoberturaCoverage.class, "linesValid");
            xstream.aliasField("lines-valid", CoberturaCoverage.class, "linesValid");
            
            xstream.useAttributeFor(CoberturaClass.class, "name");
            xstream.aliasField("name", CoberturaClass.class, "name");
            
            xstream.useAttributeFor(CoberturaLine.class, "number");
            xstream.aliasField("number", CoberturaLine.class, "number");
            
            xstream.useAttributeFor(CoberturaLine.class, "hits");
            xstream.aliasField("hits", CoberturaLine.class, "hits");
            
            xstream.useAttributeFor(CoberturaMethod.class, "name");
            xstream.aliasField("name", CoberturaMethod.class, "name");

            coverage = (CoberturaCoverage) xstream.fromXML(file);
        }
        return coverage;
    }

}
