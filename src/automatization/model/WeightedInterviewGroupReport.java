/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.InvalidFilterException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Артем Ковалев
 */
public class WeightedInterviewGroupReport 
{
    Content content;
    Content weightContent;
    private double totalGroupedWeight = 0.0;
    Map<InterviewGroup,Double> weightedCountmap = new HashMap<>();
    List<InterviewGroup> groupslist = new ArrayList<>();

    public double getTotalGroupedWeight() 
    {
        return totalGroupedWeight;
    }
    
    
    public WeightedInterviewGroupReport (List<InterviewGroup> intgrouplist, Content wc, Content content)
    {
        weightContent=wc;
        groupslist=intgrouplist;
        this.content=content;
    }
    public WeightedInterviewGroupReport (List<InterviewGroup> intgrouplist, Content wc)
    {
        weightContent=wc;
        groupslist=intgrouplist;
        this.content=null;
    }
    
    public WeightedInterviewGroupReport (Path excelFilePath, Content wc, Content content) throws IOException
    {
        weightContent=wc;
        this.content=content;
        groupslist = GroupsReport.getInterviewGroupsFromExcel(excelFilePath, content);
    }
    
    public WeightedInterviewGroupReport (Path excelFilePath, Content wc) throws IOException
    {
        weightContent=wc;
        this.content=null;
        groupslist = GroupsReport.getInterviewGroupsFromExcel(excelFilePath, null);
    }
    
    public void populateGroups(UniqueList<Map<Content,String>> interviews) throws InvalidFilterException
    {
        if (groupslist.isEmpty())
            return;
        if ((interviews==null)||(interviews.isEmpty()))
            return;
        for (Map<Content,String> interview:interviews)
        {
            double interviewweight = ContentUtils.countInterviewWeight(weightContent, interview, 1.0);
            boolean countthis = false;
            for (InterviewGroup gr: groupslist)
            {
                
                if (gr.isInterviewInGroup(interview))
                {
                    Double v = weightedCountmap.getOrDefault(gr, 0.0);
                    v+=interviewweight;
                    weightedCountmap.put(gr, v);
                    countthis=true;
                }
            }
            if (countthis) 
                totalGroupedWeight+=interviewweight;
        }
        //System.out.println(weightedCountmap);
    }
    
    public InterviewGroup findGroupByName(String gname)
    {
        for (InterviewGroup ig: groupslist)
        {
            if (ig.getName().equals(gname))
            {
                return ig;
            }
        }
        return null;
    }
    
}
