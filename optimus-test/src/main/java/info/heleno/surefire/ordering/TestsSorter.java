/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire.ordering;

import info.heleno.surefire.api.AdditionalOrderer;
import info.heleno.surefire.api.DefaultOrderer;
import info.heleno.surefire.api.Orderer;
import info.heleno.surefire.ordering.techniques.PrioritizationTechniques;
import info.heleno.testing.AbstractTest;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.surefire.report.ConsoleStream;

/**
 *
 * @author helenocampos
 */
public class TestsSorter
{
    private final ConsoleStream consoleStream;
    
    public TestsSorter(ConsoleStream consoleStream){
        this.consoleStream = consoleStream;
    }
    
    public List<AbstractTest> sort(List<AbstractTest> tests, String prioritizationTechnique, String testGranularity){
        Orderer orderer = PrioritizationTechniques.getTechinqueByNameAndGranularity(prioritizationTechnique, testGranularity, consoleStream);
        if(orderer == null){
            return tests;
        }else if(orderer.getStrategy().equals(Strategy.DEFAULT.getName())){
            sort(tests,(DefaultOrderer) orderer);
        }else if(orderer.getStrategy().equals(Strategy.ADDITIONAL.getName())){
            return sortAdditional(tests,(AdditionalOrderer) orderer);
        }else if(orderer.getStrategy().equals(Strategy.RANDOM.getName())){
            sortRandom(tests);
        }
        return tests;
    }
    
    public void sortRandom(List<AbstractTest> tests){
        Collections.shuffle(tests);
    }
    
    public void sort(List<AbstractTest> tests, DefaultOrderer testsOrderer){
        Collections.sort(tests,testsOrderer.reversed());
    }
    
    public List<AbstractTest> sortAdditional(List<AbstractTest> tests, AdditionalOrderer<AbstractTest> testsOrderer){
        List<AbstractTest> sortedTests = new LinkedList<>();
        while(!tests.isEmpty()){
            AbstractTest nextTest = testsOrderer.getNextTest(tests, sortedTests);
            sortedTests.add(nextTest);
            tests.remove(nextTest);
        }
        return sortedTests;
    }
}
