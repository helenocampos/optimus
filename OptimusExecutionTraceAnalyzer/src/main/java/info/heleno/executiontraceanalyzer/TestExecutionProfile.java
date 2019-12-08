/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.executiontraceanalyzer;

import info.heleno.extractor.model.TestMethod;
import info.heleno.extractor.model.Coverage;
import info.heleno.extractor.model.JavaClass;
import info.heleno.extractor.model.JavaSourceCodeClass;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 *
 * @author helenocampos This class represents one test method execution profile
 */
public class TestExecutionProfile
{

    private HashMap<String, String> executionTraces;
    private int totalLinesExecuted;

    public TestExecutionProfile(TestMethod testMethod, HashMap<String, JavaSourceCodeClass> projectClasses)
    {
        this.executionTraces = new HashMap<>();
        this.totalLinesExecuted = 0;
        processExecutionTraces(testMethod, projectClasses);

    }

    public TestExecutionProfile()
    {
        this.executionTraces = new HashMap<>();
        this.totalLinesExecuted = 0;
    }

    private String createEmptyHitsString(int size)
    {
        String stringHits = "";
        for (int i = 0; i < size; i++)
        {
            stringHits += "0,";
        }
        return stringHits;
    }

    private void processExecutionTraces(TestMethod testMethod, HashMap<String, JavaSourceCodeClass> projectClasses)
    {
        Coverage coverage = testMethod.getCoverage();
        for (String classKey : projectClasses.keySet())
        {
            JavaClass clazz = projectClasses.get(classKey);
            String linesHitString = "";
            if (coverage != null)
            {
                linesHitString = coverage.getLinesHit(clazz.getQualifiedName());
            }
            if (linesHitString == null || linesHitString.equals(""))
            {
                linesHitString = createEmptyHitsString(clazz.getExecutableLines());
            }
            this.executionTraces.put(classKey, linesHitString);
            this.totalLinesExecuted+=getTotalLinesHit(linesHitString);
        }

    }

    private int getTotalLinesHit(String linesHit){
        int total =0;
        String[] linesHitSplit = linesHit.split(",");
        for(String amount : linesHitSplit){
            if(!amount.equals("")){
                total += Integer.valueOf(amount);
            }
        }
        return total;
    }
    
    public HashMap<String, String> getExecutionTraces()
    {
        return executionTraces;
    }

    public int getTotalLinesExecuted()
    {
        return totalLinesExecuted;
    }

    public String getFrequencyProfile()
    {
        String frequencyProfile = "";
        for (String className : executionTraces.keySet())
        {
            String executionTrace = executionTraces.get(className);
            frequencyProfile += executionTrace;
        }
        return frequencyProfile;
    }

    //ordered frequency profile
    public String getOrderedSequence()
    {
        String frequencyProfile = getFrequencyProfile();
        return getOrderedSequence(frequencyProfile);
    }
    
    public static String getOrderedSequence(String frequencyProfile)
    {
        String[] frequencyProfileArray = frequencyProfile.split(",");
        ArrayIndexComparator comparator = new ArrayIndexComparator(frequencyProfileArray);
        Integer[] indexes = comparator.createIndexArray();
        Arrays.sort(indexes, comparator);
        return encodeArraytoString(indexes);
    }

    private static String encodeArraytoString(Integer[] array)
    {
        String result = "";
        for (int i = 0; i < array.length; i++)
        {
            result += Integer.toString(array[i]) + ",";
        }
        return result;
    }

    public static class ArrayIndexComparator implements Comparator<Integer>
    {
        private final String[] array;

        public ArrayIndexComparator(String[] array)
        {
            this.array = array;
        }

        public Integer[] createIndexArray()
        {
            Integer[] indexes = new Integer[array.length];
            for (int i = 0; i < array.length; i++)
            {
                indexes[i] = i;
            }
            return indexes;
        }

        @Override
        public int compare(Integer index1, Integer index2)
        {
            return array[index1].compareTo(array[index2]);
        }
    }

    public void setExecutionTraces(HashMap<String, String> executionTraces)
    {
        this.executionTraces = executionTraces;
    }
}
