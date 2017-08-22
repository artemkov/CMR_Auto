/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author Артем Ковалев
 */
public class LinearReport 
{
    private Content content;
    private TreeSet<String> uniqvalues = new TreeSet<>();
    private Map<String,Double> statmap = new HashMap<>();
    private double totalSize;
    private Content weightcontent = null;

    public double getTotal()
    {
        return totalSize;
    }
    public Content getContent() {
        return content;
    }

    public TreeSet<String> getUniqvalues() {
        return uniqvalues;
    }

    public Map<String, Double> getStatmap() {
        return statmap;
    }
    
    
    

    public LinearReport(UniqueList<Map<Content,String>> interviewList, Content content, Content weightc)
    {
        double weight = 1.0;
        this.weightcontent=weightc;
            
        this.totalSize = ContentUtils.countWeights(weightcontent, interviewList);
         
        this.content=content;
        List<String> values = new ArrayList<>();
        for(Map<Content,String> valmap : interviewList )
        {
            if (weightcontent!=null)
            {
                String wstr = valmap.get(weightcontent);
                try
                {
                    weight = Double.parseDouble(wstr);
                }
                catch (NumberFormatException nfe)
                {
                    weight = 1.0;
                }
            }
            String value = valmap.get(content);
            if (value!=null)
            {
                values.add(value);
                uniqvalues.add(value);
            }
        }
        for (String value: values)
        {
            if (statmap.containsKey(value))
            {
                Double numb = statmap.get(value);
                statmap.put(value, numb+weight);
            }
            else
            {
                statmap.put(value, weight);
            }
        }
    }
    
    public LinearReport(UniqueList<Map<Content,String>> interviewList, Content content)
    {
        double weight = 1.0;
        this.weightcontent=null;
            
        this.totalSize = ContentUtils.countWeights(weightcontent, interviewList);
         
        this.content=content;
        List<String> values = new ArrayList<>();
        for(Map<Content,String> valmap : interviewList )
        {
            if (weightcontent!=null)
            {
                String wstr = valmap.get(weightcontent);
                try
                {
                    weight = Double.parseDouble(wstr);
                }
                catch (NumberFormatException nfe)
                {
                    weight = 1.0;
                }
            }
            String value = valmap.get(content);
            if (value!=null)
            {
                values.add(value);
                uniqvalues.add(value);
            }
        }
        for (String value: values)
        {
            if (statmap.containsKey(value))
            {
                Double numb = statmap.get(value);
                statmap.put(value, numb+weight);
            }
            else
            {
                    statmap.put(value, weight);
            }
        }
    }

      
}
