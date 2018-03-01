/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.ordering;

/**
 *
 * @author helenocampos
 */
public enum Granularity
{
    METHOD("method"),
    CLASS("class");
    
    Granularity(String name){
        this.name = name;
    }
    
    private String name;
    public String getName(){
        return this.name;
    }
    
}
