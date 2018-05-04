/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.junit4;

import io.github.helenocampos.extractor.model.ClassMethod;
import io.github.helenocampos.extractor.model.JavaClass;
import io.github.helenocampos.extractor.model.JavaTestClass;
import io.github.helenocampos.extractor.model.ProjectData;
import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.LineData;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author helenocampos
 */
public class CoberturaListener extends RunListener
{

    private String projectPath;
    private io.github.helenocampos.extractor.model.ProjectData projectData;

    public CoberturaListener(String projectPath, ProjectData projectData)
    {
        super();
        this.projectPath = projectPath;
        this.projectData = projectData;
    }

    @Override
    public void testFinished(Description description) throws Exception
    {
        super.testFinished(description);
        JavaTestClass testClass = projectData.getTestClassByName(description.getClassName());
        if (testClass != null)
        {
            ClassMethod testMethod = testClass.getMethodByName(description.getMethodName());
            if (testMethod != null)
            {
                Path coberturaFilePath = Paths.get(projectPath, "target", "cobertura", "cobertura.ser");
                File coberturaFile = coberturaFilePath.toFile();
                net.sourceforge.cobertura.coveragedata.ProjectData coberturaProjectData = CoverageDataFileHandler.loadCoverageData(coberturaFile);
                if (coberturaProjectData != null)
                {
                    Collection<ClassData> classes = coberturaProjectData.getClasses();
                    for (ClassData clazz : classes)
                    {
                        String statementsHits = "";
                        for (CoverageData covData : clazz.getLines())
                        {
                            storeCoveredClassSize(clazz.getName(), clazz.getLines().size());
                            LineData lineData = (LineData) covData;
                            statementsHits = statementsHits + lineData.getHits() + ",";
                        }
                        if (!statementsHits.equals(""))
                        {
                            testMethod.getCoverage().addLinesHit(clazz.getName(), statementsHits);
                        }
                    }
                }
            }
        }
        deleteCoberturaDataFile();
    }

    private void storeCoveredClassSize(String className, int size)
    {
        JavaClass coveredClass = projectData.getClassByName(className);
        if (coveredClass != null)
        {
            coveredClass.setExecutableLines(size);
        }
    }

    private void initializeCoberturaFile()
    {
        net.sourceforge.cobertura.coveragedata.ProjectData.saveGlobalProjectData();
    }

    private void deleteCoberturaDataFile()
    {
        File defaultFile = CoverageDataFileHandler.getDefaultDataFile();
        if (defaultFile.exists())
        {
            defaultFile.delete();
            initializeCoberturaFile();
        }
    }

    public void writeProjectDataFile()
    {
        projectData.writeProjectDataFile();
    }
}
