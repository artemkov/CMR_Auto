/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.InvalidFilterException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Артем Ковалев
 */
public class InterviewGroup 
{
    private String name;
    private String filterString;

    public InterviewGroup(String name, String filterString) 
    {
        this.name = name;
        this.filterString = filterString;
    }

    public String getName() {
        return name;
    }

    public String getFilterString() {
        return filterString;
    }
    
    public boolean isInterviewInGroup(Map<Content, String> interview) throws InvalidFilterException
    {
        if (filterString!=null)
        {
            return Filter.checkInterview(interview, filterString);
        }
        return false;
    }
    
    public void addCondition(String condition)
    {
        
        filterString += filterString.isEmpty()?"":"|";
        filterString +=condition;
    }
    
    public InterviewGroup(AnswerGroup ag, Content content)
    {
        this.name=ag.getName();
        String fstr="";
        String contName = content.getName();
        Set<String> answerSet = ag.getAnswerset();
        Iterator<String> i = answerSet.iterator();
        int count=0;
        while (i.hasNext())
        {
            if (count>0)
                fstr+="|";
            String ans = i.next();
            fstr+=contName+"("+ans+")";
            count++;
        }
        filterString=fstr;
    }
    
    public void addCondition(String answer, String content)
    {
        String condition = content+"("+answer+")";
        if ((filterString==null)||(filterString.isEmpty()))
            filterString = condition;
        else
            filterString +="|"+condition;
    }
    
    @Override
    public String toString()
    {
        return "["+this.name+"=>"+this.filterString+"]";
    }
    
}
