/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.junit4;

import io.github.helenocampos.surefire.report.ExecutionData;
import io.github.helenocampos.surefire.util.MathUtils;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author helenocampos
 */
public class MethodAPFDListener extends RunListener
{

    private int executedTests;
    private int faultsAmount;
    private HashMap<String,Integer> faultRevealingTests;

    public MethodAPFDListener()
    {
        super();
        this.executedTests = 0;
        this.faultsAmount = 0;
        this.faultRevealingTests = new HashMap<String,Integer>();
        readTestsFile();
    }
    

    private void readTestsFile(){
        Path file = Paths.get("TestsRevealingFaults");
        List<String> fileLines = new LinkedList<String>();
        try
        {
            fileLines = Files.readAllLines(file, Charset.forName("UTF-8"));
        } catch (IOException ex)
        {
//            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(String line: fileLines){
            faultRevealingTests.put(line, 0);
        }
        this.faultsAmount = fileLines.size();
    }
    
    public void testFinished(Description description) throws Exception
    {
        super.testFinished(description);
        this.executedTests++;
        String testName = description.getClassName() + "." + description.getMethodName();
        if(faultRevealingTests.containsKey(testName)){
            faultRevealingTests.put(testName, executedTests);
        }
        writeAPFDInfo();
        
    }
    
    private void writeAPFDInfo(){
        List<String> lines = new LinkedList<String>();
        lines.add(Integer.toString(executedTests));
        lines.add(Integer.toString(faultsAmount));
        for(Integer position: faultRevealingTests.values()){
            lines.add(Integer.toString(position));
        }
        Path file = Paths.get("APFDInfo");
        try
        {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException ex)
        {
//            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void readAPFDInfo(){
        Path file = Paths.get("APFDInfo");
        List<String> fileLines = new LinkedList<String>();
        try
        {
            fileLines = Files.readAllLines(file, Charset.forName("UTF-8"));
            if(fileLines.size()>=3){
                this.executedTests = Integer.valueOf(fileLines.get(0));
                this.faultsAmount = Integer.valueOf(fileLines.get(1));
                for(int x=2; x<fileLines.size(); x++){
                    faultRevealingTests.put(Integer.toString(x), Integer.valueOf(fileLines.get(x)));
                }
                
                        
            }
        } catch (IOException ex)
        {
//            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static ExecutionData calculateAPFD(){
        MethodAPFDListener listener = new MethodAPFDListener();
        listener.readAPFDInfo();
        double n = listener.executedTests;
        double m = listener.faultsAmount;
        double APFD = 1-(sum(listener.faultRevealingTests.values())/(n*m))+(1/(2*n));
        double optimalAPFD = getOptimalAPFD(n, m);
        System.out.println("Optimal APFD="+optimalAPFD);
        System.out.println("Achieved APFD="+APFD);
        return getExecutionData(listener, APFD, optimalAPFD);
    }
    
    private static ExecutionData getExecutionData(MethodAPFDListener listener, double APFD, double optimalAPFD){
        ExecutionData data = new ExecutionData();
        data.setAmountExecutedTests(listener.executedTests);
        data.setSeededFaultsAmount(listener.faultsAmount);
        data.setAPFD(APFD);
        data.setOptimalAPFD(optimalAPFD);
        return data;
    }
    
    //considering that 1 test reveals only 1 fault
    private static double getOptimalAPFD(double amountOfTests, double amountOfFaults){
        double APFD = 0;
        APFD = 1-(MathUtils.arithmeticSum((int)amountOfFaults,1)/(amountOfTests*amountOfFaults))+(1/(2*amountOfTests));
        return APFD;
    }
    
    public static int sum(Collection<Integer> list){
        int sum = 0;
        for(Integer value: list){
            sum+=value;
        }
        return sum;
    }
}
