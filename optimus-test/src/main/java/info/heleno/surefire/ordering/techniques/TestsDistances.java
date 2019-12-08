/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire.ordering.techniques;

import java.util.HashMap;

/**
 *
 * @author Heleno
 */
public class TestsDistances {
    private HashMap<String,HashMap<String,Double>> distances;
    
    public TestsDistances(){
        this.distances = new HashMap<>();
    }
    
    public void addDistance(String source, String target, Double value){
        HashMap<String,Double> line = distances.get(source);
        if(line==null){
            line = new HashMap<>();
            distances.put(source, line);
        }
        Double distance = line.get(target);
        if(distance==null){
            line.put(target, value);
        }
    }
    
    public Double getDistance(String source, String target){
        HashMap<String,Double> line = distances.get(source);
        if(line!=null){
            return line.get(target);
        }
        return null;
    }
}
