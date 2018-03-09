/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire;

import io.github.helenocampos.surefire.ordering.Granularity;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class TestClass extends AbstractTest
{
    //TODO: é realmente necessário? pode ser obtido através de clazz.getMethods
    private List<Method> methods;
    
    @Override
    public Granularity getTestGranularity()
    {
        return Granularity.CLASS;
    }

    public List<Method> getMethods()
    {
        return methods;
    }

    public void setMethods(List<Method> methods)
    {
        this.methods = methods;
    }
    
    public void addMethod(Method method)
    {
        this.methods.add(method);
    }

    @Override
    public String getTestName()
    {
        return this.getTestClass().getName();
    }

    @Override
    public String getQualifiedName()
    {
        return getTestName();
    }
}
