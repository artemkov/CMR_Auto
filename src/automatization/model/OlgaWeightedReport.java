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
import static automatization.model.ReportUtils.getDoubleFromProperties;
import static automatization.model.ReportUtils.getStringFromProperties;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Дина
 */
public class OlgaWeightedReport 
{
    public static final String ZOGROUPNAME = "З.О.";
    public static final double DEFAULTUNIVERSE = 1000000.0;
    public static final double DEFAULTCONFLEVEL = 0.95;
    
    Content content1,content2,content3;
    Path groupfile1,groupfile2,groupfile3;
    
    GroupsCrossReport gcr = null;
    List<NPSReport> npsReportList = new ArrayList<>();
    List<WeightedInterviewGroupNPSReport> wig_NPSReportList = new ArrayList<>();
    List<WeightedInterviewGroupReport> wig_LinearReportList = new ArrayList<>();
    List<UniqueList<Map<Content,String>>> group1samples = null;
    List<UniqueList<Map<Content,String>>> group2samples = null;
    String [] dastringarray2s;
    String [] dastringarray1s;
    Double universe, confLevel;
    DAReport[][][] damatrix ;
    List<Double> confIntervalList = new ArrayList<>();
    Map<String,List<String>> linormDAMap = new HashMap<>();
    Map<String,List<String>> ganormDAMap = new HashMap<>();
    List<ArithmeticMeanReport> meanReportList = new ArrayList<>();
    List<String> meandastringList = new ArrayList<>();
    List<List<Double>> compValList = new ArrayList<>();
    List<List<Double>> studentDistValList = new ArrayList<>();
    double[][][] valMatrix = null;
    double[][][] stMatrix = null;
    List<String> davaluestringList  = new ArrayList<>();
    List<String> daststringList  = new ArrayList<>();
    Content weightcontent = null;
    
    
    public OlgaWeightedReport(UniqueList<Map<Content,String>> interviews, Content content1, Properties properties, TemplateNode<String> rootNode) throws GroupsFileNotFoundException, InvalidTemplateFileFormatException, ReportParamsNotDefinedException, VariableNotFoundException, InvalidFilterException, IOException, ReportFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException
    {
        this.content1=content1;
        
        //Cтроим кросс часть
        gcr = new GroupsCrossReport(interviews, content1, properties, rootNode);
        content2 = gcr.getContent2();
        List<InterviewGroup> igroup1=gcr.getIgroup1();
        List<InterviewGroup> igroup2=gcr.getIgroup2();
        
        
        //Разбивка интервью по группам
        group1samples=new ArrayList<>();
        group2samples=new ArrayList<>();
        if (ReportUtils.getBooleanFromProperties("AddAll", false, properties))
            group1samples.add(interviews);
        
        group1samples.addAll(Filter.splitByInterviewGroups(interviews, igroup1));
        for (int i =0; i<group1samples.size();i++)
        {
            group2samples.addAll(Filter.splitByInterviewGroups(group1samples.get(i),igroup2));
        }
        
        //Weight Content
        String weightcontentname = getWeightcontentnameFromProperties (null, properties);
        weightcontent = ContentUtils.getContentByNameFromInterviewList(interviews,weightcontentname);
        
        //Переменная 3
        String thirdvarname = ReportUtils.getStringFromProperties("thirdvar",null,properties);
        if (thirdvarname==null)
        {
            throw new ReportParamsNotDefinedException("Не указан необходимый параметр 'thirdvar'");
        }
        content3 = ContentUtils.getContentByNameFromInterviewList(interviews, thirdvarname);
        if (content3==null)
        {
            throw new VariableNotFoundException(thirdvarname);
        }
        if (properties.containsKey("groupfile3"))
        {
            groupfile3 = Paths.get(properties.getProperty("groupfile3"));
        }
        else
        {
            throw new ReportParamsNotDefinedException("Не указан необходимый параметр 'groupfile3'");
        }
        
        //ShowAnswerText
        boolean showAnswerText = ReportUtils.getBooleanFromProperties("ShowAnswersText", false, properties);
        
        //общий список ответов из всех сэмплов
        List<String> var3LinearGroupsNameslist = new UniqueList<>();
        
        //NPS & Linear & Mean
        for(UniqueList<Map<Content,String>> sample: group2samples)
        {
            //NPS
            WeightedInterviewGroupNPSReport wigr_npsr = new WeightedInterviewGroupNPSReport(groupfile3, weightcontent,content3);
            wigr_npsr.populateGroups(sample);
            wig_NPSReportList.add(wigr_npsr);
            
            
            //Linear
            List<InterviewGroup> var3LinearGroupslist = GroupsReport.constructInterviewGroupsFormContent(content3,sample,showAnswerText);
            for (InterviewGroup ig: var3LinearGroupslist)
                var3LinearGroupsNameslist.add(ig.getName());
            WeightedInterviewGroupReport wigr_linear = new WeightedInterviewGroupReport(var3LinearGroupslist, weightcontent,content3);
            wigr_linear.populateGroups(sample);
            wig_LinearReportList.add(wigr_linear);
            
            //Mean
            Properties props = new Properties();
            props.setProperty("content1", thirdvarname+"-1");
            props.setProperty("rowname1", "Среднее для "+thirdvarname);
            if (properties.containsKey("ExcludeList"))
                props.setProperty("ExcludeList", properties.getProperty("ExcludeList"));
            ArithmeticMeanReport mrep = new ArithmeticMeanReport(sample, content3, props, rootNode);
            meanReportList.add(mrep);
        }
        
        Collections.sort(var3LinearGroupsNameslist, new StringIntComparator());
        //Получение обычных значимостей
        //для Tops Bottoms Passives и других груп
        int sample1size = group2samples.size()/group1samples.size();/*количество колонок второго уровня в одной колонке 1го уровня*/
        List<InterviewGroup> iglist = wig_NPSReportList.get(0).groupslist;
        for (InterviewGroup curig: iglist)
        {
            List<String> grdastringList =new ArrayList<>();
            for (int i=0;i<group2samples.size();i++)
                     grdastringList.add("");
            String curgroupname = curig.getName();
            for (int k=0;k<group1samples.size();k++)
            {
                
                for (int i=0; i<sample1size;i++)
                {
                    int curindex = k*sample1size+i;
                    WeightedInterviewGroupNPSReport currep = wig_NPSReportList.get(curindex);
                    Map<InterviewGroup,Double> valcmap = currep.weightedCountmap;
                    Double curval = valcmap.getOrDefault(currep.findGroupByName(curgroupname), 0.0);
                    Double cursize = currep.getTotalGroupedWeight();
                    Double curpercent = cursize>0?curval/cursize*100.0:0.0;
                    for (int j=i+1; j<sample1size;j++)
                    {
                        int compindex = k*sample1size+j;
                        WeightedInterviewGroupNPSReport comprep = wig_NPSReportList.get(compindex);
                        Map<InterviewGroup,Double> valcompmap = comprep.weightedCountmap;
                        Double compval = valcompmap.getOrDefault(comprep.findGroupByName(curgroupname), 0.0);
                        Double compsize = comprep.getTotalGroupedWeight();
                        Double comppercent = compsize>0?compval/compsize*100.0:0.0;
                        Double normDAVal = (curval!=null&&compval!=null)?ReportUtils.getNormDAVal(curpercent, comppercent, cursize, compsize):null;
                        if ((normDAVal!=null)&&(normDAVal>0))
                        {
                            String val = grdastringList.get(k*sample1size+i);
                            val+=" >"+(j+1);
                            grdastringList.set(k*sample1size+i,val);
                        }
                        if ((normDAVal!=null)&&(normDAVal<0))
                        {
                            String val = grdastringList.get(k*sample1size+j);
                            val+=" >"+(i+1);
                            grdastringList.set(k*sample1size+j,val);
                        }
                    }
                    
                }
            }
            ganormDAMap.put(curgroupname, grdastringList);
        }
        for (String gname: var3LinearGroupsNameslist)
        {
            List<String> dastringList =new ArrayList<>();
            for (int i=0;i<group2samples.size();i++)
                 dastringList.add("");
            for (int k=0;k<group1samples.size();k++)
            {
                for (int i=0; i<sample1size;i++)
                {
                    String dastr ="";
                    WeightedInterviewGroupReport wgr = wig_LinearReportList.get(k*sample1size+i);
                    InterviewGroup curig = wgr.findGroupByName(gname);
                    Double cursize = wgr.getTotalGroupedWeight();
                    Double curval = cursize!=0? wgr.weightedCountmap.getOrDefault(curig, 0.0)/cursize*100:0;
                    for (int j=i+1; j<sample1size;j++)
                    {
                        WeightedInterviewGroupReport compwgr = wig_LinearReportList.get(k*sample1size+j);
                        InterviewGroup compig = compwgr.findGroupByName(gname);
                        Double compsize = compwgr.getTotalGroupedWeight();
                        Double compval = cursize!=0? compwgr.weightedCountmap.getOrDefault(compig, 0.0)/compsize*100:0;
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
            linormDAMap.put(gname, dastringList);
        }
    }
    
    public static double getUniverseFromProperties(final double default_universe, Properties properties) 
    {
        return getDoubleFromProperties("universe",default_universe,properties);
    }
    
    public static double getConflevelFromProperties(final double default_universe, Properties properties) 
    {
        return getDoubleFromProperties("conflevel",default_universe,properties);
    }

    public static String getWeightcontentnameFromProperties(String weightContentName, Properties properties) 
    {
        return getStringFromProperties("weightcontent", weightContentName, properties);
    }
}
