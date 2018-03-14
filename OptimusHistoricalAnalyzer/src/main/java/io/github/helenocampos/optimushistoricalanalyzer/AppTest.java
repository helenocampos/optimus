/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.optimushistoricalanalyzer;

import io.github.helenocampos.optimushistoricalanalyzer.dao.TestExecutionDAO;
import io.github.helenocampos.optimushistoricalanalyzer.domain.TestExecution;
import io.github.helenocampos.optimushistoricalanalyzer.domain.TestGranularity;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class AppTest
{

    public static void main(String[] args)
    {
        TestExecutionDAO dao = new TestExecutionDAO("test312");
        TestExecution execution = new TestExecution();
        execution.setTestName("test2");
        execution.setGranularity(TestGranularity.CLASS);
        execution.setResult(true);
        execution.setTimeStamp(System.currentTimeMillis());
        execution.setExecutionTime(12.3);
        execution.setProjectName("Gameoflife");
        dao.insertExecution(execution);
        List<TestExecution> entries = dao.selectAllEntriesByTestName("test2",execution.getTestName());
        System.out.println("");
    }
}
