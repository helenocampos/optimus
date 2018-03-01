/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.extractor.model;

/**
 *
 * @author helenocampos
 */
public class JavaSourceCodeClass extends JavaClass{
    
    public JavaSourceCodeClass(String className, String path, String projectBasePath, String packageName) {
        super(className, path, projectBasePath, packageName);
        
    }

    @Override
    public String getType()
    {
        return "Source code class";
    }
    
    public String toString(){
        return this.getClassName();
    }
}
