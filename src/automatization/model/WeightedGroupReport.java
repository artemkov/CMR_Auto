/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automatization.model;

import automatization.exceptions.VariableNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Дина
 */
public class WeightedGroupReport extends GroupsReport
{
    Content weightContent;
    double totalWeight = 0.0;
    Map<AnswerGroup,Double> weightedCountmap = new HashMap<>();
    //Map<AnswerGroup,Integer> fictivevaluecountermap = new HashMap<>();
    public WeightedGroupReport(List<AnswerGroup> groups, Content wc) 
    {
        super(groups);
        weightContent=wc;
    }

    public double getTotalWeight() 
    {
        return totalWeight;
    }
    
    
    
    public double getGroupedWeightedTotal()
    {
        double totweight=0.0;
        for (AnswerGroup agroup: getGroups())
        {
            totweight += weightedCountmap.getOrDefault(agroup, 0.0);
        }
        return totweight;
    }
    
    /*public Map<AnswerGroup, Double> getFictiveWeightedFrequencymap()
    {
        Map<AnswerGroup, Double> fictiveWeightedFrequencymap = new HashMap<>();
        for (AnswerGroup agroup: getGroups())
        {
            fictiveWeightedFrequencymap.put(agroup 
                    , getFictiveValueCountermap().getOrDefault(agroup, 0)/getWeightedTotal());
        }
        return fictiveWeightedFrequencymap;
    }*/

    public Map<AnswerGroup, Double> getWeightedCountmap() {
        return weightedCountmap;
    }

    /*public Map<AnswerGroup, Integer> getFictiveValueCountermap() {
        return fictivevaluecountermap;
    }*/
    
    
    
    @Override
    public void populateGroups(UniqueList<Map<Content,String>> interviews, String varName) throws VariableNotFoundException
    {
        Set<Content> cset=null;
        Content content=ContentUtils.getContentByNameFromInterviewList(interviews, varName);
        
        
        
            
        
        //Defining MapEntries
        for (AnswerGroup agroup: getGroups())
        {
            weightedCountmap.put(agroup, 0.0);
            valuecountermap.put(agroup, 0.0);
        }
        
        if (content==null)
        {
            return;
        }
        
        countWeightedGroup(interviews,content);
        
        
    }
    
    private void countWeightedGroup(List<Map<Content,String>> interviews,Content content)
    {
        int total=0;
        double totweight = 0.0;
        for (Map<Content,String> interview: interviews)
        {
            String value = interview.get(content);
            
            double weight=1.0;
            if (weightContent!=null)
            {
                try
                {
                    String weightVal = interview.get(weightContent);
                    if (weightVal!=null)
                    {
                        weight = Double.parseDouble(weightVal);
                    }
                }
                catch (Exception e)
                {
                    
                }
            }
            for (AnswerGroup agroup: getGroups())
            {
                if (agroup.isAnswerInGroup(value))
                {
                    double val = weightedCountmap.get(agroup);
                    weightedCountmap.put(agroup, val+weight);
                    
                    double dobVal = valuecountermap.get(agroup);
                    valuecountermap.put(agroup, dobVal+1);
                }
            }
            total++;
            totweight+=weight;
        }
        this.totalWeight = totweight;
        
        /*for (AnswerGroup agroup: getGroups())
        {
            grweight += weightedCountmap.getOrDefault(agroup, 0.0);
        }
        
        for (AnswerGroup agroup: getGroups())
        {
            float fictiveFrequency = (float) (weightedCountmap.get(agroup)/totweight);
            
            this.fictivevaluecountermap.put(agroup, Math.round(total*fictiveFrequency));
        }*/
    }
    
}
