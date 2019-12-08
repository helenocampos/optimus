/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.testing;

/**
 *
 * @author helenocampos
 */
public enum TestGranularity
{
    METHOD("method",1),
    CLASS("class",2);

    TestGranularity(String name, int id)
    {
        this.name = name;
        this.id = id;
    }

    private String name;
    private int id;

    public String getName()
    {
        return this.name;
    }
    
    public int getId()
    {
        return this.id;
    }

    public static TestGranularity getGranularityByName(String name)
    {
        for (TestGranularity value : values())
        {
            if (value.getName().equalsIgnoreCase(name))
            {
                return value;
            }
        }
        return null;
    }
    
    public static TestGranularity getGranularityById(int id)
    {
        for (TestGranularity granularity : TestGranularity.values())
        {
            if (id == granularity.id)
            {
                return granularity;
            }
        }
        return null;
    }

}
