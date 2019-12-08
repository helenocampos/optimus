/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import info.heleno.surefire.util.SimilarityMeasures;
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
public class JaccardTest
{

    public JaccardTest()
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
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void distanceTest()
    {
        String left = "10010";
        String right = "11010";
        assertEquals(0.3333d, SimilarityMeasures.getJaccardDistance(left, right), 0.01);
    }

    @Test
    public void distanceTest2()
    {
        String left = "1000000";
        String right ="1111111";
        assertEquals(0.8571d, SimilarityMeasures.getJaccardDistance(left, right), 0.01);
    }
}
