/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Дина
 */
public class AnswerGroup 
{
    private String name;
    private Set<String> answerset=new HashSet<>();

    public AnswerGroup(String name )
    {
        this.name = name;
        
    }

    public Set<String> getAnswerset() {
        return answerset;
    }
    
    
    
    public boolean isAnswerInGroup(String answer)
    {
        if (answerset!=null)
            return answerset.contains(answer);
        return false;
    }
    
    
    
    public void addAnswer(String answer)
    {
        if (answerset!=null)
            answerset.add(answer);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String toString()
    {
        return "["+name+": "+answerset+"]";
    }
     
}
