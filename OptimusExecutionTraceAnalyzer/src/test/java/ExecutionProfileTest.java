/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import io.github.helenocampos.executiontraceanalyzer.TestExecutionProfile;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author helenocampos
 */
public class ExecutionProfileTest
{
    TestExecutionProfile executionProfile;
    public ExecutionProfileTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
        executionProfile = new TestExecutionProfile();
    }
    
    @After
    public void tearDown()
    {
        executionProfile = new TestExecutionProfile();
    }

    @Test
    public void testFrequencyProfile(){
        HashMap<String, String> executionTraces = new HashMap<>();
        String trace1 = "1,1,0,0,0,1,";
        String trace2 = "1,1,0,0,0,1,";
        String trace3 = "1,2,1,1,1,1,";
        String trace4 = "1,3,2,0,2,1,";
        executionTraces.put("class1", trace1);
        executionTraces.put("class2", trace2);
        executionTraces.put("class3", trace3);
        executionTraces.put("class4", trace4);
        String frequencyProfile = "1,3,2,0,2,1,1,2,1,1,1,1,1,1,0,0,0,1,1,1,0,0,0,1,";
        executionProfile.setExecutionTraces(executionTraces);
        assertEquals(frequencyProfile,executionProfile.getFrequencyProfile());
    }
    
    @Test
    public void testOrderedSequence(){
        HashMap<String, String> executionTraces = new HashMap<>();
        String trace1 = "1,1,0,0,0,1,";
        String trace2 = "1,1,0,0,0,1,";
        String trace3 = "1,2,1,1,1,1,";
        String trace4 = "1,3,2,0,2,1,";
        executionTraces.put("class1", trace1);
        executionTraces.put("class2", trace2);
        executionTraces.put("class3", trace3);
        executionTraces.put("class4", trace4);
        //                     0 1 2 3 4 5 6 7 8 91011121314151617181920212223
        // frequencyProfile = "1,3,2,0,2,1,1,2,1,1,1,1,1,1,0,0,0,1,1,1,0,0,0,1,";
        //ordered sequence = 3,14,15,16,20,21,22,0,5,6,8,9,10,11,12,13,17,18,19,23,2,4,7,1,
        executionProfile.setExecutionTraces(executionTraces);
        String orderedSequence = "3,14,15,16,20,21,22,0,5,6,8,9,10,11,12,13,17,18,19,23,2,4,7,1,";
        assertEquals(orderedSequence,executionProfile.getOrderedSequence());
    }
}
