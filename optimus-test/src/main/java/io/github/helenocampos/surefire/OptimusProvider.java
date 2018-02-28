package io.github.helenocampos.surefire;

import io.github.helenocampos.surefire.api.JUnitExecutor;
import io.github.helenocampos.surefire.junit4.JUnit4Executor;
import io.github.helenocampos.surefire.util.JUnit4TestChecker;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.surefire.common.junit3.JUnit3TestChecker;
import org.apache.maven.surefire.common.junit4.JUnit4RunListenerFactory;

import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
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
    private JUnit4TestChecker junit4TestChecker;

    private PojoAndJUnit3Checker v3testChecker;

    private List<org.junit.runner.notification.RunListener> customRunListeners;

    boolean isJunit4 = false;

    PrintWriter filePrinter;

    public OptimusProvider(ProviderParameters booterParameters) {
        this.providerParameters = booterParameters;
        this.testClassLoader = booterParameters.getTestClassLoader();
        this.scanResult = booterParameters.getScanResult();
        this.runOrderCalculator = booterParameters.getRunOrderCalculator();
        this.executor = null;
        this.customRunListeners = JUnit4RunListenerFactory.createCustomListeners(providerParameters.getProviderProperties().get("listener"));
        this.junit4TestChecker = new JUnit4TestChecker(testClassLoader);
        String exampleTestClassName = providerParameters.getProviderProperties().get("tc.0");
        if (exampleTestClassName != null) {
            try {
                Class exampleTestClass = testClassLoader.loadClass(exampleTestClassName);
                if (junit4TestChecker.accept(exampleTestClass)) {
                    if (this.junit4TestChecker.isValidJUnit4Test(exampleTestClass)) {
                        this.isJunit4 = true;
                    }
                } else {
                    this.v3testChecker = new PojoAndJUnit3Checker(new JUnit3TestChecker(testClassLoader));
                }
                executor = new JUnit4Executor(booterParameters, customRunListeners);
            } catch (ClassNotFoundException e) {
            }
        }

//        if (runOrderCalculator instanceof DefaultRunOrderCalculator) {
//            DefaultRunOrderCalculator orderCalculator = (DefaultRunOrderCalculator) runOrderCalculator;
//            try {
//                Field f = DefaultRunOrderCalculator.class.getDeclaredField("runOrderParameters");
//                f.setAccessible(true);
//                RunOrderParameters runOrderParameters = (RunOrderParameters) f.get(orderCalculator);
//                RunEntryStatisticsMap stat = RunEntryStatisticsMap.fromFile( runOrderParameters.getRunStatisticsFile() );
//                System.out.println("sassdsca");
//             } catch (IllegalAccessException e) {
//
//            } catch (NoSuchFieldException ex) {
//                Logger.getLogger(OptimusProvider.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (SecurityException ex) {
//                Logger.getLogger(OptimusProvider.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

    }

    private TestsToRun scanClassPath() {
        if (isJunit4) {
            final TestsToRun testsToRun = scanResult.applyFilter(junit4TestChecker, testClassLoader);
            return runOrderCalculator.orderTestClasses(testsToRun);
        } else {
            final TestsToRun testsToRun = scanResult.applyFilter(v3testChecker, testClassLoader);
            return runOrderCalculator.orderTestClasses(testsToRun);
        }
    }

    public Iterable<?> getSuites() {
        TestMethodsToRun toRun = new TestMethodsToRun(scanClassPath(), isJunit4);
        return toRun;
    }

    public RunResult invoke(Object forkTestSet) throws TestSetFailedException, ReporterException, InvocationTargetException {
        if (forkTestSet == null || !(forkTestSet instanceof String)) {
            throw new TestSetFailedException("IsolatedJUnitProvider requires reuseForks=false");
        }
        if (executor == null) {
            throw new TestSetFailedException("Optimus test doesn't support this junit version. Please use junit 3 or junit 4. If you are using a supported version, please report this bug.");
        }
        String toRun = (String) forkTestSet;
        try {
            String[] d = toRun.split("#");
            Class<?> tc = testClassLoader.loadClass(d[0]);
            String method = d[1];
            if (executor != null) {
                return executor.invoke(tc, method);
            } else {
                return null;
            }

        } catch (ClassNotFoundException e) {
            throw new TestSetFailedException(e);
        }
    }

}
