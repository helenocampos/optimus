/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.testing;

/**
 *
 * @author helenocampos
 */
public abstract class AbstractTest
{
    private Class testClass;
    private String qualifiedName;
    private String classPath; //TODO: pode ser obtido através do classLoader?
    private String sourcePath;
    
    public abstract TestGranularity getTestGranularity();

    public Class getTestClass()
    {
        return testClass;
    }

    public void setTestClass(Class clazz)
    {
        this.testClass = clazz;
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName)
    {
        this.qualifiedName = qualifiedName;
    }

    public String getClassPath()
    {
        return classPath;
    }

    public void setClassPath(String classPath)
    {
        this.classPath = classPath;
    }

    public String getSourcePath()
    {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath)
    {
        this.sourcePath = sourcePath;
    }
    
    public abstract String getTestName();
    
}
