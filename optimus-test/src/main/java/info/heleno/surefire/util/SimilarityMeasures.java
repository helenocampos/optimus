/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire.util;

/**
 *
 * @author helenocampos
 */
public class SimilarityMeasures
{
    public static double getJaccardDistance(String left, String right){
        double distance = 1;
        if(validateCoverageString(left) && validateCoverageString(right)){
            if(left.length() == right.length()){
                double intersection = 0;
                double union = 0;
                for(int i=0; i<left.length();i++){
                    boolean leftValue = getBooleanValue(left.charAt(i));
                    boolean rightValue = getBooleanValue(right.charAt(i));
                    if(leftValue && rightValue) intersection++;
                    if (leftValue || rightValue) union++;
                }
                distance = 1-(intersection/union);
            }
        }
        return distance;
    }
    
    private static boolean getBooleanValue(char charValue){
        return charValue=='1';
    }
    
    private static boolean validateCoverageString(String coverageString){
        return coverageString.matches("[01]+");
    }
}
