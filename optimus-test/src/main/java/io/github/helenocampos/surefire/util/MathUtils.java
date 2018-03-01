/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.util;

/**
 *
 * @author helenocampos
 */
public class MathUtils
{

    public static double arithmeticSum(int amountOfTerms, int factor)
    {
        return (amountOfTerms*(1+arithmeticSeries_getNValue(factor, amountOfTerms)))/2;
    }
    
    public static double arithmeticSeries_getNValue(int d, int n)
    {
        return 1+((n-1)*d);
    }
}
