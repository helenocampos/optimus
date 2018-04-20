/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.extractor.model;

import io.github.helenocampos.extractor.AttributesExtractor;
import java.util.HashMap;

/**
 *
 *
 * @author helenocampos
 */
public class JavaTestClass extends JavaClass {

    private HashMap<String,ClassMethod> methods = null;
    
    public JavaTestClass(String className, String path, String packageName) {
        super(className, path, packageName);
        this.methods = AttributesExtractor.parseClass(path).getMethods();
    }
    
    @Override
    public String getType()
    {
        return "Test class";
    }

    public String toString(){
        return this.getClassName();
    }

    public ClassMethod getMethodByName(String methodName)
    {
        return this.methods.get(methodName);
    }
    
    public HashMap<String, ClassMethod> getMethods(){
        return this.methods;
    }
}
