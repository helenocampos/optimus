package info.heleno.surefire;

import info.heleno.testing.AbstractTest;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author helenocampos
 */
public abstract class AbstractTestsToRun implements Iterable<AbstractTest>
{
    private List<AbstractTest> tests = new LinkedList<AbstractTest>();
    
    public Class<?>[] extractClassesFromSuite(Class<?> suite) {
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
    
    public abstract String getGranularity();
    
     @Override
    public String toString() {
        return "Test"+getGranularity()+"ToRun [tests=" + this.tests + "]";
    }
    
    public Iterator<AbstractTest> iterator() {
        return tests.iterator();
    }
    
    public List<AbstractTest> getTests(){
        return this.tests;
    }
    
    public void addTest(AbstractTest test){
        this.tests.add(test);
    }
}
