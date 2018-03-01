package io.github.helenocampos.surefire;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.maven.surefire.util.TestsToRun;

public class TestMethodsToRun extends AbstractTestsToRun{

    public TestMethodsToRun(TestsToRun tcs, boolean isJUnit4) {
        for (Class<?> clazz : tcs) {
            for (Class<?> c : extractClassesFromSuite(clazz)) {
                ArrayList<String> methods = new ArrayList<String>();
                for (Method m : c.getDeclaredMethods()) {
                    boolean found = false;
                    if (isJUnit4 && m.getReturnType() == Void.TYPE && m.getParameterTypes().length == 0) {
                        for (Annotation a : m.getAnnotations()) {
                            if (a.annotationType().getCanonicalName().equals("org.junit.Test")) {
                                found = true;
                            }
                        }
                    }
                    if (!isJUnit4 && (m.getName().startsWith("test")) && m.getReturnType() == Void.TYPE && m.getParameterTypes().length == 0) {
                        found = true;
                    }
                    if (found) {
                        methods.add(m.getName());
                    }
                }
                Collections.sort(methods);
                for (String s : methods) {
                    TestMethod testMethod = new TestMethod();
                    testMethod.setTestClass(c);
                    testMethod.setMethodName(s);
                    this.addTest(testMethod);
                }
            }
        }
    }

    @Override
    public String getGranularity()
    {
        return "Method";
    }

}
