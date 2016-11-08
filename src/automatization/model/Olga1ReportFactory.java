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
import static automatization.model.ReportUtils.getBooleanFromProperties;
import static automatization.model.ReportUtils.getDoubleFromProperties;
import static automatization.model.ReportUtils.getStringFromProperties;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Дина
 */
public class Olga1ReportFactory implements ReportFactory
{
    

    @Override
    public Report makeReport(TemplateNode<String> level3node, Content content, Properties properties, List<UniqueList<Map<Content, String>>> sampleList, List<String> sampleNames) throws InvalidTemplateFileFormatException, ReportParamsNotDefinedException, VariableNotFoundException, InvalidFilterException, IOException, GroupsFileNotFoundException, ReportFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException 
    {
        
        Report report = new Report();
        report.addProperties(properties);
        report.setContent(content);
        double universe=1000000.0;
        double confLevel=0.95;
        String weightContentName=null;
        List<OlgaWeightedReport> olrlist = new ArrayList<>();
        TreeSet<String> uniquevallist;
        String volumeheader = null, totalrowheader = null;
        //Главный заголовок
                
        universe = getUniverseFromProperties(universe,properties);
        confLevel = getConflevelFromProperties(confLevel, properties);
        weightContentName = getWeghtcontentnameFromProperties(weightContentName,properties);
        boolean addall = ReportUtils.getBooleanFromProperties("AddAll", false, properties);
        
        for (int i=0; i<sampleList.size(); i++)
        {
            OlgaWeightedReport owr = new OlgaWeightedReport(sampleList.get(i), content, properties, level3node);
            olrlist.add(owr);
            if (i==0)
            {
                //Заголовок базы
                boolean mustdrawtotal = getBooleanFromProperties("drawtotal",true,properties);
                report.setNoFirstString(mustdrawtotal);
                
                //Уникальные значения линейного отчета
                uniquevallist = ContentUtils.getContentUniqueValuesFromSampleList(sampleList, owr.content3);
                
                //размеры отчета
                report.setVolumeWidth(owr.gcr.getAgroup2().size());
                report.setSampleWidth(report.getVolumeWidth()*(owr.group1samples.size()));
                        
                //Заголовок разделов (заголовок 1)
                volumeheader = getStringFromProperties("VOLUMEHEADER", null, properties)==null?owr.content1.getName():
                        getStringFromProperties("VOLUMEHEADER", null, properties);                        
                report.addRowHeader(volumeheader);
                report.addRowType(volumeheader,"VOLUMEHEADER");
                
                
                //Заголовок 2
                totalrowheader =  getStringFromProperties("HEADER", null, properties)==null?owr.content2.getName():
                        getStringFromProperties("HEADER", null, properties);
                report.addRowHeader(totalrowheader);
                report.addRowType(totalrowheader,"HEADER");
                
                
                report.addRowHeader("Размер выборки");
                report.addRowType("Размер выборки","VALUE");
                        
                report.addRowHeader("В группах");
                report.addRowType("В группах","VALUE");
                        
                        
                report.addRowHeader("MEAN "+owr.content3.getName());
                report.addRowType("MEAN "+owr.content3.getName(),"VALUE");
                
                report.addRowHeader("MEAN DA "+owr.content3.getName());
                report.addRowType("MEAN DA "+owr.content3.getName(),"VALUE");
                        
                boolean debugvals = getBooleanFromProperties("DebugVals",false,properties);
                if (debugvals)
                {
                    report.addRowHeader("VARIANCE "+owr.content3.getName());
                }
                    
                report.addRowHeader("SEMEAN "+owr.content3.getName());
                report.addRowType("SEMEAN "+owr.content3.getName(),"VALUE");
                        
                if (debugvals)
                {
                    report.addRowHeader("DIFFERENCE "+owr.content3.getName());
                        
                    report.addRowHeader("STUDENT "+owr.content3.getName());
                            
                    report.addRowHeader("SD");
                }    
                    
                report.addRowHeader("NPS "+owr.content3.getName());
                report.addRowType("NPS "+owr.content3.getName(),"VALUE;DA;PERCENTAGES");
                        
                report.addRowHeader("NPSDA 2s");
                        
                report.addRowHeader("ConfInt");
                        
                        
                //Группы NPS
                List<InterviewGroup> agList = owr.wig_NPSReportList.get(0).groupslist;   
                for (InterviewGroup ag: agList)
                {
                    report.addRowHeader(ag.getName());
                    report.addRowType(ag.getName(),"VALUE;DA;PERCENTAGES;NOBOTTOMBORDER");
                            
                    report.addRowHeader("Значимости "+ag.getName());
                    report.addRowType("Значимости "+ag.getName(),"VALUE;NOCHANGEODD;DA");
                }
            }
            List<String>volumenameslist=new ArrayList<>();
            for(InterviewGroup ag: owr.gcr.getAgroup1())
            {
                String name1 = ag.getName();
                if (owr.gcr.isAgrop1fictive())
                    if (owr.content1.getAnswerCodeMap()!=null)
                    {
                        name1 = owr.content1.getAnswerCodeMap().get(ag.getName());
                    }
                if (name1.equals(""))
                    name1 = ag.getName();
                volumenameslist.add(name1);
            }
            if (addall)
            {
                volumenameslist.add(0,"Все");
                List<String> namelist = new ArrayList<>();
                for (InterviewGroup ag2: owr.gcr.getAgroup2())
                {
                    namelist.add(ag2.getName());
                }
                report.addStrToList(namelist, totalrowheader);
            }
            
        }
        
        
                
        
        
        
        
        
        return report;

        
    }
    
    
    
    
    
    private double getUniverseFromProperties(final double default_universe, Properties properties) 
    {
        return getDoubleFromProperties("universe",default_universe,properties);
    }
    
    private double getConflevelFromProperties(final double default_universe, Properties properties) 
    {
        return getDoubleFromProperties("conflevel",default_universe,properties);
    }

    private String getWeghtcontentnameFromProperties(String weightContentName, Properties properties) 
    {
        return getStringFromProperties("weightcontent", weightContentName, properties);
    }
    
}
