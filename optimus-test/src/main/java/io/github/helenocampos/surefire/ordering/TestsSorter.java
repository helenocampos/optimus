/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.ordering;

import io.github.helenocampos.surefire.AbstractTest;
import io.github.helenocampos.surefire.api.AdditionalOrderer;
import io.github.helenocampos.surefire.api.DefaultOrderer;
import io.github.helenocampos.surefire.api.Orderer;
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
    private String projectName;
    
    public TestsSorter(ConsoleStream consoleStream,String projectName){
        this.consoleStream = consoleStream;
        this.projectName = projectName;
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
        List<AbstractTest> sortedTests = new LinkedList<AbstractTest>();
        while(!tests.isEmpty()){
            AbstractTest highestCoverageTest = testsOrderer.getHighestCoverageTest(tests);
            sortedTests.add(highestCoverageTest);
            tests.remove(highestCoverageTest);
        }
        return sortedTests;
    }
}
