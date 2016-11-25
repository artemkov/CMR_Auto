/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 *
 * @author Артем Ковалев
 */
public class ArithmeticMeanReport 
{
    public static final double DEFAULTCONFINTCOEFF=1.00;
    public static final double DEFAULTCONFINT=0.95;
    Content weightContent;
    Content maincontent;
    List<String>rownamesList = new ArrayList<>();
    List<Double>meanList = new ArrayList<>();
    List<Double>meanErrorList = new ArrayList<>();
    List<Double>varianceList = new ArrayList<>();
    List<Double>semeanList = new ArrayList<>();
    Double[][] daValueMatrix;
    List<String> excludeList = null;
    //List<Double[]>debugValList = new ArrayList<>();
    
    public ArithmeticMeanReport (UniqueList<Map<Content,String>> interviews, Content content1, Properties properties, TemplateNode<String> rootNode) 
    {
        if (content1!=null)
            maincontent=content1;
        
        TreeMap<Integer,String> contentnamesmap = getContentNamesMap(properties);
        //TreeMap<Integer,String> basestringmap = getFilterBasesMap(properties);
        TreeMap<Integer,String> rownamesmap = getRowNamesMap(properties);
        String weightContentName = getWeightContent(properties);
        
        if (properties.containsKey("ExcludeList"))
        {
            String excludeListStr = properties.getProperty("ExcludeList");
            excludeList = Arrays.asList(excludeListStr.split("[,;]"));
        }
        if (weightContentName!=null)
        {
            weightContent=ContentUtils.getContentByNameFromInterviewList(interviews, weightContentName);
        }
        
        
        
        List<String>varList;
        for (Map.Entry<Integer,String> entry: contentnamesmap.entrySet())
        {
            double corrector = 0.0;
            double value = 0.0,errorvalue=0.0, variance=0.0, semean=0.0;
            Double debugVals[];
            Integer index = entry.getKey();
            String cstring = entry.getValue();
            if (cstring==null)
                continue;
            String ptrn = "^\\s*([_a-zA-Zа-яА-Я]\\w*\\s*([\\+\\-]\\s*([_a-zA-Zа-яА-Я]\\w*|\\d+(.\\d+)*)\\s*)+)$";
            if (cstring.matches(ptrn))
            {
                varList = Arrays.asList(cstring.split("[\\+\\-]"));
                String[] operArray = cstring.split("\\w+");
                List<Content>contList = new ArrayList<>();
                List<String>opList = new ArrayList<>();
                /*for(String operstr: operArray)
                {
                    if (operstr.isEmpty()||operstr.equals("+"))
                        opList.add("+");
                    else if (operstr.equals("-"))
                        opList.add("-");
                }*/
                int count=0;
                for (String var: varList)
                {
                    if (var.trim().matches("\\d+(.\\d+)*"))
                    {
                        if (operArray[count].isEmpty()||operArray[count].equals("+"))
                            corrector += Double.parseDouble(var);
                        else if (operArray[count].equals("-"))
                            corrector-=Double.parseDouble(var);
                    }
                    else
                    {
                        Content c = ContentUtils.getContentByNameFromInterviewList(interviews, var);
                        contList.add(c);
                        String op = operArray[count];
                        if (op.isEmpty())
                            op="+";
                        opList.add(op);
                    }
                    count++;
                }
                
                
                value = getMean(interviews, contList, opList)+corrector;
                variance = getVariance(interviews, contList, opList, value, -corrector);
                semean = getSEMean(variance, getInterviewsWeight(interviews,contList));
                
                
                //errorvalue = getMeanError(interviews, contList, opList, value, -corrector);
            }
            else if (cstring.matches("^\\s*([_a-zA-Zа-яА-Я]\\w*)\\s*$"))
            {
                Content c = ContentUtils.getContentByNameFromInterviewList(interviews, cstring);
                List<Content>contList = new ArrayList<>();
                if (c==null)
                    value = 0.0;
                else
                {
                    contList.add(c);
                    List<String>opList = new ArrayList<>();
                    opList.add("+");
                    value = getMean(interviews, contList, opList);
                    variance = getVariance(interviews, contList, opList, value, -corrector);
                    semean = getSEMean(variance, getInterviewsWeight(interviews,contList));
                    
                    //errorvalue = getMeanError(interviews, contList, opList, value, -corrector);
                }
            }
            
            rownamesList.add(rownamesmap.get(index));
            meanList.add(value);
            varianceList.add(variance);
            semeanList.add(semean);
        }
        
    }
    public double countSampleWeight(UniqueList<Map<Content,String>> interviews)
    {
        double interviewsweight=0.0;
    
        for (Map<Content,String> interview: interviews)
        {
            double weight=1.0;
            if (weightContent!=null)
            {
                try
                {
                    weight=Double.parseDouble(interview.getOrDefault(weightContent, "0"));
                    
                }
                catch (NumberFormatException e)
                {
                    
                }
            }
            interviewsweight+=weight;
        }
        return interviewsweight;
    }
    private double getInterviewsWeight(UniqueList<Map<Content,String>> interviews, List<Content> contentList)
    {
        double interviewsweight=0.0;
        for (Map<Content,String> interview: interviews)
        {
            double weight = 1.0;
            double localSum = 0.0;
            int count = 0;
            if (weightContent!=null)
            {
                try
                {
                    weight=Double.parseDouble(interview.getOrDefault(weightContent, "0"));
                }
                catch (NumberFormatException e)
                {
                    
                }
            }
            Content c = contentList.get(0);
            if (c==null)
            {
                interviewsweight+=weight;
                continue;
            }
            
            String valstr = interview.getOrDefault(c, "NV");
            if (valstr.isEmpty())
                valstr = "NV";
            
            //Обработка листа исключенных значений. Только для первой переменной
            if ("NV".equals(valstr))
                System.out.print("");
            if (("NV".equals(valstr))||(excludeList!=null&&excludeList.contains(valstr)))
            {
                continue;
            }
            interviewsweight+=weight;
        }
        return interviewsweight;
    }
    
