package io.github.helenocampos.surefire;

import io.github.helenocampos.surefire.api.JUnitExecutor;
import io.github.helenocampos.surefire.extractor.LocalProjectCrawler;
import io.github.helenocampos.surefire.junit4.ClassAPFDListener;
import io.github.helenocampos.surefire.junit4.MethodAPFDListener;
import io.github.helenocampos.surefire.junit4.CoverageListener;
import io.github.helenocampos.surefire.junit4.JUnit4Executor;
import io.github.helenocampos.surefire.ordering.Granularity;
import io.github.helenocampos.surefire.ordering.TestsSorter;
import io.github.helenocampos.surefire.report.ExecutionData;
import io.github.helenocampos.surefire.util.JUnit4TestChecker;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
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
                    this.customRunListeners.add(new CoverageListener(this.projectPath));
                    if (this.calculateAPFD)
                    {
                        if (this.testGranularity.equals(Granularity.METHOD.getName()))
                        {
                            this.customRunListeners.add(new MethodAPFDListener());
                        } else
                        {
                            this.customRunListeners.add(new ClassAPFDListener());
                        }
                    }
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
        if (this.testGranularity == null && this.prioritizationTechnique == null)
        {
            Map<String, String> providerProperties = this.providerParameters.getProviderProperties();
            this.testGranularity = providerProperties.getOrDefault("granularity", "class");
            this.prioritizationTechnique = providerProperties.getOrDefault("prioritization", "");
            String apfd = providerProperties.getOrDefault("apfd", "false");
            this.calculateAPFD = Boolean.valueOf(apfd);
        }

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
        if (!integrationTests)
        {
            TestsSorter sorter = new TestsSorter(consoleStream);
            tests = sorter.sort(tests, prioritizationTechnique, testGranularity);
        }
        return tests;
    }

    public RunResult invoke(Object forkTestSet) throws TestSetFailedException, ReporterException, InvocationTargetException
    {
        if (forkTestSet == null)
        {
            RunResult globalResult = new RunResult(0, 0, 0, 0);
            Iterable<AbstractTest> testsToRun = orderTests(getSuites());
            for (AbstractTest toRun : testsToRun)
            {
                globalResult.aggregate(invokeExecutor(toRun));
            }
            if (this.calculateAPFD)
            {
                ExecutionData executionData;
                if (this.testGranularity.equals(Granularity.METHOD.getName()))
                {
                    executionData = MethodAPFDListener.calculateAPFD();
                } else
                {
                    executionData =ClassAPFDListener.calculateAPFD();
                }
                if(executionData!=null){
                    executionData.setTechnique(prioritizationTechnique);
                    executionData.setTestGranularity(testGranularity);
                    executionData.setProjectPath(projectPath);
                    executionData.writeExecutionData();
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
