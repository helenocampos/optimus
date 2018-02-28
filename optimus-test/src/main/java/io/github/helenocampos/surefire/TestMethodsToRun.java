package io.github.helenocampos.surefire;

import io.github.helenocampos.surefire.ordering.methods.MethodsAlphabeticalOrder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.surefire.util.TestsToRun;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

public class TestMethodsToRun implements Iterable<String> {

    private final List<String> tests;

    public TestMethodsToRun(TestsToRun tcs, boolean isJUnit4) {

        tests = new LinkedList<String>();
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
                    tests.add(c.getName() + "#" + s);
                }
            }
        }
//        Collections.shuffle(tests);
        Collections.sort(tests, new MethodsAlphabeticalOrder());
    }

    private Class<?>[] extractClassesFromSuite(Class<?> suite) {
        if (isSuite(suite)) {
            Suite.SuiteClasses annotation = suite.getAnnotation(Suite.SuiteClasses.class);
            return annotation.value();
        } else {
            return new Class<?>[]{suite};
        }

    }

    private boolean isSuite(Class<?> clazz) {
        for (Annotation a : clazz.getAnnotationsByType(RunWith.class)) {
            RunWith annotation = (RunWith) a;
            if (annotation.value().equals(Suite.class)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "TestMethodsToRun [tests=" + tests + "]";
    }

    public Iterator<String> iterator() {
        return tests.iterator();
    }
}
