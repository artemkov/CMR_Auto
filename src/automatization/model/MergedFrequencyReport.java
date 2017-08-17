/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.GroupsFileNotFoundException;
import automatization.exceptions.InvalidFilterException;
import automatization.exceptions.InvalidGroupsFileFormatException;
import automatization.exceptions.InvalidTemplateFileFormatException;
import automatization.exceptions.NoSampleDataException;
import automatization.exceptions.ReportFileNotFoundException;
import automatization.exceptions.ReportParamsNotDefinedException;
import automatization.exceptions.VariableNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 *
 * @author Артем Ковалев
 */
public class MergedFrequencyReport 
{
    private String weightContentName=null;

    public String getWeightContentName() 
    {
        return weightContentName;
    }
    static class MFREntry
    {
        int index;
        UniqueList<Map<Content,String>> base;
        UniqueList<Map<Content,String>> filteredbase;
        String rowname;
        Content weightContent;
        String dastring;

        public MFREntry(UniqueList<Map<Content, String>> base, UniqueList<Map<Content, String>> filteredbase, String rowname,int index, Content wc) {
            this.base = base;
            this.filteredbase = filteredbase;
            this.rowname=rowname;
            weightContent = wc;
            this.index=index;
        }
        
        public Double getFrequency()
        {
            
            if (weightContent==null)
            {
                if ((base!=null)&&(!base.isEmpty()))
                    return filteredbase.size()*100.0/base.size();
                else
                    return 0.0;
            }
            else
            {
                
                double baseWeight = ContentUtils.countWeights(weightContent.getName(), base);
                double filterWeight = ContentUtils.countWeights(weightContent.getName(), filteredbase);
                if (baseWeight!=0)
                {
                    return filterWeight*100.0/baseWeight;
                }
                else
                {
                    return 0.0;
                }
                
            }
        }
        public Double getValue()
        {
            if (weightContent==null)
            {
                if ((base!=null)&&(!base.isEmpty()))
                    return (double)filteredbase.size();
                else
                    return 0.0;
            }
            else
            {
                double filterWeight = ContentUtils.countWeights(weightContent.getName(), filteredbase);
                return filterWeight;
            }
        }
    }
    
    private List<MergedFrequencyReport.MFREntry> repentrylist = new ArrayList<>();
    
    public MergedFrequencyReport (UniqueList<Map<Content,String>> interviews, Content content1, Properties properties, TemplateNode<String> rootNode) throws GroupsFileNotFoundException, InvalidTemplateFileFormatException, ReportParamsNotDefinedException, VariableNotFoundException, InvalidFilterException, IOException, ReportFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException
    {
        TreeMap<Integer,String> filtermap = null;
        TreeMap<Integer,String> basemap = null;
        TreeMap<Integer,String> rownamesmap=null;
        //Если не указаны Rownames
        if (properties.stringPropertyNames().contains("ByVariable"))
        {
            Content byVaryable = ContentUtils.getContentByNameFromInterviewList(interviews, properties.getProperty("ByVariable"));
            if (byVaryable!=null)
            {
                int rowcounter = 1;
                rownamesmap=new TreeMap<>();
                filtermap=new TreeMap<>();
                for (Map.Entry<String,String> entry: byVaryable.getAnswerCodeMap().entrySet())
                {
                    rownamesmap.put(rowcounter, entry.getValue());
                    filtermap.put(rowcounter, byVaryable.getName()+"("+entry.getKey()+")");
                    rowcounter++;
                }    
            }
        }
        else
        {
            rownamesmap = getRowNamesMap(properties);
            filtermap = getFiltersMap(properties);
            basemap = getBasesMap(properties);
        }
        
        
        List<Integer> dalist = getDAInternalList(properties);
        weightContentName = this.getWeightContent(properties);
        Content weightContent = ContentUtils.getContentByNameFromInterviewList(interviews,weightContentName);
        
        
        for (Map.Entry<Integer,String> entry: filtermap.entrySet())
        {
            int index = entry.getKey();
            String filter = entry.getValue();
            if ((filter==null)||(filter.isEmpty()))
                continue;
            String baseString = basemap.getOrDefault(index, "All");
            UniqueList<Map<Content,String>> base = Filter.filter(interviews, baseString);
            UniqueList<Map<Content,String>> filteredbase = Filter.filter(base, filter);
            String rowname = rownamesmap.getOrDefault(index, "");
            if (rowname.isEmpty())
                rowname=filter;
            repentrylist.add(new MFREntry(base, filteredbase, rowname,index,weightContent));
        }
        getDAStringList(rownamesmap,dalist);
        
    }
    
    public int getIndexByName(String rowname)
    {
        for (MFREntry entry: repentrylist)
        {
            if (entry.rowname.equals(rowname))
            {
                return entry.index;
            }
        }
        return -1;
    }
    
