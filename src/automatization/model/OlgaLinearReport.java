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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Артем Ковалев
 */
class OlgaLinearReport 
{
    
    List<WeightedInterviewGroupReport> weightedInterviewGroupReportList = new ArrayList<>();
    List<UniqueList<Map<Content,String>>> group1samples = null;
    List<UniqueList<Map<Content,String>>> group2samples = null;
    Map<String,List<String>> normDAMap = new HashMap<>();
    List<ArithmeticMeanReport> meanReportList = new ArrayList<>();
    private final Content content1,content2,content3;
    private final GroupsCrossReport gcr;
    Path groupfile1,groupfile2,groupfile3;
    int variant = 0;
    
    public OlgaLinearReport(UniqueList<Map<Content,String>> interviews, Content content1, Properties properties, TemplateNode<String> rootNode) throws GroupsFileNotFoundException, InvalidTemplateFileFormatException, ReportParamsNotDefinedException, VariableNotFoundException, InvalidFilterException, IOException, ReportFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException
    {
        this.content1=content1;
        //Cтроим кросс часть
        gcr = new GroupsCrossReport(interviews, content1, properties, rootNode);
        content2 = gcr.getContent2();
        List<InterviewGroup> igroup1=gcr.getAgroup1();
        List<InterviewGroup> igroup2=gcr.getAgroup2();
        group1samples=new ArrayList<>();
        group2samples=new ArrayList<>();
        List<InterviewGroup> var3glist = null;
        
        String weightContentName = this.getWeightContent(properties);
        
        String thirdvarname=null;
        if (properties.containsKey("AddAll"))
        {
            if (properties.getProperty("AddAll", "false").equalsIgnoreCase("true"))
            {
                group1samples.add(interviews);
            }
        }
        group1samples.addAll(Filter.splitByInterviewGroups(interviews, igroup1));
        for (int i =0; i<group1samples.size();i++)
        {
            group2samples.addAll(Filter.splitByInterviewGroups(group1samples.get(i),igroup2));
        }
        
        
        if (properties.containsKey("thirdvar"))
        {
            thirdvarname = properties.getProperty("thirdvar");
            content3 = ContentUtils.getContentByNameFromInterviewList(interviews, thirdvarname);
            variant=1;
            if (content3==null)
            {
                throw new VariableNotFoundException(thirdvarname);
            }
            if (properties.containsKey("groupfile3"))
            {
                groupfile3 = Paths.get(properties.getProperty("groupfile3"));
                variant=2;
            }
            
        }
        else
        {
            content3=null;
            if (properties.containsKey("groupfile3"))
            {
                groupfile3 = Paths.get(properties.getProperty("groupfile3"));
                variant=3;
            }
            else
            {
                throw new ReportParamsNotDefinedException("Не указан необходимый параметр 'thirdvar' или 'groupfile3'");
            }
            
        }
        
        //Получение списка групп линейного отчета
        boolean showAnswerText=false;
        if (properties.containsKey("ShowAnswersText"))
        {
            if (properties.getProperty("ShowAnswersText", "false").equalsIgnoreCase("true"))
            {
                    showAnswerText=true;
            }
        }
        switch (variant)
        {
            case 1:
                //Указан только thirdvar (группы создаются по ответам thirdvar)
                var3glist=GroupsReport.constructInterviewGroupsFormContent(content3, interviews, showAnswerText);
                break;
            case 2:
                //Указан thirdvar и groupfile3 (группы берутся из файла groupfile3 в котором указаны ответы на вопросы)
                var3glist=GroupsReport.getInterviewGroupsFromExcel(groupfile3, content3);
                break;
            case 3:
                //Указан только groupfile3 (группы строятся на основании фильтров в groupfile3)
                var3glist=GroupsReport.getInterviewGroupsFromExcel(groupfile3, null);
                break;
        }
        
        //Поиск весовой переменной
        Content weightContent = ContentUtils.getContentByNameFromInterviewList(interviews,weightContentName);
        
        for(UniqueList<Map<Content,String>> sample: group2samples)
        {
            WeightedInterviewGroupReport wgr = new WeightedInterviewGroupReport(var3glist,weightContent);
            wgr.populateGroups(sample);
            weightedInterviewGroupReportList.add(wgr);
        }
        
        //Получение обычных значимостей
        int sample1size = group2samples.size()/group1samples.size();
        for (InterviewGroup group: var3glist)
        {
            List<String> dastringList =new ArrayList<>();
            String gname = group.getName();
            for (int i=0;i<group2samples.size();i++)
                 dastringList.add("");
            for (int k=0;k<group1samples.size();k++)
            {
                for (int i=0; i<sample1size;i++)
                {
                    String dastr ="";
                    WeightedInterviewGroupReport wgr = weightedInterviewGroupReportList.get(k*sample1size+i);
                    Double cursize = wgr.getTotalGroupedWeight();
                    Double curval = cursize!=0? wgr.weightedCountmap.getOrDefault(group, 0.0)/cursize*100:0;
                    for (int j=i+1; j<sample1size;j++)
                    {
                        WeightedInterviewGroupReport compwgr = weightedInterviewGroupReportList.get(k*sample1size+j);
                        Double compsize = compwgr.getTotalGroupedWeight();
                        Double compval = cursize!=0? compwgr.weightedCountmap.getOrDefault(group, 0.0)/compsize*100:0;
                        Double normDAVal = ReportUtils.getNormDAVal(curval, compval, cursize, compsize);
                        if ((normDAVal!=null)&&(normDAVal>0))
                        {
                            ReportUtils.getNormDAVal(curval, compval, cursize, compsize);
                            String val = dastringList.get(k*sample1size+i);
                            val+=" >"+(j+1);
                            dastringList.set(k*sample1size+i,val);
                        }
                        if ((normDAVal!=null)&&(normDAVal<0))
                        {
                            ReportUtils.getNormDAVal(curval, compval, cursize, compsize);
                            String val = dastringList.get(k*sample1size+j);
                            val+=" >"+(i+1);
                            dastringList.set(k*sample1size+j,val);
                        }
                    }
                    
                }
            }
            normDAMap.put(gname, dastringList);
        }
        
        
        
        
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

    public Content getContent1() {
        return content1;
    }

    public Content getContent2() {
        return content2;
    }

    public Content getContent3() {
        return content3;
    }

    public GroupsCrossReport getGcr() {
        return gcr;
    }
    
}
