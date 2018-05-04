package io.github.helenocampos.surefire;

import io.github.helenocampos.testing.AbstractTest;
import io.github.helenocampos.extractor.LocalProjectCrawler;
import io.github.helenocampos.extractor.model.ProjectData;
import io.github.helenocampos.surefire.api.JUnitExecutor;
import io.github.helenocampos.surefire.junit4.APFDListener;
import io.github.helenocampos.surefire.junit4.ClassAPFDListener;
import io.github.helenocampos.surefire.junit4.CoberturaListener;
import io.github.helenocampos.surefire.junit4.MethodAPFDListener;
import io.github.helenocampos.surefire.junit4.CoverageListener;
import io.github.helenocampos.surefire.junit4.FaultsListener;
import io.github.helenocampos.surefire.junit4.HistoricalDataListener;
import io.github.helenocampos.surefire.junit4.JUnit4Executor;
import io.github.helenocampos.testing.Granularity;
import io.github.helenocampos.surefire.ordering.TestsSorter;
import io.github.helenocampos.surefire.report.ExecutionData;
import io.github.helenocampos.surefire.util.JUnit4TestChecker;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.surefire.common.junit3.JUnit3TestChecker;
import org.apache.maven.surefire.common.junit4.JUnit4RunListenerFactory;
import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ConsoleStream;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.RunOrderCalculator;
import org.apache.maven.surefire.util.ScanResult;
import org.apache.maven.surefire.util.TestsToRun;

public class OptimusProvider extends AbstractProvider
{

    private final ClassLoader testClassLoader;
    private final ProviderParameters providerParameters;
    private final RunOrderCalculator runOrderCalculator;
    private JUnitExecutor executor;
    private final ScanResult scanResult;
    private JUnit4TestChecker junit4TestChecker;
    private PojoAndJUnit3Checker v3testChecker;
    private List<org.junit.runner.notification.RunListener> customRunListeners;
    private boolean isJunit4 = false;
    private String testGranularity;
    private String prioritizationTechnique;
    private String projectPath;
    final ConsoleStream consoleStream;
    private boolean integrationTests;
    private boolean calculateAPFD = false;
    private boolean registerExecution = true;
    private String dbPath;
    private String projectName;
    private boolean generateFaultsFile = false;
    private boolean collectCoberturaData = false;
    private ProjectData projectData;
    private boolean collectCoverageData = true;

    public OptimusProvider(ProviderParameters booterParameters)
    {
        this.providerParameters = booterParameters;
        this.integrationTests = isIntegrationTest();
        this.testClassLoader = booterParameters.getTestClassLoader();
        this.scanResult = booterParameters.getScanResult();
        this.runOrderCalculator = booterParameters.getRunOrderCalculator();
        this.executor = null;
        this.customRunListeners = JUnit4RunListenerFactory.createCustomListeners(providerParameters.getProviderProperties().get("listener"));
        this.consoleStream = this.providerParameters.getConsoleLogger();
        this.projectPath = getProjectPath(providerParameters.getProviderProperties().get("testClassesDirectory"));

        this.junit4TestChecker = new JUnit4TestChecker(testClassLoader);
        LocalProjectCrawler crawler = new LocalProjectCrawler(this.projectPath);
        this.projectData = crawler.getProjectData();

        String exampleTestClassName = providerParameters.getProviderProperties().get("tc.0");
        if (exampleTestClassName != null)
        {
            try
            {
                Class exampleTestClass = testClassLoader.loadClass(exampleTestClassName);
                if (junit4TestChecker.accept(exampleTestClass))
                {
                    if (this.junit4TestChecker.isValidJUnit4Test(exampleTestClass))
                    {
                        this.isJunit4 = true;
                    }
                } else
                {
                    this.v3testChecker = new PojoAndJUnit3Checker(new JUnit3TestChecker(testClassLoader));
                }
                executor = new JUnit4Executor(booterParameters, customRunListeners);
            } catch (ClassNotFoundException e)
            {
            } finally
            {
                setupProperties();
                if (!integrationTests)
                {
                    if (collectCoverageData)
                    {
                        this.customRunListeners.add(new CoverageListener(this.projectPath, this.projectData));
                    }
                    if (this.calculateAPFD)
                    {
                        if (this.testGranularity.equals(Granularity.METHOD.getName()))
                        {
                            this.customRunListeners.add(new MethodAPFDListener(this.testGranularity));
                        } else
                        {
                            this.customRunListeners.add(new ClassAPFDListener(this.testGranularity));
                        }
                    }
                    if (this.registerExecution)
                    {
                        if (this.projectName != null)
                        {
                            this.customRunListeners.add(new HistoricalDataListener(this.testGranularity, this.dbPath, this.projectName));
                        }
                    }
                    if (this.generateFaultsFile && this.testGranularity != null)
                    {
                        this.customRunListeners.add(new FaultsListener(getAPFDListener()));
                    }
                }
                if (this.collectCoberturaData)
                {
                    this.customRunListeners.add(new CoberturaListener(this.projectPath, this.projectData));
                }
            }
        }
    }

