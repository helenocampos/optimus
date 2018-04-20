package io.github.helenocampos.surefire.junit3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import io.github.helenocampos.testing.AbstractTest;
import io.github.helenocampos.surefire.api.JUnitExecutor;
import org.apache.maven.surefire.common.junit3.JUnit3Reflector;
import org.apache.maven.surefire.common.junit3.JUnit3TestChecker;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ConsoleOutputCapture;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.ReflectionUtils;

import org.apache.maven.surefire.report.TestSetReportEntry;

/**
 * @author Kristian Rosenvold
 */
public class JUnit3Executor implements JUnitExecutor {

    private final ClassLoader testClassLoader;

    private final JUnit3TestChecker jUnit3TestChecker;

    private final JUnit3Reflector reflector;

    private final ProviderParameters providerParameters;

    public JUnit3Executor(ProviderParameters booterParameters) {
        this.providerParameters = booterParameters;
        testClassLoader = booterParameters.getTestClassLoader();
        reflector = new JUnit3Reflector(testClassLoader);
        jUnit3TestChecker = new JUnit3TestChecker(testClassLoader);
    }

    //TODO: execute requestTestMethod
    public RunResult invokeMethod(AbstractTest test) throws TestSetFailedException {
        ReporterFactory reporterFactory = providerParameters.getReporterFactory();
        RunResult runResult;
        try {
            final RunListener reporter = reporterFactory.createReporter();
            ConsoleOutputCapture.startCapture((ConsoleOutputReceiver) reporter);

            final String smClassName = System.getProperty("surefire.security.manager");
            if (smClassName != null) {
                SecurityManager securityManager =
                (SecurityManager) ReflectionUtils.instantiate( this.getClass().getClassLoader(), smClassName, SecurityManager.class );
            }

            SurefireTestSet surefireTestSet = createTestSet(test.getTestClass());
            executeTestSet(surefireTestSet, reporter, testClassLoader);

        } finally {
            runResult = reporterFactory.close();
        }
        return runResult;
    }
    
    public RunResult invokeClass(AbstractTest test) throws TestSetFailedException {
        ReporterFactory reporterFactory = providerParameters.getReporterFactory();
        RunResult runResult;
        try {
            final RunListener reporter = reporterFactory.createReporter();
            ConsoleOutputCapture.startCapture((ConsoleOutputReceiver) reporter);

            final String smClassName = System.getProperty("surefire.security.manager");
            if (smClassName != null) {
                SecurityManager securityManager =
                (SecurityManager) ReflectionUtils.instantiate( this.getClass().getClassLoader(), smClassName, SecurityManager.class );
            }

            SurefireTestSet surefireTestSet = createTestSet(test.getTestClass());
            executeTestSet(surefireTestSet, reporter, testClassLoader);

        } finally {
            runResult = reporterFactory.close();
        }
        return runResult;
    }

    private SurefireTestSet createTestSet(Class<?> clazz)
            throws TestSetFailedException {
        return reflector.isJUnit3Available() && jUnit3TestChecker.accept(clazz)
                ? new JUnitTestSet(clazz, reflector)
                : new PojoTestSet(clazz);

    }

    private void executeTestSet(SurefireTestSet testSet, RunListener reporter, ClassLoader classLoader)
            throws TestSetFailedException {

        TestSetReportEntry report = new SimpleReportEntry(this.getClass().getName(), testSet.getName());

        reporter.testSetStarting(report);

        testSet.execute(reporter, classLoader);

        reporter.testSetCompleted(report);
    }

}
