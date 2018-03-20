/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.junit4;

import io.github.helenocampos.optimushistoricalanalyzer.domain.TestExecution;
import io.github.helenocampos.optimushistoricalanalyzer.domain.TestGranularity;
import io.github.helenocampos.surefire.ordering.Granularity;
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
    public FaultsListener()
    {
        super();
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

        String testName = getTestName(failure.getDescription());
        writeFaultToFile(testName);

    }

    private String getTestName(Description description)
    {
        String testName = description.getClassName() + "." + description.getMethodName();
        return testName;
    }

}
