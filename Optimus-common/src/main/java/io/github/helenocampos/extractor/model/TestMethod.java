/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.extractor.model;

/**
 *
 * @author helenocampos This class is used to represent a JUnit test method. It is intended to be created when extracting
 * info from the source code.
 */
public class TestMethod
{

    private String name;
    private Coverage coverage;

    public TestMethod(String methodDeclaration)
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
