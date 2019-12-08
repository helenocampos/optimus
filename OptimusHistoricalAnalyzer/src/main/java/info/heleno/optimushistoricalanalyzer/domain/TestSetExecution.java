/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.optimushistoricalanalyzer.domain;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class TestSetExecution
{
    private List<TestCaseExecution> executedTests;
    private int id;
    private long timeStamp;
    private String projectName;
    
    public TestSetExecution(int id, long timeStamp, String projectName){
        this.executedTests = new LinkedList<>();
        this.id=id;
        this.timeStamp = timeStamp;
        this.projectName = projectName;
    }
    
    public TestSetExecution(long timeStamp, String projectName){
        this.executedTests = new LinkedList<>();
        this.timeStamp = timeStamp;
        this.projectName = projectName;
    }
    
    public TestSetExecution(){
        this.executedTests = new LinkedList<>();
    }
    
    public void addExecutedTest(TestCaseExecution executedTest){
        this.getExecutedTests().add(executedTest);
    }

    public List<TestCaseExecution> getExecutedTests()
    {
        return executedTests;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }
}