    private double getMean(UniqueList<Map<Content,String>> interviews, List<Content> contentList, List<String> opList)
    {
        double sum = 0.0;
        double interviewsweight=getInterviewsWeight(interviews, contentList);
        for (Map<Content,String> interview: interviews)
        {
            double weight = 1.0;
            double localSum = 0.0;
            int count = 0;
            String firstval = interview.getOrDefault(contentList.get(0), "NV");
            //Обработка листа исключенных значений.
            if (excludeList!=null&&excludeList.contains(firstval))
            {
                continue;
            }
            //Ищем вес анкеты, если есть
            if (weightContent!=null)
            {
                try
                {
                    weight=Double.parseDouble(interview.getOrDefault(weightContent, "0.0"));
                }
                catch (NumberFormatException e)
                {
                    
                }
            }
            
            for (Content c: contentList)
            {
                
                if (c==null)
                    continue;
                
                String valstr = interview.getOrDefault(c, "NV");
                if (valstr.isEmpty())
                    valstr="NV";
                Double valdouble = 0.0;
                try
                {
                    
                    valdouble = Double.parseDouble(valstr)*weight;
                }
                catch (NumberFormatException e)
                {
                    
                }
                if (opList.get(count).equals("+"))
                {
                    localSum+=valdouble;
                }
                else
                {
                    localSum-=valdouble;
                }
                count++;
            }
            sum+=localSum;
        }
        return sum/interviewsweight;
    }
    
