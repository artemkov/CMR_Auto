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
 * @author Дина
 */
public class MeanReport 
{
    private String varName;
    Double mean=0.0;
    Integer sampleSize=0;
    
    public MeanReport (UniqueList<Map<Content,String>> interviews, String varName, List<String> excludeList)
    {
        Content content = ContentUtils.getContentByNameFromInterviewList(interviews, varName);
        //список значений переменной
        List<String> values = new ArrayList<>();
        TreeSet<String> uniqvalues = new TreeSet<>();
        for(Map<Content,String> valmap : interviews )
        {   
            String value = valmap.get(content);
            if (value!=null)
            {
                values.add(value);
                uniqvalues.add(value);
            }
            
        }
        //карта линейной статистики ответов
        Map<String,Integer> statmap = new HashMap<>();
        for (String value: values)
        {
            if (statmap.containsKey(value))
            {
                Integer numb = statmap.get(value);
                statmap.put(value, numb+1);
            }
            else
            {
                statmap.put(value, 1);
            }
        }
        
        //Расчет среднего значения
        double weightedval=0;
        for (Map.Entry<String,Integer> entry: statmap.entrySet())
        {
            String key = entry.getKey();
            if ((excludeList!=null)&&(excludeList.contains(key)))
                continue;
            try
            {
                Double intkey = Double.parseDouble(key);
                sampleSize+=entry.getValue();
                weightedval+=entry.getValue()*intkey;
            }
            catch (NumberFormatException e)
            {
                continue;
            }
           
        }
        mean = (weightedval)/sampleSize;
    }
}
