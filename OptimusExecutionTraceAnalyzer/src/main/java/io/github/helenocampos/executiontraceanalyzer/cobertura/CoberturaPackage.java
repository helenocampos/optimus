/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.executiontraceanalyzer.cobertura;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author helenocampos
 */
@XStreamAlias("package")
public class CoberturaPackage
{
    @XStreamAlias("classes")
    List<CoberturaClass> classes = new ArrayList<>();
}
