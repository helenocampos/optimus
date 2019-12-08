/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.PlainTextOutput;
import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.Method;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author helenocampos
 */
// this class represents a .class file in the project being tested
public class ProjectClass
{

    private String qualifiedName;
    private String absolutePath;
    private List<ProjectClassMutant> mutants;
    private List<String> methods;
    private List<String> sourceCodeText;
    private List<String> generatedMutant;

    public ProjectClass(String qualifiedName, String absolutePath, File classFile)
    {
        this.qualifiedName = qualifiedName;
        this.absolutePath = absolutePath;
        this.mutants = new LinkedList<ProjectClassMutant>();
        this.methods = new LinkedList<String>();
        this.sourceCodeText = new LinkedList<String>();
        parseMethods();
        loadSourceCodeText(classFile);
    }

    private void parseMethods()
    {

        try
        {
            ClassParser parser = new ClassParser(absolutePath);
            com.sun.org.apache.bcel.internal.classfile.JavaClass cls = parser.parse();
            for (Method method : cls.getMethods())
            {
                this.methods.add(method.getName());
            }

        } catch (IOException ex)
        {
            Logger.getLogger(ProjectClass.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName)
    {
        this.qualifiedName = qualifiedName;
    }

    public String getAbsolutePath()
    {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath)
    {
        this.absolutePath = absolutePath;
    }

    public List<ProjectClassMutant> getMutants()
    {
        return mutants;
    }

    public void setMutants(List<ProjectClassMutant> mutants)
    {
        this.mutants = mutants;
    }

    public void addMutant(ProjectClassMutant mutant)
    {
        this.mutants.add(mutant);
    }

    public List<String> getMethods()
    {
        return methods;
    }

    public void setMethods(List<String> methods)
    {
        this.methods = methods;
    }

    public List<String> getSourceCodeText()
    {
        return sourceCodeText;
    }

    public void setSourceCodeText(List<String> sourceCodeText)
    {
        this.sourceCodeText = sourceCodeText;
    }

    public void loadSourceCodeText(File f){
        if (this.getSourceCodeText().isEmpty())
        {
            if (f.exists())
            {
                PlainTextOutput output = new PlainTextOutput();
                Decompiler.decompile(f.getAbsolutePath(), output);
                String[] lines = output.toString().split("\n");
                this.sourceCodeText.addAll(Arrays.asList(lines));
            }
        }
    }

    public List<String> getGeneratedMutant()
    {
        return generatedMutant;
    }

    public void setGeneratedMutant(List<String> generatedMutant)
    {
        this.generatedMutant = generatedMutant;
    }
}