    private boolean isIntegrationTest()
    {
        if (this.providerParameters != null)
        {
            if (this.providerParameters.getDirectoryScannerParameters() != null)
            {
                if (this.providerParameters.getDirectoryScannerParameters().getIncludes() != null)
                {
                    List<String> includes = this.providerParameters.getDirectoryScannerParameters().getIncludes();
                    for (String include : includes)
                    {
                        if (StringUtils.containsIgnoreCase(include, "integration"))
                        {
                            return true;
                        }

                    }
                }
            }
        }

        return false;
    }

    private void setupProperties()
    {
        Properties properties = System.getProperties();
        this.testGranularity = properties.getProperty("granularity");
        this.prioritizationTechnique = properties.getProperty("prioritization");
        this.dbPath = properties.getProperty("dbPath");
        this.collectCoverageData = Boolean.valueOf(properties.getProperty("collectCoverageData", "true"));
        this.collectCoberturaData = Boolean.valueOf(properties.getProperty("collectCoberturaData", "false"));
        if (this.testGranularity == null && this.prioritizationTechnique == null)
        {
            this.testGranularity = getProviderProperties("granularity", "class");
            this.prioritizationTechnique = getProviderProperties("prioritization", "");
            this.calculateAPFD = Boolean.valueOf(getProviderProperties("apfd", "false"));
            this.generateFaultsFile = Boolean.valueOf(getProviderProperties("faultsFile", "false"));
        }
        if (dbPath == null)
        {
            this.dbPath = getProviderProperties("dbPath", "");
            this.registerExecution = !dbPath.equals("");
            properties.setProperty("dbPath", this.dbPath);
        }
        this.projectName = getProviderProperties("projectName", null);
        if (this.projectName != null)
        {
            properties.setProperty("projectName", this.projectName);
        }
        properties.setProperty("clustersAmount", getProviderProperties("clustersAmount", null));
    }

    private String getProviderProperties(String property, String defaultValue)
    {
        return this.providerParameters.getProviderProperties().getOrDefault(property, defaultValue);
    }

    private TestsToRun scanClassPath()
    {
        if (isJunit4)
        {
            final TestsToRun testsToRun = scanResult.applyFilter(junit4TestChecker, testClassLoader);
            return runOrderCalculator.orderTestClasses(testsToRun);
        } else
        {
            final TestsToRun testsToRun = scanResult.applyFilter(v3testChecker, testClassLoader);
            return runOrderCalculator.orderTestClasses(testsToRun);
        }
    }

    public Iterable getSuites()
    {
        AbstractTestsToRun toRun = null;
        if (this.testGranularity.equals("method"))
        {
            toRun = new TestMethodsToRun(scanClassPath(), isJunit4);

        } else
        {
            toRun = new TestClassesToRun(scanClassPath());

        }
        return toRun;

    }

