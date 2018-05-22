package io.github.helenocampos.surefire;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.maven.surefire.util.TestsToRun;

public class TestMethodsToRun extends AbstractTestsToRun
{

    public TestMethodsToRun(TestsToRun tcs)
    {
        for (Class<?> clazz : tcs)
        {
            for (Class<?> c : extractClassesFromSuite(clazz))
            {
                ArrayList<String> methods = new ArrayList<String>();
                for (Method m : c.getDeclaredMethods())
                {
                    boolean found = false;
                    if (m.getReturnType() == Void.TYPE && m.getParameterTypes().length == 0)
                    {
                        for (Annotation a : m.getAnnotations())
                        {
                            if (a.annotationType().getCanonicalName().equals("org.junit.Test"))
                            {
                                found = true;
                            }
                        }
                    }
                    if (!found && (m.getName().startsWith("test")) && m.getReturnType() == Void.TYPE && m.getParameterTypes().length == 0)
                    {
                        if (c.getSuperclass().getCanonicalName().equals("junit.framework.TestCase"))
                        {
                            found = true;
                        }
                    }
                    if (found)
                    {
                        methods.add(m.getName());
                    }
                }
                Collections.sort(methods);
                for (String s : methods)
                {
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