    public Map<String,String> getDAStringsMap(Properties properties)
    {
        Map<String,String> getDAStringsMap = new HashMap<>();
        List<Integer> dalist = getDAInternalList(properties);
        for (MFREntry entry: repentrylist)
        {
            if (dalist.contains(entry.index)) 
                getDAStringsMap.put(entry.rowname, entry.dastring);
        }
        return getDAStringsMap;
    }
    private MFREntry findRepEntryByIndexFromCFRList(int index)
    {
        if (repentrylist==null)
            return null;
        for (MFREntry mfrentry: repentrylist)
        {
            if (mfrentry.index==index)
                return mfrentry;
        }
        return null;
    }
    
    public void getDAStringList(TreeMap<Integer,String> contentnamesmap, List<Integer> dalist)
    {
        
        List<String> strlist = new ArrayList<>();
        List<MergedFrequencyReport.MFREntry> entrylist = new ArrayList<>();
        if ((repentrylist==null)||(dalist==null))
        {
            return;
        }
        
        for (Integer index: dalist)
        {
            MFREntry entry = findRepEntryByIndexFromCFRList(index);
            if (entry!=null)
                entrylist.add(entry);
        }
        
        
        for (MFREntry entry1: entrylist)
        {
            String dastr = "";
            
            Double entry1value = entry1.getFrequency();
            Double base1count = ContentUtils.countWeights(weightContentName, entry1.base);
            for (MFREntry entry2: entrylist)
            {
                if (entry2!=entry1)
                {
                    Double entry2value = entry2.getFrequency();
                    Double normDAVal = ReportUtils.getNormDAVal(entry1value, entry2value, base1count, base1count);
                    if ((normDAVal!=null)&&(normDAVal>0))
                    {
                        dastr+=" >"+(entry2.index);
                    }
                }
            }
            entry1.dastring= dastr;
            
            strlist.add(dastr);
        }
    }
    
    private TreeMap<Integer,String> getBasesMap (Properties properties)
    {
        TreeMap<Integer,String> tempmap = new TreeMap<>();
        for (String key: properties.stringPropertyNames())
        {
            if (key.matches("base\\d+"))
            {
                Integer keyint = Integer.parseInt(key.replace("base",""));
                String value = properties.getProperty(key);
                tempmap.put(keyint, value);
            }
        }
        return tempmap;
    }
    
    private TreeMap<Integer,String> getFiltersMap (Properties properties)
    {
        TreeMap<Integer,String> tempmap = new TreeMap<>();
        for (String key: properties.stringPropertyNames())
        {
            if (key.matches("filter\\d+"))
            {
                Integer keyint = Integer.parseInt(key.replace("filter",""));
                String value = properties.getProperty(key);
                tempmap.put(keyint, value);
                
            }
        }
        return tempmap;
    }
    
    private TreeMap<Integer,String> getRowNamesMap (Properties properties) throws VariableNotFoundException
    {
        TreeMap<Integer,String> tempmap = new TreeMap<>();
        for (String key: properties.stringPropertyNames())
        {
            if (key.matches("rowname\\d+"))
            {
                Integer keyint = Integer.parseInt(key.replace("rowname",""));
                String value = properties.getProperty(key);
                tempmap.put(keyint, value);
            }
        }
        return tempmap;
    }
    private List<Integer> getDAInternalList(Properties properties) 
    {
        String dastring = null;
        List<Integer> dalist = new ArrayList<>();
        for (String key: properties.stringPropertyNames())
        {
            if (key.matches("(d|D)ainternal"))
            {
                dastring = properties.getProperty(key);
                String arr[] = dastring.split(",\\s*");
                for (String val:arr)
                {
                    try
                    {
                        Integer a = Integer.parseInt(val.trim());
                        dalist.add(a);
                    }
                    catch (NumberFormatException e)
                    {
                        continue;
                    }
                }
            }
        }
        return dalist;
        
    }
    
    public List<Double> getFrequencies()
    {
        List<Double> retlist = new ArrayList<>();
        for (MergedFrequencyReport.MFREntry mfrentry: repentrylist)
        {
            retlist.add(mfrentry.getFrequency());
        }
        return retlist;
    }
    
    public List<Double> getCounts()
    {
        List<Double> retlist = new ArrayList<>();
        for (MergedFrequencyReport.MFREntry mfrentry: repentrylist)
        {
            retlist.add(mfrentry.getValue());
        }
        return retlist;
    }
    
    public List<String> getRowNames()
    {
        List<String> retlist = new ArrayList<>();
        for (MergedFrequencyReport.MFREntry mfrentry: repentrylist)
        {
            retlist.add(mfrentry.rowname);
        }
        return retlist;
    }
    
    private String getWeightContent(Properties properties) 
    {
        for (String key: properties.stringPropertyNames())
        {
            if (key.matches("(w|W)eight(c|C)ontent"))
            {
                return properties.getProperty(key);
            }
        }
        return null;
    }
}
