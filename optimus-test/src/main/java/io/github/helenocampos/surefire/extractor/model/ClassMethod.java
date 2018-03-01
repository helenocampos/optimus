/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.extractor.model;

import java.util.HashMap;

/**
 *
 * @author helenocampos This class is used to represent a method of a class from
 * the source code being tested. It is intended to be created when extracting
 * info from the source code.
 */
public class ClassMethod
{

    private String name;
    private Coverage coverage;

    public ClassMethod(String methodDeclaration)
    {
        this.name = methodDeclaration;
        this.coverage = new Coverage();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return this.name;
    }

    public Coverage getCoverage()
    {
        return coverage;
    }

    public void setCoverage(Coverage coverage)
    {
        this.coverage = coverage;
    }

    
}
