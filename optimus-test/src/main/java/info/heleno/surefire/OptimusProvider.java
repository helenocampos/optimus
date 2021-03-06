package info.heleno.surefire;

import info.heleno.extractor.LocalProjectCrawler;
import info.heleno.extractor.model.ProjectData;
import info.heleno.surefire.api.JUnitExecutor;
import info.heleno.surefire.junit4.CoberturaListener;
import info.heleno.surefire.junit4.FaultsListener;
import info.heleno.surefire.junit4.JUnit4Executor;
import info.heleno.surefire.junit4.TestExecutorSimulation;
import info.heleno.surefire.ordering.TestsSorter;
import info.heleno.testing.AbstractTest;
import info.heleno.testing.TestGranularity;
import info.heleno.surefire.report.ExecutionData;
import info.heleno.surefire.junit4.CoverageListener;
import info.heleno.surefire.junit4.MethodAPFDListener;
import info.heleno.surefire.junit4.HistoricalDataListener;
import info.heleno.surefire.junit4.ClassAPFDListener2;
import info.heleno.surefire.junit4.APFDListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
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


public class OptimusProvider extends AbstractProvider {

    private final ClassLoader testClassLoader;
    private final ProviderParameters providerParameters;
    private final RunOrderCalculator runOrderCalculator;
    private JUnitExecutor executor;
    private final ScanResult scanResult;
    private List<org.junit.runner.notification.RunListener> customRunListeners;
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
    private boolean collectCoverageData = false;
    private boolean simulateExecution = false;
    
    public OptimusProvider(ProviderParameters booterParameters) {
        this.providerParameters = booterParameters;
        this.integrationTests = isIntegrationTest();
        this.testClassLoader = booterParameters.getTestClassLoader();
        this.scanResult = booterParameters.getScanResult();
        this.runOrderCalculator = booterParameters.getRunOrderCalculator();
        this.executor = null;
        this.customRunListeners = JUnit4RunListenerFactory.createCustomListeners(providerParameters.getProviderProperties().get("listener"));
        this.consoleStream = this.providerParameters.getConsoleLogger();
        this.projectPath = getProjectPath(providerParameters.getProviderProperties().get("testClassesDirectory"));
        setupProperties();
        if (simulateExecution) {
            this.projectData = ProjectData.getProjectData();
            executor = new TestExecutorSimulation(consoleStream, booterParameters, customRunListeners);
        } else {
            LocalProjectCrawler crawler = new LocalProjectCrawler(this.projectPath);
            this.projectData = crawler.getProjectData();
            executor = new JUnit4Executor(booterParameters, customRunListeners);
        }
        if (!integrationTests) {
            if (collectCoverageData) {
                this.customRunListeners.add(new CoverageListener(this.projectPath, this.projectData));
            }
            if (this.calculateAPFD) {
                if (this.testGranularity.equals(TestGranularity.METHOD.getName())) {
                    this.customRunListeners.add(new MethodAPFDListener(this.testGranularity));
                } else {
                    this.customRunListeners.add(new ClassAPFDListener2(this.testGranularity));
                }
            }
            if (this.registerExecution && !simulateExecution) {
                if (this.projectName != null) {
                    this.customRunListeners.add(new HistoricalDataListener(this.testGranularity, this.dbPath, this.projectName));
                }
            }
            if (this.generateFaultsFile && this.testGranularity != null) {
                this.customRunListeners.add(new FaultsListener(getAPFDListener()));
            }
        }
        if (this.collectCoberturaData) {
            this.customRunListeners.add(new CoberturaListener(this.projectPath, this.projectData));
        }

    }

    private boolean isIntegrationTest() {
        if (this.providerParameters != null) {
            if (this.providerParameters.getDirectoryScannerParameters() != null) {
                if (this.providerParameters.getDirectoryScannerParameters().getIncludes() != null) {
                    List<String> includes = this.providerParameters.getDirectoryScannerParameters().getIncludes();
                    for (String include : includes) {
                        if (StringUtils.containsIgnoreCase(include, "integration")) {
                            return true;
                        }

                    }
                }
            }
        }

        return false;
    }

    private void setupProperties() {
        Properties properties = System.getProperties();
        this.collectCoberturaData = Boolean.valueOf(properties.getProperty("collectCoberturaData", "false"));
        this.testGranularity = properties.getProperty("granularity");
        this.prioritizationTechnique = properties.getProperty("prioritization");
        this.dbPath = properties.getProperty("dbPath");
        if (!this.collectCoberturaData) {
            this.collectCoverageData = Boolean.valueOf(getProviderProperty("collectCoverageData", "true"));
            if (this.testGranularity == null && this.prioritizationTechnique == null) {
                this.testGranularity = getProviderProperty("granularity", "class");
                this.prioritizationTechnique = getProviderProperty("prioritization", "");
                this.calculateAPFD = Boolean.valueOf(getProviderProperty("apfd", "false"));
                this.generateFaultsFile = Boolean.valueOf(getProviderProperty("faultsFile", "false"));
                properties.setProperty("firstVersionExecution", getProviderProperty("firstVersionExecution", "false"));
            }
            if (dbPath == null) {
                this.dbPath = getProviderProperty("dbPath", "");
                this.registerExecution = !dbPath.equals("");
                properties.setProperty("dbPath", checkForPreviousVersionDB(this.dbPath));
            }
            this.projectName = getProviderProperty("projectName", null);
            if (this.projectName != null) {
                properties.setProperty("projectName", this.projectName);
            }
            properties.setProperty("clustersAmount", getProviderProperty("clustersAmount", ""));
            this.simulateExecution = Boolean.valueOf(getProviderProperty("simulateExecution", "false"));
        }
    }
    