    private double getVariance (UniqueList<Map<Content,String>> interviews, List<Content> contentList, List<String> opList, double mean, double corrector)
    {
        double sum = 0.0;
        double interviewsweight=getInterviewsWeight(interviews, contentList);
        for (Map<Content,String> interview: interviews)
        {
            double weight = 1.0;
            double localSum = 0.0;
            int count = 0;
            String firstval = interview.getOrDefault(contentList.get(0), "NV");
            //Обработка листа исключенных значений.
            if (excludeList!=null&&excludeList.contains(firstval))
            {
                continue;
            }
            //Ищем вес анкеты, если есть
            if (weightContent!=null)
            {
                try
                {
                    weight=Double.parseDouble(interview.getOrDefault(weightContent, "0.0"));
                }
                catch (NumberFormatException e)
                {
                    
                }
            }
            
            for (Content c: contentList)
            {
                
                if (c==null)
                    continue;
                
                String valstr = interview.getOrDefault(c, "NV");
                if (valstr.isEmpty())
                    valstr="NV";
                Double valdouble = 0.0;
                try
                {
                    valdouble = Double.parseDouble(valstr)*weight;
                }
                catch (NumberFormatException e)
                {
                    
                }
                if (opList.get(count).equals("+"))
                {
                    localSum+=valdouble;
                }
                else
                {
                    localSum-=valdouble;
                }
                count++;
            }
            sum+=Math.pow(localSum-(mean+corrector), 2);
        }
        return Math.sqrt(sum/(interviewsweight));
    }
    
    
    
    
    private double getSEMean (double variance, double interviewsweight)
    {
        if (interviewsweight>1)
            return DEFAULTCONFINTCOEFF*(variance/Math.sqrt(interviewsweight-1));
        return 0.0;
    }
    
    private Double[] getDebugValue (double mean1, double mean2, double semean1, double semean2, double iweight1, double iweight2, int bonferronin) 
    {
        return ReportUtils.getStudentDAValDebug(mean1, mean2, semean1 , semean2 , DEFAULTCONFINT, iweight1+iweight2, bonferronin);
    }
    
    
    private double getMeanError(UniqueList<Map<Content,String>> interviews, List<Content> contentList, List<String> opList, double mean, double corrector)
    {
        double sum = 0.0,res=0.0;
        double interviewsweight=0.0;
        for (Map<Content,String> interview: interviews)
        {
            double weight = 1.0;
            double localSum = 0.0;
            int count = 0;
            if (weightContent!=null)
            {
                try
                {
                    weight=Double.parseDouble(interview.getOrDefault(weightContent, "0"));
                }
                catch (NumberFormatException e)
                {
                    
                }
            }
            interviewsweight+=weight;
            for (Content c: contentList)
            {
                
                if (c==null)
                    continue;
                String valstr = interview.getOrDefault(c, "NV");
                try
                {
                    Double.parseDouble(valstr);
                }
                catch (NumberFormatException e)
                {
                    System.out.print("");
                }
                
                if (valstr.isEmpty())
                    valstr="NV";
                Double valdouble = 0.0;
                try
                {
                    //Обработка листа исключенных значений. Только если среднее по одной переменной
                    if ((excludeList!=null&&excludeList.contains(valstr)&&contentList.size()==1)||("NV".equals(valstr)))
                    {
                        //Вычитаем вес интервью из общего веса
                        interviewsweight-=weight;
                        //Переходим к следующему интервью
                        break;
                    }
                    valdouble = Double.parseDouble(valstr)*weight;
                }
                catch (NumberFormatException e)
                {
                    
                }
                if (opList.get(count).equals("+"))
                {
                    localSum+=valdouble;
                }
                else
                {
                    localSum-=valdouble;
                }
                count++;
            }
            sum+=Math.pow(localSum-(mean+corrector), 2);
        }
        res=DEFAULTCONFINTCOEFF*Math.sqrt(sum/(interviewsweight-1)/interviewsweight);
        return res;
        
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
        if (tempmap.isEmpty()&(maincontent!=null))
            tempmap.put(1, maincontent.getName());
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
        if (tempmap.isEmpty()&(maincontent!=null))
            tempmap.put(1,"Среднее для "+maincontent.getName());
        return tempmap;
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
