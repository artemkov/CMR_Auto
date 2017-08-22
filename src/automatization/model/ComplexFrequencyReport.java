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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Дина
 */
public class ComplexFrequencyReport 
{

    private String weightContentName=null;
    
    public String getWeightContentName() 
    {
        return weightContentName;
    }

    

    
    
    static class CFREntry
    {
        int index;
        Content content;
        Content weightContent;
        UniqueList<Map<Content,String>> base;
        List<AnswerGroup> aglist;
        String firstGroupName;
        String dastring;
        
        

        public CFREntry(int index, Content content,Content wc, UniqueList<Map<Content, String>> base, List<AnswerGroup> aglist) {
            this.content = content;
            this.weightContent = wc;
            this.base = base;
            this.aglist = aglist;
            this.index = index;
        }
        
        
        public double getFrequency()
        {
            if ((content!=null)&&(base!=null)&&(!base.isEmpty())&&aglist!=null)
            {
                //GroupsReport gr = new GroupsReport(aglist);
                
                WeightedGroupReport wgr = new WeightedGroupReport(aglist,weightContent);
                
                try 
                {
                    //gr.populateGroups(base, content.getName());
                    
                    wgr.populateGroups(base, content.getName());
                    firstGroupName=wgr.getGroups().get(0).getName();
                } 
                catch (VariableNotFoundException ex) 
                {
                    System.out.println("CFREntry::getFrequency()  Variable "+content.getName()+" was not found");
                }
                
                double wvalue = wgr.getWeightedCountmap().getOrDefault(wgr.getGroups().get(0), 0.0);
                return wvalue*100/wgr.getTotalWeight();
            }
            return 0.0;
        }
        
        public double getCount()
        {
            if ((content!=null)&&(base!=null)&&(!base.isEmpty())&&aglist!=null)
            {
                
                
                WeightedGroupReport wgr = new WeightedGroupReport(aglist,weightContent);
                
                try 
                {
                    
                    wgr.populateGroups(base, content.getName());
                    firstGroupName=wgr.getGroups().get(0).getName();
                } 
                catch (VariableNotFoundException ex) 
                {
                    System.out.println("CFREntry::getFrequency()  Variable "+content.getName()+" was not found");
                }
                double wvalue = wgr.getWeightedCountmap().getOrDefault(wgr.getGroups().get(0), 0.0);
                return wvalue;
            }
            return 0.0;
        }
        
    }
    
