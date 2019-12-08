/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire.ordering;

/**
 *
 * @author helenocampos
 */
public enum Strategy
{
    DEFAULT("default"), //default ordering strategy, order tests by comparing pairs (Collections.sort)
    RANDOM("random"), //random ordering strategy, order tests randomly (Collections.shuffle)
    ADDITIONAL("additional");  // custom ordering strategy, using feedback from the list of already prioritized tests
    
    Strategy(String name){
        this.name = name;
    }
    
    private String name;
    public String getName(){
        return this.name;
    }
    
}
