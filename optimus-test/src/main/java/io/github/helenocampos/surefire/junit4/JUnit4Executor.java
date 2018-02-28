/*
 * Copyright 2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.helenocampos.surefire.junit4;

import io.github.helenocampos.surefire.api.JUnitExecutor;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.maven.surefire.common.junit4.JUnit4RunListener;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ConsoleOutputCapture;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.PojoStackTraceWriter;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;

/**
 *
 * @author helenocampos
 */
public class JUnit4Executor implements JUnitExecutor{
    
    private final ProviderParameters providerParameters;
    private List<org.junit.runner.notification.RunListener> customRunListeners;
    
    public JUnit4Executor(ProviderParameters booterParameters, List<org.junit.runner.notification.RunListener> customRunListeners){
        this.providerParameters = booterParameters;
        this.customRunListeners = customRunListeners;
     }

    private RunNotifier getRunNotifier(org.junit.runner.notification.RunListener main, Result result, List<org.junit.runner.notification.RunListener> others) {
        RunNotifier fNotifier = new RunNotifier();
        fNotifier.addListener(main);
        fNotifier.addListener(result.createListener());
        for (org.junit.runner.notification.RunListener listener : others) {
            fNotifier.addListener(listener);
        }
        return fNotifier;
    }

    //Mostly taken from Junit4Provider.java
    public RunResult invoke(Class<?> clazz, String requestedTestMethod) throws TestSetFailedException {

        final ReporterFactory reporterFactory = providerParameters.getReporterFactory();

        RunListener reporter = reporterFactory.createReporter();

        ConsoleOutputCapture.startCapture((ConsoleOutputReceiver) reporter);

        JUnit4RunListener jUnit4TestSetReporter = new JUnit4RunListener(reporter);

        Result result = new Result();
        RunNotifier listeners = getRunNotifier(jUnit4TestSetReporter, result, customRunListeners);

        listeners.fireTestRunStarted(Description.createTestDescription(clazz, requestedTestMethod));

        final ReportEntry report = new SimpleReportEntry(getClass().getName() + "." + requestedTestMethod, clazz.getName() + "." + requestedTestMethod);
        reporter.testSetStarting(report);
        try {
            for (final Method method : clazz.getMethods()) {
                if (method.getParameterTypes().length == 0 && requestedTestMethod.equals(method.getName())) {
                    Request.method(clazz, method.getName()).getRunner().run(listeners);
                }
            }
        } catch (Throwable e) {
            reporter.testError(SimpleReportEntry.withException(report.getSourceName(), report.getName(), new PojoStackTraceWriter(report.getSourceName(), report.getName(), e)));
        } finally {
            reporter.testSetCompleted(report);
        }
        listeners.fireTestRunFinished(result);
        JUnit4RunListener.rethrowAnyTestMechanismFailures(result);

        closeRunNotifier(jUnit4TestSetReporter, customRunListeners);
        return reporterFactory.close();
    }

    private void closeRunNotifier(org.junit.runner.notification.RunListener main, List<org.junit.runner.notification.RunListener> others) {
        RunNotifier fNotifier = new RunNotifier();
        fNotifier.removeListener(main);
        for (org.junit.runner.notification.RunListener listener : others) {
            fNotifier.removeListener(listener);
        }
    }
}
