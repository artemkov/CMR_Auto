/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.InvalidFilterException;
import automatization.exceptions.VariableNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Артем Ковалев
 */
public class Filter 
{
    public static UniqueList<Map<Content, String>> filter(UniqueList<Map<Content, String>> interviewList, String filterString) throws InvalidFilterException
    {
        UniqueList<Map<Content, String>> filteredList = new UniqueList<>();
        
        if (interviewList==null||interviewList.isEmpty())
            return filteredList;
        
        if (filterString.equalsIgnoreCase("ALL")||filterString.isEmpty())
            return interviewList;
            
       
        
        FilterBase fb2 = new FilterBase(filterString);
        for (int i =0; i<interviewList.size();i++)
        {
            if (fb2.compute(interviewList.get(i)))
            {
                filteredList.add(interviewList.get(i));
            }
        }
        return filteredList;
    }
    
    public static List<UniqueList<Map<Content, String>>> filter(List<UniqueList<Map<Content, String>>> sampleList, String filterString) throws InvalidFilterException
    {
        List<UniqueList<Map<Content, String>>> filteredSampleList = new ArrayList<>();
        
        if (filterString.equalsIgnoreCase("ALL")||filterString.isEmpty())
            return sampleList;
            
        
        
        FilterBase fb2 = new FilterBase(filterString);
        for (UniqueList<Map<Content, String>> interviewList: sampleList)
        {
            
            UniqueList<Map<Content, String>> filteredList = new UniqueList<>();
            for (int i =0; i<interviewList.size();i++)
            {
                if (fb2.compute(interviewList.get(i)))
                {
                    filteredList.add(interviewList.get(i));
                }
            }
            if (filteredList.size()>0)
                filteredSampleList.add(filteredList);
            else
            {
                
                filteredSampleList.add(null);
            }
        }
        return filteredSampleList;
        
    }
    
    public static List<UniqueList<Map<Content, String>>> splitByAnswerGroups (UniqueList<Map<Content, String>> fullInterviewList, List<AnswerGroup> agroups, Content content)
    {
        List<UniqueList<Map<Content, String>>> splittedSampleList = new ArrayList<>();
        for (int i =0 ; i<agroups.size();i++)
        {
            UniqueList<Map<Content, String>> splittedList = new UniqueList<>();
            AnswerGroup ag = agroups.get(i);
            for (Map<Content, String> interview: fullInterviewList)
            {
                if (interview.containsKey(content))
                {
                    if (ag.isAnswerInGroup(interview.get(content)))
                    {
                        splittedList.add(interview);
                    }
                }
            }
            splittedSampleList.add(splittedList);
        }
        return splittedSampleList;
    }
    
    public static boolean checkInterview (Map<Content, String> iview, String filterString)throws InvalidFilterException
    {
        if (filterString.equalsIgnoreCase("ALL")||filterString.isEmpty())
            return true;
        FilterBase fb2 = new FilterBase(filterString);
        
        if (fb2.compute(iview))
        {
            return true;
        }
        return false;
    }
    
    public static List<UniqueList<Map<Content, String>>> splitByInterviewGroups (UniqueList<Map<Content, String>> fullInterviewList, List<InterviewGroup> igroups) throws InvalidFilterException
    {
        List<UniqueList<Map<Content, String>>> splittedSampleList = new ArrayList<>();
        for (int i =0 ; i<igroups.size();i++)
        {
            UniqueList<Map<Content, String>> splittedList = new UniqueList<>();
            InterviewGroup ig = igroups.get(i);
            for (Map<Content, String> interview: fullInterviewList)
            {
                if (ig.isInterviewInGroup(interview))
                {
                    splittedList.add(interview);
                }
            }
            splittedSampleList.add(splittedList);
        }
        return splittedSampleList;
    }
    
    
    
    
    
}
