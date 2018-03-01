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
public abstract class JavaClass{

    private String classFileName;
    private String path;
    private String projectBasePath;
    private String packageName;
    private String classFilePath;


    public JavaClass(String className, String path, String projectBasePath, String packageName) {
        this.classFileName = className;
        this.path = path;
        this.projectBasePath = projectBasePath;
        this.packageName = packageName;
    }
    
    public String getClassName() {
        return classFileName;
    }

    public void setClassName(String className) {
        this.classFileName = className;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    //returns packagename+className
    public String getQualifiedName(){
        return this.packageName+"."+getSimpleName();
    }
    
    public String getSimpleName(){
        return this.classFileName.replace(".java", "");
    }
    
    public String toString(){
        return this.getClassName();
    }

    public String getProjectBasePath() {
        return projectBasePath;
    }

    public void setProjectBasePath(String projectBasePath) {
        this.projectBasePath = projectBasePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    //returns the package name with '/' instead of '.' + the name of the class
    public String getCoverageName(){
        return getQualifiedName().replaceAll("\\.", "/");
    }
    
    public abstract String getType();
    
    public String getClassFilePath()
    {
        return classFilePath;
    }

    public void setClassFilePath(String classFilePath)
    {
        this.classFilePath = classFilePath;
    }
}
