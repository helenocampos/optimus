/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.extractor.model;

/**
 *
 * @author helenocampos
 */
public abstract class JavaClass{

    private String classFileName;
    private String path;
    private String packageName;
    private String classFilePath;
    private int executableLines;
    private int executableBranches;
    private int executableMethods;


    public JavaClass(String className, String path, String packageName) {
        this.classFileName = className;
        this.path = path;
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

    public int getExecutableLines()
    {
        return executableLines;
    }

    public void setExecutableLines(int executableLines)
    {
        this.executableLines = executableLines;
    }

    public int getExecutableBranches()
    {
        return executableBranches;
    }

    public void setExecutableBranches(int executableBranches)
    {
        this.executableBranches = executableBranches;
    }

    public int getExecutableMethods()
    {
        return executableMethods;
    }

    public void setExecutableMethods(int executableMethods)
    {
        this.executableMethods = executableMethods;
    }
    
    public int getExecutableElements(CoverageGranularity granularity){
        switch(granularity){
            case STATEMENT: return executableLines;
            case BRANCH: return executableBranches;
            case METHOD: return executableMethods;
            default: return 0;
        }
    }
}