    private String checkForPreviousVersionDB(String path)
    {
        File f = new File(path + ".backup");
        if (f.exists() && !f.isDirectory()){
            return path + ".backup";
        }else{
            return path;
        }
    }

    private String getProviderProperty(String property, String defaultValue) {
        return this.providerParameters.getProviderProperties().getOrDefault(property, defaultValue);
    }

    private TestsToRun scanClassPath() {
        final TestsToRun testsToRun = scanResult.applyFilter(null, testClassLoader);
        return runOrderCalculator.orderTestClasses(testsToRun);
    }

    public Iterable getSuites() {
        AbstractTestsToRun toRun = null;
        if (this.testGranularity.equals("method")) {
            toRun = new TestMethodsToRun(scanClassPath());
        } else {
            toRun = new TestClassesToRun(scanClassPath());
        }
        return toRun;
    }

    private List<AbstractTest> orderTests(Iterable testSet) {
        List<AbstractTest> tests = new LinkedList<AbstractTest>();
        for (Object test : testSet) {
            tests.add((AbstractTest) test);
        }
        if (!integrationTests && tests.size() > 0) {
            TestsSorter sorter = new TestsSorter(consoleStream);
            tests = sorter.sort(tests, prioritizationTechnique, testGranularity);
        }
        return tests;
    }

    public RunResult invoke(Object forkTestSet) throws TestSetFailedException, ReporterException, InvocationTargetException {
        if (forkTestSet == null) {
            RunResult globalResult = new RunResult(0, 0, 0, 0);
            List<String> techniques = getPrioritizationTechniquesList(prioritizationTechnique);
            for (String technique : techniques) {
                prioritizationTechnique = technique;
                long startTime = System.currentTimeMillis();
                Iterable<AbstractTest> testsToRun = orderTests(getSuites());
                boolean hasExecutableTests = false;
                for (AbstractTest toRun : testsToRun) {
                    hasExecutableTests = true;
                    globalResult.aggregate(invokeExecutor(toRun));
                }
                long finishTime = System.currentTimeMillis();
                if (hasExecutableTests) {
                    broadcastTestFinished();
                    if (this.calculateAPFD) {
                        ExecutionData executionData;
                        APFDListener listener = getAPFDListener();
                        executionData = listener.calculateAPFD();
                        if (executionData != null) {
                            executionData.setTechnique(prioritizationTechnique);
                            executionData.setTestGranularity(testGranularity);
                            executionData.setProjectPath(projectPath);
                            float executionTime = (float) (finishTime - startTime) / 1000;
                            executionData.setExecutionTime(executionTime);
                            executionData.setExecutionDate(new SimpleDateFormat("HH:mm:ss MM/dd/yyyy").format(new Date()));
                            executionData.writeExecutionData();
                        }
                        listener.resetListener();
                    }
                }
            }
            return globalResult;
        }
        if (!(forkTestSet instanceof AbstractTest)) {
            throw new TestSetFailedException("This test mode is not supported yet");
        }
        if (executor == null) {
            throw new TestSetFailedException("Optimus test doesn't support this junit version. Please use junit 3 or junit 4. If you are using a supported version, please report this bug.");
        }
        return null;
    }

    private List<String> getPrioritizationTechniquesList(String prioritizationTechnique) {
        String[] techniques = prioritizationTechnique.split(",");
        return Arrays.asList(techniques);
    }

    private APFDListener getAPFDListener() {
        for (org.junit.runner.notification.RunListener listener : this.customRunListeners) {
            if (listener instanceof APFDListener) {
                return (APFDListener) listener;
            }
        }
        return null;
    }

    private void broadcastTestFinished() {
        if (!simulateExecution) {
            this.projectData.writeProjectDataFile();
        }
        if (registerExecution) {
            HistoricalDataListener listener = this.getHistoricalDataListener();
            if (listener != null) {
                listener.registerExecution();
            }
        }
    }

    private HistoricalDataListener getHistoricalDataListener() {
        for (org.junit.runner.notification.RunListener listener : this.customRunListeners) {
            if (listener instanceof HistoricalDataListener) {
                return (HistoricalDataListener) listener;
            }
        }
        return null;
    }

    private RunResult invokeExecutor(AbstractTest toRun) throws TestSetFailedException {
        if (executor != null) {
            if (this.testGranularity.equals("method")) {
                return executor.invokeMethod(toRun);

            } else if (this.testGranularity.equals("class")) {
                return executor.invokeClass(toRun);
            }

        }
        return null;
    }

    //given the path of any class directory, returns the project root path
    // 
    //example: classesPath = /Users/auser/documents/project/target/main
    //returns: /Users/auser/documents/project
    private String getProjectPath(String classesPath) {
        String path = "";
        if (classesPath.contains("/")) { // linux based systems
            path = classesPath.substring(0, classesPath.indexOf("/target"));
        } else if (classesPath.contains("\\")) { // windows based systems
            path = classesPath.substring(0, classesPath.indexOf("\\target"));
        }

        return path;
    }

}
