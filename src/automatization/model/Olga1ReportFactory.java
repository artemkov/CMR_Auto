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
import java.util.Collections;
import java.util.HashMap;
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
        report.setReportType(level3node.getData());
        report.setRepRow(level3node.getExcelRow());
        
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
        List<InterviewGroup> agList = null;
        report.setSampleNames(sampleNames);
        for (int i=0; i<sampleList.size(); i++)
        {
            
            OlgaWeightedReport owr = new OlgaWeightedReport(sampleList.get(i), content, properties, level3node.findRootNode());
            OlgaWeightedReport previosowr = i>0?olrlist.get(i-1):null;
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
                    report.addRowType("VARIANCE "+owr.content3.getName(),"VALUE");
                }
                    
                report.addRowHeader("SEMEAN "+owr.content3.getName());
                report.addRowType("SEMEAN "+owr.content3.getName(),"VALUE");
                        
                if (debugvals)
                {
                    report.addRowHeader("DIFFERENCE "+owr.content3.getName());
                    report.addRowType("DIFFERENCE "+owr.content3.getName(),"VALUE");
                        
                    report.addRowHeader("STUDENT "+owr.content3.getName());
                    report.addRowType("STUDENT "+owr.content3.getName(),"VALUE");
                            
                    report.addRowHeader("SD");
                    report.addRowType("SD","VALUE");
                }    
                    
                report.addRowHeader("NPS "+owr.content3.getName());
                report.addRowType("NPS "+owr.content3.getName(),"VALUE;DA;PERCENTAGES;NOBOTTOMBORDER");
                        
                report.addRowHeader("NPSDA 2s");
                report.addRowType("NPSDA 2s","VALUE;NOCHANGEODD;DA");
                        
                report.addRowHeader("ConfInt");
                report.addRowType("ConfInt","VALUE");
                        
                        
                //Группы NPS
                agList = owr.wig_NPSReportList.get(0).groupslist;   
                for (InterviewGroup ag: agList)
                {
                    report.addRowHeader(ag.getName());
                    report.addRowType(ag.getName(),"VALUE;DA;PERCENTAGES;NOBOTTOMBORDER");
                            
                    report.addRowHeader("Значимости "+ag.getName());
                    report.addRowType("Значимости "+ag.getName(),"VALUE;NOCHANGEODD;DA");
                }
            }
            
            //Заголовки разделов (переменная content1)
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
            report.addStrToList(volumenameslist, volumeheader);
            
            //Значимости NPS
            List<Color> cList = new ArrayList<>();
            for(int j=0;j<owr.group2samples.size();j++)
            {
                if (previosowr!=null)
                {
                    NPSgetter rep1 = previosowr.wig_NPSReportList.get(j);
                    NPSgetter rep2 = owr.wig_NPSReportList.get(j);
                                
                        if (rep1!=null&&rep2!=null)
                        {    
                            DAReport crossdarep = new DAReport(rep1, rep2, owr.content2.getName(), owr.content2.getName(), confLevel, universe);
                            Double nps1 = rep1.getNps();
                            Double nps2 = rep2.getNps();
                            String conclusion2s = crossdarep.conclusion2s;
                            if ((conclusion2s!=null)&&(conclusion2s.equals("Different")))
                                if (nps2>nps1)
                                {
                                    cList.add(Color.BLUE);
                                }
                                else
                                {
                                    cList.add(Color.RED);
                                }
                                else
                                    cList.add(Color.BLACK);
                        }
                        else
                           cList.add(Color.BLACK);
                }
                else
                {
                    cList.add(Color.BLACK);
                }    
            }
            report.getColorMap().put("NPS "+owr.content3.getName(), cList);
            
            for (InterviewGroup ag1: owr.gcr.getAgroup1())
            {
                String name1="";
                if (owr.gcr.isAgrop1fictive())
                    if (owr.content1.getAnswerCodeMap()!=null)
                    {
                        name1 = owr.content1.getAnswerCodeMap().get(ag1.getName());
                    }
                if ((name1==null)||(name1.isEmpty()))
                    name1 = ag1.getName();
                List<String> namelist = new ArrayList<>();
                for (InterviewGroup ag2: owr.gcr.getAgroup2())
                {
                    String name2="";
                    if (owr.gcr.isAgrop2fictive())
                        if (owr.content2.getAnswerCodeMap()!=null)
                        {
                            name2 = owr.content2.getAnswerCodeMap().get(ag2.getName());
                        }
                    if ((name2==null)||(name2.isEmpty()))
                        name2 = ag2.getName();
                    namelist.add(name2);
                }
                report.addStrToList(namelist, totalrowheader);
            }
            
            List<Number> samplecountlist = new ArrayList<>();
            for(int j=0;j<owr.group2samples.size();j++)
                samplecountlist.add(owr.group2samples.get(j).size());
            report.addToList(samplecountlist, "Размер выборки");
                    
                    
            List<Number> groupedtotalcountlist = new ArrayList<>();
            for(int j=0;j<owr.wig_NPSReportList.size();j++)
            {
                groupedtotalcountlist.add(owr.wig_NPSReportList.get(j).getGroupedTotal());
            }
            report.addToList(groupedtotalcountlist, "В группах");
            
            List<String> ganormDAlist = null;
            Map<String,List<Number>> grValList = new HashMap<>();
            for (InterviewGroup ag: agList)
            {
                String gname = ag.getName();
                List<Number> valList = new ArrayList<>();
                for (int k=0;k<owr.wig_NPSReportList.size();k++)
                {
                    WeightedInterviewGroupNPSReport currentrep = owr.wig_NPSReportList.get(k),oldrep;
                    Double size = currentrep.getGroupedTotal();
                    Double count = currentrep.weightedCountmap.getOrDefault(currentrep.findGroupByName(gname),0.0);
                    
                    Double percent = size>0?count/size*100.0:0.0;
                    
                    if (previosowr!=null)
                    {
                        oldrep=previosowr.wig_NPSReportList.get(k);
                        Double oldsize = oldrep.getGroupedTotal();
                        Double oldcount = oldrep.weightedCountmap.getOrDefault(oldrep.findGroupByName(gname),0.0);
                        Double oldpercent = oldsize>0?oldcount/oldsize*100.0:0.0;
                        Double davalue = ReportUtils.getNormDAVal(oldpercent, percent, oldsize, size);
                        Color color = ReportUtils.getColorFromDiff(davalue);
                        report.addToColorMap(gname,color);
                    }
                    else
                    {
                        report.addToColorMap(gname,Color.BLACK);
                    }
                    valList.add(report.round_p(percent));
                }
                grValList.put(gname, valList);
                report.addToList(valList,gname);
                        
                if (owr.ganormDAMap.containsKey(gname))
                {
                    ganormDAlist=owr.ganormDAMap.get(gname);
                    report.addStrToList(ganormDAlist, "Значимости "+gname);
                    report.getColorMap().put("Значимости "+gname, Collections.nCopies(sampleList.size()*owr.group2samples.size(), Color.GREEN));
                }
            }
            
            List<Number> npscountlist = new ArrayList<>();
            for(int j=0;j<owr.wig_NPSReportList.size();j++)
            {
                Double nps = owr.wig_NPSReportList.get(j).getNps()==null?0:owr.wig_NPSReportList.get(j).getNps();
                npscountlist.add(report.round_p(nps));
            }
            report.addToList(npscountlist, "NPS "+owr.content3.getName());
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
