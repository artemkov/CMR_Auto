/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author Дина
 */
public class ContentUtils 
{
    public static double countWeights(Content weightContent, UniqueList<Map<Content,String>> interviewList)
    {
        double weight = 1.0;
        double sum=0.0;
        if (weightContent == null)
        {
            return interviewList.size();
        }
        else
        {
            for (Map<Content,String> interview: interviewList)
            {
                double value = 1.0;
                try
                {
                    String weightVal = interview.get(weightContent);
                    if (weightVal!=null)
                    {
                        value = Double.parseDouble(weightVal);
                    }
                }
                catch (Exception e)
                {
                    
                }
                sum+=value;
            }
            return sum;
        }
        
        
    }
    
    public static double countWeights(String weightContent, UniqueList<Map<Content,String>> interviewList)
    {
        double weight = 1.0;
        double sum=0.0;
        Content wc = null;
        try
        {
            wc = ContentUtils.getContentByNameFromInterviewList(interviewList, weightContent);
        }
        catch (Exception e)
        {
            return (double)interviewList.size();
        }
        if ((wc!=null))
        {
            for (Map<Content,String> interview: interviewList)
            {
                double value = 1.0;
                try
                {
                    String weightVal = interview.get(wc);
                    if (weightVal!=null)
                    {
                        value = Double.parseDouble(weightVal);
                    }
                }
                catch (Exception e)
                {
                    
                }
                sum+=value;
            }
            return sum;
        }
        return (double)interviewList.size();
    }
    
    public static double countInterviewWeight(Content weightContent,Map<Content,String> interview, double defaultval)
    {
        if (weightContent==null)
        {
            return defaultval;
        }
        
        String valstr = interview.get(weightContent);
        try
        {
            double val = Double.parseDouble(valstr);
            return val;
        }
        catch (NumberFormatException nfe)
        {
            return defaultval;
        }
                
    }
    public static Content getContentByNameFromInterviewList(UniqueList<Map<Content,String>> interviewList, String varname)
    {
      if (varname==null)
          return null;
      for (Map<Content,String> iview: interviewList)
      {
        Iterator<Content> iterator = iview.keySet().iterator();
        while (iterator.hasNext())
        {
            Content c = iterator.next();
            if (c.getName().equalsIgnoreCase(varname))
                return c;
        }
        
      } 
      return null;
    }
    
    public static Content getContentByNameFromInterview (Map<Content,String> interview, String varname)
    {
      
        Iterator<Content> iterator = interview.keySet().iterator();
        while (iterator.hasNext())
        {
            Content c = iterator.next();
            if (c.getName().equalsIgnoreCase(varname))
                return c;
        }
        return null;
    }
    
    public static TreeSet<String> getContentUniqueValuesFromInterviewList(UniqueList<Map<Content,String>> interviewList, Content content)
    {
        TreeSet<String> uniqvalues = new TreeSet<>(new StringIntComparator());
                
        for(Map<Content,String> valmap : interviewList )
        {   
            String value = valmap.get(content);
            if (value!=null)
            {
                uniqvalues.add(value);
            }
        }
        return uniqvalues;
    }
    
    public static TreeSet<String> getContentUniqueValuesFromSampleList(List<UniqueList<Map<Content,String>>> sampleList, Content content)
    {
        TreeSet<String> uniqvalues = new TreeSet<>(new StringIntComparator());
                
        for (UniqueList<Map<Content,String>> interviewList : sampleList)
        {
            for(Map<Content,String> valmap : interviewList )
            {   
                String value = valmap.get(content);
                if (value!=null)
                {
                    uniqvalues.add(value);
                }
            }
        }
        return uniqvalues;
    }
}
