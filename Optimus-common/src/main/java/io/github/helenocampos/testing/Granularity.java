/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.testing;

/**
 *
 * @author helenocampos
 */
public enum Granularity
{
    METHOD("method"),
    CLASS("class");

    Granularity(String name)
    {
        this.name = name;
    }

    private String name;

    public String getName()
    {
        return this.name;
    }

    public static Granularity getGranularityByName(String name)
    {
        for (Granularity value : values())
        {
            if (value.getName().equalsIgnoreCase(name))
            {
                return value;
            }
        }
        return null;
    }

}
