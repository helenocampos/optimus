/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.junit4;

import io.github.helenocampos.surefire.extractor.model.ClassMethod;
import io.github.helenocampos.surefire.extractor.model.JavaClass;
import io.github.helenocampos.surefire.extractor.model.JavaTestClass;
import io.github.helenocampos.surefire.extractor.model.ProjectData;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import org.jacoco.agent.rt.RT;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author helenocampos
 */
public class CoverageListener extends RunListener
{

    private String projectPath;

    public CoverageListener(String projectPath)
    {
        super();
        this.projectPath = projectPath;
    }

    public void testFinished(Description description) throws Exception
    {
        super.testFinished(description);
        ProjectData projectData = ProjectData.getProjectDataFromFile();
        JavaTestClass testClass = projectData.getTestClassByName(description.getClassName());
        if (testClass != null)
        {
            ClassMethod testMethod = testClass.getMethodByName(description.getMethodName());
            if (testMethod != null)
            {
                final ByteArrayInputStream buffer = new ByteArrayInputStream(RT.getAgent().getExecutionData(true));
                ExecutionDataReader reader = new ExecutionDataReader(buffer);
                reader.setSessionInfoVisitor(new ISessionInfoVisitor()
                {
                    ProjectData projectData;

                    public void visitSessionInfo(final SessionInfo info)
                    {
//                System.out.printf("Session \"%s\": %s - %s%n", info.getId(), new Date(
//                        info.getStartTimeStamp()),
//                        new Date(info.getDumpTimeStamp()));
                    }

                    public ISessionInfoVisitor setParams(ProjectData projectData)
                    {
                        this.projectData = projectData;
                        return this;
                    }
                }.setParams(projectData));
                reader.setExecutionDataVisitor(new IExecutionDataVisitor()
                {
                    ProjectData projectData;
                    ClassMethod testMethod;

                    public void visitClassExecution(final ExecutionData data)
                    {
                        JavaClass clazz = projectData.getClassByCoverageName(data.getName());
                        if (clazz != null)
                        {
                            testMethod.getCoverage().addStatementCoverage(data.getName(), data.getProbes());
                            final CoverageBuilder coverageBuilder = new CoverageBuilder();
                            ExecutionDataStore store = new ExecutionDataStore();
                            store.put(data);
                            final Analyzer analyzer = new Analyzer(
                                    store, coverageBuilder);
                            try
                            {
                                analyzer.analyzeAll(new File(clazz.getClassFilePath()));
                                for (IClassCoverage classCoverage : coverageBuilder.getClasses())
                                {
                                    boolean[] branchProbes = new boolean[classCoverage.getBranchCounter().getTotalCount()];
                                    int currentBranchIndex = 0;
                                    if (branchProbes.length != 0)
                                    {
                                        for (int x = 0; x < classCoverage.getLastLine(); x++)
                                        {
                                            ILine line = classCoverage.getLine(x);
                                            if (line.getBranchCounter().getTotalCount() != 0)
                                            {
                                                branchProbes[currentBranchIndex++] = line.getBranchCounter().getCoveredCount() != 0;
                                            }
                                        }
                                    }
                                    testMethod.getCoverage().addBranchCoverage(data.getName(), branchProbes);

                                    boolean[] methodProbes = new boolean[classCoverage.getMethodCounter().getTotalCount()];
                                    int currentMethodIndex = 0;
                                    for (IMethodCoverage methodCoverage : classCoverage.getMethods())
                                    {
                                        methodProbes[currentMethodIndex++] = methodCoverage.getMethodCounter().getCoveredCount() != 0;
                                    }
                                    testMethod.getCoverage().addMethodCoverage(data.getName(), methodProbes);

                                }
                            } catch (IOException ex)
                            {

                            }
                        }

                    }

                    public IExecutionDataVisitor setParams(ProjectData projectData, ClassMethod testMethod)
                    {
                        this.projectData = projectData;
                        this.testMethod = testMethod;
                        return this;
                    }
                }.setParams(projectData, testMethod));
                reader.read();
                projectData.writeProjectDataFile();
            }
        }
    }
}