    private List<CFREntry> repentrylist = new ArrayList<>();
    
    
    public ComplexFrequencyReport (UniqueList<Map<Content,String>> interviews, Content content1, Properties properties, TemplateNode<String> rootNode) throws GroupsFileNotFoundException, InvalidTemplateFileFormatException, ReportParamsNotDefinedException, VariableNotFoundException, InvalidFilterException, IOException, ReportFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException
    {
        TreeMap<Integer,String> contentnamesmap = getContentNamesMap(properties);
        TreeMap<Integer,String> basestringmap = getFilterBasesMap(properties);
        TreeMap<Integer,String> groupsfilemap = getGroupFileNamesMap(properties);
        TreeMap<Integer,String> rownamesmap = getRowNamesMap(properties);
        List<Integer> dalist = getDAInternalList(properties);
        weightContentName = getWeightContent(properties);
        
        for (Map.Entry<Integer,String> entry: contentnamesmap.entrySet())
        {
            Integer index = entry.getKey();
            String cname = entry.getValue();
            if (cname==null)
                continue;
            Content con = ContentUtils.getContentByNameFromInterviewList(interviews, cname);
            Content weightContent = ContentUtils.getContentByNameFromInterviewList(interviews, weightContentName);
            if (con==null)
                continue;
            String filterString = basestringmap.getOrDefault(index, "All");
            UniqueList<Map<Content,String>> base = Filter.filter(interviews, filterString);
            String filename = groupsfilemap.getOrDefault(index, "NoFilename");
            String rowname = rownamesmap.getOrDefault(index, "");
            List<AnswerGroup> aglist = null;
            if (filename.equals("NoFilename"))
            {
                
                List<AnswerGroup> aglist2=GroupsReport.constructAnswerGroupsFormContent(con, true);
                if (aglist2==null)
                    System.out.println("");
                aglist = GroupsReport.constructAnswerGroupsFormContent(con, true).subList(0, 1);
                
            }
            else
            {
                aglist = GroupsReport.getAnswerGroupsFromExcel(Paths.get(filename));
            }
            if (!rowname.isEmpty())
            {
                aglist.get(0).setName(rowname);
            }
            
            CFREntry cfrentry = new CFREntry(index,con, weightContent, base, aglist);
            repentrylist.add(cfrentry);
        }
        getDAStringList(contentnamesmap,dalist);
    }
    public void getDAStringList(TreeMap<Integer,String> contentnamesmap, List<Integer> dalist)
    {
        
        List<String> strlist = new ArrayList<>();
        List<CFREntry> entrylist = new ArrayList<>();
        if ((repentrylist==null)||(dalist==null))
        {
            return;
        }
        
        for (Integer index: dalist)
        {
            CFREntry entry = findRepEntryByIndexFromCFRList(index);
            if (entry!=null)
                entrylist.add(entry);
        }
        
        
        for (CFREntry entry1: entrylist)
        {
            String dastr = "";
            
            Double entry1value = entry1.getFrequency();
            Double base1count = ContentUtils.countWeights(weightContentName, entry1.base);
            for (CFREntry entry2: entrylist)
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
    private CFREntry findRepEntryByIndexFromCFRList(int index)
    {
        if (repentrylist==null)
            return null;
        for (CFREntry cfrentry: repentrylist)
        {
            if (cfrentry.index==index)
                return cfrentry;
        }
        return null;
    }
    
    public List<Double> getFrequencies()
    {
        List<Double> retlist = new ArrayList<>();
        for (CFREntry cfrentry: repentrylist)
        {
            retlist.add(cfrentry.getFrequency());
        }
        return retlist;
    }
    
    public List<String> getVarNames()
    {
        List<String> retlist = new ArrayList<>();
        for (CFREntry cfrentry: repentrylist)
        {
            retlist.add(cfrentry.content.getName());
        }
        return retlist;
    }
    
    public List<String> getGroupNames()
    {
        List<String> retlist = new ArrayList<>();
        for (CFREntry cfrentry: repentrylist)
        {
            retlist.add(cfrentry.firstGroupName);
        }
        return retlist;
    }
    
    public List<Double> getCounts() 
    {
        List<Double> retlist = new ArrayList<>();
        for (CFREntry cfrentry: repentrylist)
        {
            retlist.add(cfrentry.getCount());
        }
        return retlist;
    }
    
    private TreeMap<Integer,String> getContentNamesMap (Properties properties)
    {
        TreeMap<Integer,String> tempmap = new TreeMap<>();
        for (String key: properties.stringPropertyNames())
        {
            if (key.matches("content\\d+"))
            {
                Integer keyint = Integer.parseInt(key.replace("content",""));
                String value = properties.getProperty(key);
                tempmap.put(keyint, value);
            }
        }
        return tempmap;
    }
    
    private TreeMap<Integer,String> getFilterBasesMap (Properties properties)
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
    
    private TreeMap<Integer,String> getGroupFileNamesMap (Properties properties)
    {
        TreeMap<Integer,String> tempmap = new TreeMap<>();
        for (String key: properties.stringPropertyNames())
        {
            if (key.matches("groupfile\\d+"))
            {
                Integer keyint = Integer.parseInt(key.replace("groupfile",""));
                String value = properties.getProperty(key);
                tempmap.put(keyint, value);
            }
        }
        return tempmap;
    }
    
    private TreeMap<Integer,String> getRowNamesMap (Properties properties)
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
