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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Дина
 */
public class GroupsCrossReport 
{
    private Content weightContent;
    private Content content1;
    private Content content2;
    private Double[][] data;
    private Path groupfile1;
    private Path groupfile2;
    private List<InterviewGroup> igroup1 = null;
    private List<InterviewGroup> igroup2 = null;
    private boolean agrop1fictive = false;
    private boolean agrop2fictive = false;
    private boolean showpercentage = false;

    public Content getWeightContent() {
        return weightContent;
    }

    
    public Content getContent1() {
        return content1;
    }

    public Content getContent2() {
        return content2;
    }

    public Double[][] getData() {
        return data;
    }

    public List<InterviewGroup> getAgroup1() {
        return igroup1;
    }
    
    public List<InterviewGroup> getIgroup1() {
        return igroup1;
    }

    public List<InterviewGroup> getAgroup2() {
        return igroup2;
    }
    
    public List<InterviewGroup> getIgroup2() {
        return igroup2;
    }
    
    
    
    private Double[][] populateTable (UniqueList<Map<Content,String>> interviewList) throws InvalidFilterException
    {
        double weight=1.0;
        
        double baseweight=0.0; 
        if (weightContent==null)
        {
            baseweight = interviewList.size();
        }
        else
        {
            baseweight = ContentUtils.countWeights(weightContent.getName(), interviewList);
        }
        if ((content1!=null)&&(content2!=null))
        {   
            Double[][] countData = new Double[igroup1.size()][igroup2.size()];
            for (Double[] doba: countData)
                Arrays.fill(doba,0.0);
            
            int i=0;
            int j=0;
            
            
            for (Map<Content,String>iview:interviewList)
            {
                    try
                    {
                        if (weightContent!=null)
                            weight = Double.parseDouble(iview.get(weightContent));
                    }
                    catch (NumberFormatException e)
                    {
                        
                    }
                    //String value1 = iview.get(content1);
                    //String value2 = iview.get(content2);
                    i=0;
                    for (InterviewGroup group: igroup1)
                    {
                        if (group.isInterviewInGroup(iview))
                        {
                            j=0;
                            for (InterviewGroup group2: igroup2)
                            {
                                if (group2.isInterviewInGroup(iview))
                                {
                                    if (!showpercentage)
                                    {
                                        countData[i][j]+=weight;
                                    }
                                    else
                                    {
                                        countData[i][j]+=weight/baseweight*100.0;
                                    }
                                }
                                j++;
                            }
                        }
                        i++;
                    }
            }
            data=countData;
            return data;
            
            
        }
        return null;
    }
    public GroupsCrossReport(UniqueList<Map<Content,String>> interviews, Content content1, Properties properties, TemplateNode<String> rootNode) throws GroupsFileNotFoundException, InvalidTemplateFileFormatException, ReportParamsNotDefinedException, VariableNotFoundException, InvalidFilterException, IOException, ReportFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException
    {
        this.content1=content1;
        Report secondGroupsReport = null;
        String groupfilename2 = null;
        String groupfilename1 = null;
        String secondvarname = null;
        
        this.weightContent = ContentUtils.getContentByNameFromInterviewList(interviews, getWeightContent(properties));
        
        if (properties.containsKey("showpercentage")&&properties.getProperty("showpercentage").equalsIgnoreCase("true"))
            showpercentage=true;
        //определение параметров для content1
        //задан groupfile1
        if (properties.containsKey("groupfile1")||properties.containsKey("groupfile"))
        {
            if (properties.containsKey("groupfile1"))
                groupfilename1=properties.getProperty("groupfile1");
            else
                groupfilename1=properties.getProperty("groupfile");
            
            groupfile1 = Paths.get(groupfilename1);
            if (!Files.isReadable(groupfile1))
                throw new GroupsFileNotFoundException(groupfile1.toAbsolutePath().toString());
            else
            {
                try 
                {
                    igroup1 = GroupsReport.getInterviewGroupsFromExcel(groupfile1,content1);
                } 
                catch (IOException ex) 
                {
                   throw new GroupsFileNotFoundException(groupfile1.toAbsolutePath().toString());
                }
            }
        }
        //не задан groupfile1, создаем фиктивную группу из вопросов
        else
        {
            igroup1 = GroupsReport.constructInterviewGroupsFormContent(content1);
            if (igroup1==null)
            {
                igroup1 = GroupsReport.constructInterviewGroupsFormContent(content1,interviews,false);
            }
            agrop1fictive = true;
        }
        
        //определение параметров для content2
        //вариант 1. задана метка на Групповой отчет
        if (properties.containsKey("secondtag"))
        {
            String tag = properties.getProperty("secondtag");
            
            TemplateNode<String> node3 = ReportUtils.getReportNodeByTag(rootNode,tag);
                    
            secondvarname = node3.getParent().getParent().getData();
                    
            content2 = ContentUtils.getContentByNameFromInterviewList(interviews, secondvarname);
                    
            secondGroupsReport=new Report ();
            List<UniqueList<Map<Content,String>>> fictiveSampleList = new ArrayList<>();
            fictiveSampleList.add(interviews);
            secondGroupsReport.getMultiSampleReportFromNode(node3, 
                                                            content2, 
                                                            fictiveSampleList, null);
            
            if (secondGroupsReport.hasProperty("groupfile"))
            {
                groupfilename2 = secondGroupsReport.getProperty("groupfile");
                Path groupfilepath2 = Paths.get(groupfilename2);
                if (!Files.isReadable(groupfilepath2))
                    throw new GroupsFileNotFoundException(groupfilepath2.toAbsolutePath().toString());
                else
                {
                    try 
                    {
                        igroup2=GroupsReport.getInterviewGroupsFromExcel(groupfilepath2,content2);
                        
                    } 
                    catch (IOException ex) 
                    {
                        throw new GroupsFileNotFoundException(groupfilepath2.toAbsolutePath().toString());
                    }
                }
                groupfile2=groupfilepath2;
            }
            else
            {
                igroup2 = GroupsReport.constructInterviewGroupsFormContent(content2);
                if (igroup2==null)
                {
                    igroup2 = GroupsReport.constructInterviewGroupsFormContent(content2,interviews,false);
                }
                agrop2fictive = true;
            }
        }
        //вариант 2 указаны: имя второй переменной
        else if (properties.containsKey("secondvar"))
        {
            secondvarname = properties.getProperty("secondvar");
            content2=ContentUtils.getContentByNameFromInterviewList(interviews, secondvarname);
            //вариант 2.1 указаны: имя второй переменной и файл описания групп
            if (properties.containsKey("groupfile2"))
            {
                groupfilename2 = properties.getProperty("groupfile2");
                Path groupfilepath2 = Paths.get(groupfilename2);
                if (!Files.isReadable(groupfilepath2))
                    throw new GroupsFileNotFoundException(groupfilepath2.toAbsolutePath().toString());
                else
                {
                    try 
                    {
                        igroup2 = GroupsReport.getInterviewGroupsFromExcel(groupfilepath2,content2);
                    } 
                    catch (IOException ex) 
                    {
                        throw new GroupsFileNotFoundException(groupfilepath2.toAbsolutePath().toString());
                    }
                }
                groupfile2=groupfilepath2;
            }
            else
            {
                igroup2 = GroupsReport.constructInterviewGroupsFormContent(content2);
                if (igroup2==null)
                {
                    igroup2 = GroupsReport.constructInterviewGroupsFormContent(content2,interviews,false);
                }
                agrop2fictive = true;
            }
        }
        //Неправильные параметры
        else
        {
            throw new ReportParamsNotDefinedException();
        }
        
        //получение таблицы данных
        populateTable(interviews);
                    
          
                
        
        
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
    public boolean isAgrop1fictive() {
        return agrop1fictive;
    }

    public boolean isAgrop2fictive() {
        return agrop2fictive;
    }
    
    
    
}
