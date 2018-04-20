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
@XStreamAlias("line")
public class CoberturaLine
{
    @XStreamAlias("conditions")
    private List<CoberturaCondition> conditions = new ArrayList<>();
    private String number;
    private String hits;

    public List<CoberturaCondition> getConditions()
    {
        return conditions;
    }

    public void setConditions(List<CoberturaCondition> conditions)
    {
        this.conditions = conditions;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public String getHits()
    {
        return hits;
    }

    public void setHits(String hits)
    {
        this.hits = hits;
    }
}
