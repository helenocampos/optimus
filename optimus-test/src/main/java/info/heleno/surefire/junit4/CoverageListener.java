/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire.junit4;

import info.heleno.extractor.model.TestMethod;
import info.heleno.extractor.model.JavaClass;
import info.heleno.extractor.model.JavaTestClass;
import info.heleno.extractor.model.ProjectData;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import org.jacoco.agent.rt.RT;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author helenocampos
 */
public class CoverageListener extends RunListener
{

    private String projectPath;
    private ProjectData projectData;

    public CoverageListener(String projectPath, ProjectData projectData)
    {
        super();
        this.projectPath = projectPath;
        this.projectData = projectData;
    }

    public void testFinished(Description description) throws Exception
    {
        super.testFinished(description);
        JavaTestClass testClass = projectData.getTestClassByName(description.getClassName());
        if (testClass != null)
        {
            TestMethod testMethod = testClass.getMethodByName(description.getMethodName());
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
                    TestMethod testMethod;

                    public void visitClassExecution(final ExecutionData data)
                    {
                        JavaClass clazz = projectData.getClassByCoverageName(data.getName());
                        if (clazz != null)
                        {
                            testMethod.getCoverage().addStatementCoverage(data.getName(), data.getProbes());
                            clazz.setExecutableLines(data.getProbes().length);
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
                                    testMethod.getCoverage().addLinesCovered(data.getName(), getLineCoverageString(classCoverage));
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
                                        testMethod.getCoverage().addBranchCoverage(data.getName(), branchProbes);
                                        clazz.setExecutableBranches(branchProbes.length);
                                    }

                                    boolean[] methodProbes = new boolean[classCoverage.getMethodCounter().getTotalCount()];
                                    if (methodProbes.length != 0)
                                    {
                                        int currentMethodIndex = 0;
                                        for (IMethodCoverage methodCoverage : classCoverage.getMethods())
                                        {
                                            methodProbes[currentMethodIndex++] = methodCoverage.getMethodCounter().getCoveredCount() != 0;
                                        }
                                        testMethod.getCoverage().addMethodCoverage(data.getName(), methodProbes);
                                        clazz.setExecutableMethods(methodProbes.length);
                                    }
                                }
                            } catch (IOException ex)
                            {

                            }
                        }

                    }

                    public String getLineCoverageString(IClassCoverage classCoverage)
                    {
                        String linesCoveredString = "";
                        for (int x = 0; x < classCoverage.getLastLine(); x++)
                        {
                            ILine line = classCoverage.getLine(x);
                            if ((line.getStatus() == ICounter.PARTLY_COVERED) || (line.getStatus() == ICounter.FULLY_COVERED))
                            {
                                linesCoveredString = linesCoveredString + Integer.toString(x) + ",";
                            }
                        }
                        return linesCoveredString;
                    }

                    public IExecutionDataVisitor setParams(ProjectData projectData, TestMethod testMethod)
                    {
                        this.projectData = projectData;
                        this.testMethod = testMethod;
                        return this;
                    }
                }.setParams(projectData, testMethod));
                reader.read();

            }
        }
    }

    public void writeProjectDataFile()
    {
        projectData.writeProjectDataFile();
    }
}
