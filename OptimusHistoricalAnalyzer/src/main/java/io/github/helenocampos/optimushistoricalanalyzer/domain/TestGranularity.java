/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.optimushistoricalanalyzer.domain;

/**
 *
 * @author helenocampos
 */
public enum TestGranularity
{
    METHOD(1),
    CLASS(2);

    TestGranularity(int id)
    {
        this.id = id;
    }

    private final int id;

    public int getId()
    {
        return this.id;
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
