/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author helenocampos
 */
public class MutantDetails
{

    private String clazz;
    private String method;
    private List<Integer> indexes;
    private String killingTest;

    public MutantDetails(String clazz, String method, List<Integer> indexes, String killingTest)
    {
        this.clazz = clazz;
        this.method = method;
        this.indexes = indexes;
        this.killingTest = processKilingTest(killingTest);
    }

    /*
        process the text by removing () and leaving only qualifiedName.method
    Example: com.mycompany.mockproject.ClassCTest.testGetStringtest(com.mycompany.mockproject.ClassCTest)
    Should return: com.mycompany.mockproject.ClassCTest.testGetStringtest
     */
    private String processKilingTest(String killingTest)
    {
        if (killingTest.length() > 0)
        {
            int parenthesisIndex = killingTest.indexOf("(");
            killingTest = killingTest.substring(0, parenthesisIndex);
        }
        return killingTest;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (!MutantDetails.class.isAssignableFrom(obj.getClass()))
        {
            return false;
        }
        final MutantDetails other = (MutantDetails) obj;
        if (this.getClazz().equals(other.getClazz()) && this.getMethod().equals(other.getMethod()))
        {
            for (Integer index : this.getIndexes())
            {
                if (!other.indexes.contains(index))
                {
                    return false;
                }
            }
            return true;
        }
        return false;

    }

    //details needed: clazz, method, indexes
    public static MutantDetails parseDetails(File f)
    {
        if (f.exists())
        {
            Path path = Paths.get(f.getAbsolutePath());
            try
            {
                List<String> rawDetails = Files.readAllLines(path);
                for (String line : rawDetails)
                {
                    //assuming all details are contained in 1 line
                    String clazz = extractClassName(line);
                    String method = extractMethodName(line);
                    List<Integer> indexes = extractIndexes(line);
                    return new MutantDetails(clazz, method, indexes, "");
                }

            } catch (IOException ex)
            {
                Logger.getLogger(MutantDetails.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return null;
    }

    //clazz comes followed by '=' and the name of the class.
    //example: clazz=com.mycompany.mockproject.A
    private static String extractClassName(String line)
    {
        if (line.contains("clazz="))
        {
            //locate the index location in the string for 'clazz=' and the index for the next ',' after that
            int index = line.indexOf("clazz=");
            String clazz = line.substring(index);
            clazz = clazz.replace("clazz=", "");
            int endIndex = clazz.indexOf(",");
            clazz = clazz.substring(0, endIndex);
            return clazz;
        } else
        {
            return "";
        }
    }

    //method comes followed by '=' and the name of the method.
    //example: method=<init>
    private static String extractMethodName(String line)
    {
        if (line.contains("method="))
        {
            //locate the index location in the string for 'method=' and the index for the next ',' after that
            int index = line.indexOf("method=");
            String method = line.substring(index);
            method = method.replace("method=", "");
            int endIndex = method.indexOf(",");
            method = method.substring(0, endIndex);
            return method;
        } else
        {
            return "";
        }
    }

    //indexes comes followed by '=[' then integers representing indexes separated by ',' then ']'
    //example: indexes=[12]
    private static List<Integer> extractIndexes(String line)
    {

        if (line.contains("indexes="))
        {
            //locate the index location in the string for 'indexes=[' and the index for the next ']' after that
            int index = line.indexOf("indexes=[");
            String indexes = line.substring(index);
            indexes = indexes.replace("indexes=[", "");
            int endIndex = indexes.indexOf("]");
            indexes = indexes.substring(0, endIndex);

            String[] indexesStringArray = indexes.split(",");
            List<Integer> indexesReturnList = new LinkedList<Integer>();

            for (String indexString : indexesStringArray)
            {
                indexesReturnList.add(Integer.parseInt(indexString));
            }
            return indexesReturnList;
        } else
        {
            return new LinkedList<Integer>();
        }
    }

    public String getClazz()
    {
        return clazz;
    }

    public void setClazz(String clazz)
    {
        this.clazz = clazz;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public List<Integer> getIndexes()
    {
        return indexes;
    }

    public void setIndexes(List<Integer> indexes)
    {
        this.indexes = indexes;
    }

    public String getKillingTest()
    {
        return killingTest;
    }

    public void setKillingTest(String killingTest)
    {
        this.killingTest = killingTest;
    }
}
