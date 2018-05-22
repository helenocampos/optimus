/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.optimusmodificationsanalyzer;

import io.github.helenocampos.extractor.AttributesExtractor;
import io.github.helenocampos.extractor.model.JavaSourceCodeClass;
import io.github.helenocampos.extractor.model.ModificationsGranularity;
import io.github.helenocampos.extractor.model.ProjectData;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author helenocampos
 */
public class ModificationsAnalyzer
{

    private String sourceBackupPath;
    private ProjectData projectData;

    public ModificationsAnalyzer()
    {
        this.projectData = ProjectData.getProjectDataFromFile();
        if (projectData != null)
        {
            this.sourceBackupPath = projectData.getSourceBackupPath();
        }
    }

    public ModificationsAnalyzer(ProjectData projectData)
    {
        this.projectData = projectData;
        if (projectData != null)
        {
            this.sourceBackupPath = projectData.getSourceBackupPath();
        }
    }

    public Set<String> getModifiedElements(ModificationsGranularity modificationsGranularity)
    {
        Set<String> modifiedElements = new HashSet<>();
        if (backupExists())
        {
            for (String key : projectData.getClasses().keySet())
            {
                JavaSourceCodeClass originalClass = projectData.getClasses().get(key);
                String currentVersion = originalClass.getPath();
                String oldVersion = sourceBackupPath + currentVersion.substring(currentVersion.indexOf("src/") + 3);
                if (modificationsGranularity.equals(ModificationsGranularity.METHOD))
                {
                    List<String> methods = AttributesExtractor.parseClass(currentVersion).getMethodsNames();
                    for (String method : methods)
                    {
                        ProgramElement element = new ProgramElement(method, oldVersion, currentVersion, modificationsGranularity);
                        if (element.getDeltas().size() > 0)
                        {
                            modifiedElements.add(key + "." + method);
                        }
                    }
                } else
                {
                    ProgramElement element = new ProgramElement(key, oldVersion, currentVersion, modificationsGranularity);
                    if (element.getDeltas().size() > 0)
                    {
                        modifiedElements.add(key);
                    }
                }
            }
        }
        return modifiedElements;
    }
    
    private boolean backupExists(){
        File backupFolder = new File(sourceBackupPath);
        return backupFolder.exists();
    }

    public int getAmountOfModifiedLines(String elementName, ModificationsGranularity modificationsGranularity)
    {
        int modifiedLines = 0;
        ProgramElement element = null;
        for (String key : projectData.getClasses().keySet())
        {
            JavaSourceCodeClass originalClass = projectData.getClasses().get(key);
            String currentVersion = originalClass.getPath();
            String oldVersion = sourceBackupPath + currentVersion.substring(currentVersion.indexOf("src/") + 3);
            if (modificationsGranularity.equals(ModificationsGranularity.METHOD))
            {
                List<String> methods = AttributesExtractor.parseClass(currentVersion).getMethodsNames();
                for (String method : methods)
                {
                    if (method.equals(elementName))
                    {
                        element = new ProgramElement(method, oldVersion, currentVersion, modificationsGranularity);
                    }
                }
            } else
            {
                if (key.equals(elementName))
                {
                    element = new ProgramElement(key, oldVersion, currentVersion, modificationsGranularity);
                }
            }
        }
        if (element != null)
        {
            modifiedLines = element.getDeltas().size();
        }
        return modifiedLines;
    }
}
