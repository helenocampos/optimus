/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno;

import java.io.File;

/**
 *
 * @author helenocampos
 */
public class ProjectClassMutant extends ProjectClass
{
    private MutantDetails details;
    
    
    public ProjectClassMutant(String qualifiedName, String absolutePath, MutantDetails mutantDetails, File classFile)
    {
        super(qualifiedName, absolutePath,classFile);
        this.details = mutantDetails;
    }

    public MutantDetails getDetails()
    {
        return details;
    }

    public void setDetails(MutantDetails details)
    {
        this.details = details;
    }
}
