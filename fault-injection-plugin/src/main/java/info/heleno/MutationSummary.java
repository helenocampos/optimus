/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class MutationSummary
{
    private long lastModified;
    
    private List<MutantDetails> killedMutations;
    
    public MutationSummary(long lastModified){
        this.lastModified = lastModified;
        this.killedMutations = new LinkedList<MutantDetails>();
    }
    
    public void addKilledMutant(MutantDetails mutant){
        if(mutant!=null){
            this.getKilledMutations().add(mutant);
        }
    }
    
    public boolean isAKilledMutant(MutantDetails mutant){
        for(MutantDetails killedMutant: this.getKilledMutations()){
            if(killedMutant.equals(mutant)){
                return true;
            }
        }
        return false;
    }

    public List<MutantDetails> getKilledMutations()
    {
        return killedMutations;
    }
    
}
