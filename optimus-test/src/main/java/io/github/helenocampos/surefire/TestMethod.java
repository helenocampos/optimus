/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire;

import io.github.helenocampos.testing.AbstractTest;
import io.github.helenocampos.testing.TestGranularity;

/**
 *
 * @author helenocampos
 */
public class TestMethod extends AbstractTest
{
    private String methodName;
    
    @Override
    public TestGranularity getTestGranularity()
    {
        return TestGranularity.METHOD;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    @Override
    public String getTestName()
    {
       return this.getMethodName();
    }
    
    @Override
    public String toString(){
        return this.getTestClass().getSimpleName()+"."+this.getMethodName();
    }

    @Override
    public String getQualifiedName()
    {
        return this.getTestClass().getName()+"."+this.getTestName();
    }

}