    private List<AbstractTest> orderTests(Iterable testSet)
    {
        List<AbstractTest> tests = new LinkedList<AbstractTest>();
        for (Object test : testSet)
        {
            tests.add((AbstractTest) test);
        }
        if (!integrationTests && tests.size() > 0)
        {
            TestsSorter sorter = new TestsSorter(consoleStream, projectName);
            tests = sorter.sort(tests, prioritizationTechnique, testGranularity);
        }
        return tests;
    }

    public RunResult invoke(Object forkTestSet) throws TestSetFailedException, ReporterException, InvocationTargetException
    {
        if (forkTestSet == null)
        {
            RunResult globalResult = new RunResult(0, 0, 0, 0);
            long startTime = System.currentTimeMillis();
            Iterable<AbstractTest> testsToRun = orderTests(getSuites());
            boolean hasExecutableTests = false;
            for (AbstractTest toRun : testsToRun)
            {
                hasExecutableTests = true;
                globalResult.aggregate(invokeExecutor(toRun));
            }
            long finishTime = System.currentTimeMillis();
            if (hasExecutableTests)
            {
                broadcastTestFinished();
                if (this.calculateAPFD)
                {
                    ExecutionData executionData;
                    APFDListener listener = getAPFDListener();
                    executionData = listener.calculateAPFD();
                    if (executionData != null)
                    {
                        executionData.setTechnique(prioritizationTechnique);
                        executionData.setTestGranularity(testGranularity);
                        executionData.setProjectPath(projectPath);
                        float executionTime = (float) (finishTime - startTime) / 1000;
                        executionData.setExecutionTime(executionTime);
                        executionData.setExecutionDate(new SimpleDateFormat("HH:mm:ss MM/dd/yyyy").format(new Date()));
                        executionData.writeExecutionData();
                    }
                }
            }
            return globalResult;
        }
        if (!(forkTestSet instanceof AbstractTest))
        {
            throw new TestSetFailedException("This test mode is not supported yet");
        }
        if (executor == null)
        {
            throw new TestSetFailedException("Optimus test doesn't support this junit version. Please use junit 3 or junit 4. If you are using a supported version, please report this bug.");
        }
        return null;
    }

    private APFDListener getAPFDListener()
    {
        for (org.junit.runner.notification.RunListener listener : this.customRunListeners)
        {
            if (listener instanceof APFDListener)
            {
                return (APFDListener) listener;
            }
        }
        return null;
    }

    private CoverageListener getCoverageListener()
    {
        for (org.junit.runner.notification.RunListener listener : this.customRunListeners)
        {
            if (listener instanceof CoverageListener)
            {
                return (CoverageListener) listener;
            }
        }
        return null;
    }

    private CoberturaListener getCoberturaListener()
    {
        for (org.junit.runner.notification.RunListener listener : this.customRunListeners)
        {
            if (listener instanceof CoberturaListener)
            {
                return (CoberturaListener) listener;
            }
        }
        return null;
    }

    private void broadcastTestFinished()
    {
        this.projectData.writeProjectDataFile();
    }

    private RunResult invokeExecutor(AbstractTest toRun) throws TestSetFailedException
    {
        if (executor != null)
        {
            if (this.testGranularity.equals("method"))
            {
                return executor.invokeMethod(toRun);

            } else if (this.testGranularity.equals("class"))
            {
                return executor.invokeClass(toRun);
            }

        }
        return null;
    }

    //given the path of any class directory, returns the project root path
    // 
    //example: classesPath = /Users/auser/documents/project/target/main
    //returns: /Users/auser/documents/project
    private String getProjectPath(String classesPath)
    {
        String path = "";
        if (classesPath.contains("/"))
        { // linux based systems
            path = classesPath.substring(0, classesPath.indexOf("/target"));
        } else if (classesPath.contains("\\"))
        { // windows based systems
            path = classesPath.substring(0, classesPath.indexOf("\\target"));
        }

        return path;
    }

}
