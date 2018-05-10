/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.junit4;

import io.github.helenocampos.surefire.report.TestExecution;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author helenocampos
 */
public class FaultsListener extends RunListener
{

    private APFDListener apfdListener;
    private boolean firstFailure = true;

    public FaultsListener(APFDListener apfdListener)
    {
        super();
        this.apfdListener = apfdListener;
    }

    private void resetFaultsFile()
    {
        Path file = Paths.get("TestsRevealingFaults");
        try
        {
            Files.write(file, new byte[0]);
        } catch (IOException ex)
        {
//            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeFaultToFile(String testName)
    {
        Path file = Paths.get("TestsRevealingFaults");
        List<String> existingFaults = readFaultsFile(file);
        if (!existingFaults.contains(testName))
        {
            existingFaults.add(testName);
        }
        try
        {
            Files.write(file, existingFaults, Charset.forName("UTF-8"));
        } catch (IOException ex)
        {
//            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<String> readFaultsFile(Path path)
    {
        try
        {
            if (path.toFile().exists())
            {
                return Files.readAllLines(path);
            }

        } catch (IOException ex)
        {
            Logger.getLogger(FaultsListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new LinkedList<String>();
    }

    @Override
    public void testFailure(Failure failure) throws Exception
    {
        super.testFailure(failure);
        if (firstFailure)
        {
            firstFailure = false;
            resetFaultsFile();
        }
        String testName = getTestName(failure.getDescription());
        writeFaultToFile(testName);
        updateAPFDListener(testName);
    }

    private void updateAPFDListener(String testFailure)
    {
        if (apfdListener != null)
        {
            if (apfdListener.getExecutedTests().size() > 0)
            {
                TestExecution lastExecutedTest = apfdListener.getExecutedTests().get(apfdListener.getExecutedTests().size() - 1);
                if (lastExecutedTest.getTestName().equals(apfdListener.getTestName(testFailure)))
                {
                    Integer testExecutionOrder = apfdListener.getFaultRevealingTests().get(lastExecutedTest.getTestName());
                    if (testExecutionOrder == null || testExecutionOrder == 0)
                    {
                        apfdListener.getFaultRevealingTests().put(lastExecutedTest.getTestName(), apfdListener.getExecutedTests().size());
                        apfdListener.setFaultsAmount(apfdListener.getFaultRevealingTests().size());
                    }
                } else
                {
                    Integer testExecutionOrder = apfdListener.getFaultRevealingTests().get(apfdListener.getTestName(testFailure));
                    if (testExecutionOrder == null || testExecutionOrder == 0)
                    {
                        apfdListener.getFaultRevealingTests().put(apfdListener.getTestName(testFailure), apfdListener.getExecutedTests().size() + 1);
                        apfdListener.setFaultsAmount(apfdListener.getFaultRevealingTests().size());
                    }
                }
            } else
            {
                apfdListener.getFaultRevealingTests().put(apfdListener.getTestName(testFailure), apfdListener.getExecutedTests().size() + 1);
                apfdListener.setFaultsAmount(apfdListener.getFaultRevealingTests().size());
            }
        }
    }

    private String getTestName(Description description)
    {
        String testName = description.getClassName() + "." + description.getMethodName();
        return testName;
    }

}
