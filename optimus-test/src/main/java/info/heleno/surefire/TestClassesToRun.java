/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire;

import org.apache.maven.surefire.util.TestsToRun;

/**
 *
 * @author helenocampos
 */
public class TestClassesToRun extends AbstractTestsToRun
{

    public TestClassesToRun(TestsToRun tcs)
    {
        for (Class<?> clazz : tcs)
        {
            for (Class<?> c : extractClassesFromSuite(clazz))
            {
                TestClass tc = new TestClass();
                tc.setTestClass(c);
                tc.setQualifiedName(c.getCanonicalName());
                this.addTest(tc);
            }
        }
    }

    @Override
    public String getGranularity()
    {
        return "Classes";
    }

}
