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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Артем Ковалев
 */
public class OlgaReport 
{
    public static final String ZOGROUPNAME = "З.О.";
    public static final double DEFAULTUNIVERSE = 1000000.0;
    public static final double DEFAULTCONFLEVEL = 0.95;
    
    Content content1,content2,content3;
    Path groupfile1,groupfile2,groupfile3;
    
    GroupsCrossReport gcr = null;
    List<NPSReport> npsReportList = new ArrayList<>();
    List<LinearReport> linearReportList = new ArrayList<>();
    List<UniqueList<Map<Content,String>>> group1samples = null;
    List<UniqueList<Map<Content,String>>> group2samples = null;
    String [] dastringarray2s;
    String [] dastringarray1s;
    Double universe, confLevel;
    DAReport[][][] damatrix ;
    //List<Double> confIntervalList = new ArrayList<>();
    List<Double> newconfIntervalList = new ArrayList<>();
    Map<String,List<String>> normDAMap = new HashMap<>();
    Map<String,List<String>> ganormDAMap = new HashMap<>();
    List<ArithmeticMeanReport> meanReportList = new ArrayList<>();
    List<String> meandastringList = new ArrayList<>();
    List<List<Double>> compValList = new ArrayList<>();
    List<List<Double>> studentDistValList = new ArrayList<>();
    double[][][] valMatrix = null;
    double[][][] stMatrix = null;
    List<String> davaluestringList  = new ArrayList<>();
    List<String> daststringList  = new ArrayList<>();
    
    public OlgaReport(UniqueList<Map<Content,String>> interviews, Content content1, Properties properties, TemplateNode<String> rootNode) throws GroupsFileNotFoundException, InvalidTemplateFileFormatException, ReportParamsNotDefinedException, VariableNotFoundException, InvalidFilterException, IOException, ReportFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException
    {
        this.content1=content1;
        //Cтроим кросс часть
        gcr = new GroupsCrossReport(interviews, content1, properties, rootNode);
        content2 = gcr.getContent2();
        List<InterviewGroup> igroup1=gcr.getAgroup1();
        List<InterviewGroup> igroup2=gcr.getAgroup2();
        group1samples=new ArrayList<>();
        group2samples=new ArrayList<>();
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
        }
        
        
        for(UniqueList<Map<Content,String>> sample: group2samples)
        {
            if (sample==null)
                System.out.print("");
            if (sample.isEmpty())
                System.out.print("");
            NPSReport npsRep =  NPSReport.getAnswerGroupsWithNPSFromExcel(groupfile3);
            npsRep.computeNPS(sample, thirdvarname);
            npsReportList.add(npsRep);
            
            
            
            
            
            
            
            
            LinearReport lrep = new LinearReport(sample, content3);
            
            
            
            Properties props = new Properties();
            props.setProperty("content1", thirdvarname+"-1");
            props.setProperty("rowname1", "Среднее для "+thirdvarname);
            
            if (properties.containsKey("ExcludeList"))
                props.setProperty("ExcludeList", properties.getProperty("ExcludeList"));
            else
            {
                AnswerGroup zogroup = npsRep.getGroupByName(ZOGROUPNAME);
                String zoexcludeproperty = "";
                int count=0;
                for (String zoanswer: zogroup.getAnswerset())
                {
                    if (count>0)
                        zoexcludeproperty+=",";
                    zoexcludeproperty += zoanswer ;
                    count++;
                }
                props.setProperty("ExcludeList", zoexcludeproperty);
            }
            
            ArithmeticMeanReport mrep = new ArithmeticMeanReport(sample, content3, props, rootNode);
            meanReportList.add(mrep);
            linearReportList.add(lrep);
        }
        //Получение обычных значимостей
        //для Tops Bottoms Passives и других груп
        int sample1size = group2samples.size()/group1samples.size();
        List<AnswerGroup> aglist = npsReportList.get(0).getGroups();
        for (AnswerGroup ag: aglist)
        {
            List<String> grdastringList =new ArrayList<>();
            for (int i=0;i<group2samples.size();i++)
                     grdastringList.add("");
            for (int k=0;k<group1samples.size();k++)
            {
                List<Double> curvolumeValues = new ArrayList<>();
                for (int i=0; i<sample1size;i++)
                {
                    
                    int index = k*sample1size+i;
                    NPSReport crep = npsReportList.get(index);
                    Map<AnswerGroup,Double> valcmap = crep.getValuecountermap();
                    AnswerGroup cag = npsReportList.get(k*sample1size+i).getGroupByName(ag.getName());
                    Double csize = (double)crep.getIntGroupedTotal();
                            
                    Double cval = csize>0?valcmap.get(cag)/csize*100.0:0.0;
                    curvolumeValues.add(cval);
                    for (int j=i+1; j<sample1size;j++)
                    {
                        
                        AnswerGroup compag = npsReportList.get(k*sample1size+j).getGroupByName(ag.getName());
                        Double compsize = (double)npsReportList.get(k*sample1size+j).getIntGroupedTotal();
                        Double compval = compsize>0?npsReportList.get(k*sample1size+j).getValuecountermap().get(compag)/compsize*100.0:0.0;
                        Double normDAVal = (cval!=null&&compval!=null)?ReportUtils.getNormDAVal(cval, compval, csize, compsize):null;
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
            ganormDAMap.put(ag.getName(), grdastringList);
        }
        
        
        
        //для линейного отчета
        
        for (String code: content3.getCodesListFromCodeMap())
        {
            List<String> dastringList =new ArrayList<>();
            for (int i=0;i<group2samples.size();i++)
                 dastringList.add("");
            for (int k=0;k<group1samples.size();k++)
            {
                
                for (int i=0; i<sample1size;i++)
                {
                
                    String dastr ="";
                    LinearReport lrep = linearReportList.get(k*sample1size+i); 
                    
                    Double cursize = group2samples.get(k*sample1size+i).size()+0.0;
                    Double curval = cursize!=0?lrep.getStatmap().getOrDefault(code, 0.0)/cursize*100:0;
                    for (int j=i+1; j<sample1size;j++)
                    {
                        Double compsize = group2samples.get(k*sample1size+j).size()+0.0;
                        Double compval = compsize!=0?linearReportList.get(k*sample1size+j).getStatmap().getOrDefault(code, 0.0)/compsize*100:0;
                        Double normDAVal = ReportUtils.getNormDAVal(curval, compval, cursize, compsize);
                        if ((normDAVal!=null)&&(normDAVal>0))
                        {
                            
                            String val = dastringList.get(k*sample1size+i);
                            val+=" >"+(j+1);
                            dastringList.set(k*sample1size+i,val);
                        }
                        if ((normDAVal!=null)&&(normDAVal<0))
                        {
                            String val = dastringList.get(k*sample1size+j);
                            val+=" >"+(i+1);
                            dastringList.set(k*sample1size+j,val);
                        }
                    }
                }
            }
            normDAMap.put(code, dastringList);
        }
        
        
        String u =properties.getProperty("universe", DEFAULTUNIVERSE+"");
        String conf = properties.getProperty("conflevel", DEFAULTCONFLEVEL+"");
        try
        {
            Double un = Double.parseDouble(u);
            universe = un;
        }
        catch (NumberFormatException e)
        {
                        
        }
        
        try
        {
            Double un = Double.parseDouble(conf);
            confLevel = un;
        }
        catch (NumberFormatException e)
        {
                        
        }
        //Получение значимостей для средних
        for (int i=0;i<group2samples.size();i++)
            meandastringList.add("");
        valMatrix = new double[group1samples.size()][sample1size][sample1size];
        stMatrix = new double[group1samples.size()][sample1size][sample1size];
        for (int k=0;k<group1samples.size();k++)
        {
                
            for (int i=0; i<sample1size;i++)
            {
                
                String dastr ="";
                ArithmeticMeanReport meanrep = meanReportList.get(k*sample1size+i);
                Double cursize = meanrep.countSampleWeight(group2samples.get(k*sample1size+i));
                Double curmean = meanrep.meanList.get(0);
                Double cursemean = meanrep.semeanList.get(0);
                
                List<Double> valList = new ArrayList<>();
                List<Double> studentList = new ArrayList<>();
                
                
                for (int j=i+1; j<sample1size;j++)
                {
                    ArithmeticMeanReport compmeanrep = meanReportList.get(k*sample1size+j);
                    Double compsize = compmeanrep.countSampleWeight(group2samples.get(k*sample1size+j));
                    Double compmean = compmeanrep.meanList.get(0);
                    Double compsemean = compmeanrep.semeanList.get(0);
                    Double studentDAVAL = ReportUtils.getStudentDAVal(curmean, compmean, cursemean , compsemean, confLevel, cursize+compsize,sample1size);
                    if (properties.getProperty("DebugVals", "false").equalsIgnoreCase("true"))
                    {
                        Double debugVals[] = ReportUtils.getStudentDAValDebug(curmean, compmean, cursemean , compsemean, confLevel, cursize+compsize, sample1size);
                        if (debugVals!=null)
                        {
                            valMatrix[k][i][j]=debugVals[0];
                            valMatrix[k][j][i]=-debugVals[0];
                            stMatrix[k][j][i]=debugVals[1];
                            stMatrix[k][i][j]=debugVals[1];
                        }
                    }
                    if (studentDAVAL!=null)
                    {
                        if (studentDAVAL>0)
                        {
                            String val = meandastringList.get(k*sample1size+i);
                            val+=" >"+(j+1);
                            meandastringList.set(k*sample1size+i,val);
                        }
                        else
                        {
                            String val = meandastringList.get(k*sample1size+j);
                            val+=" >"+(i+1);
                            meandastringList.set(k*sample1size+j,val);
                        }
                    }
                }
                if (properties.getProperty("DebugVals", "false").equalsIgnoreCase("true"))
                {
                    String val="",st="";
                    for (int j=0; j<sample1size;j++)
                    {
                        val+= String.format("%6.2f ", valMatrix[k][i][j]);
                        st += String.format("%6.2f ", stMatrix[k][i][j]);
                    }
                    davaluestringList.add(val);
                    daststringList.add(st);
                }
            }
        }
        
        
        
        
        //Значимости для NPS
        dastringarray2s = new String[group2samples.size()]; 
        dastringarray1s = new String[group2samples.size()]; 
        damatrix = new DAReport[group1samples.size()][group2samples.size()/group1samples.size()][group2samples.size()/group1samples.size()];
        
        for(int i=0;i<group1samples.size();i++)
        {
            
            for (int j =0; j< group2samples.size()/group1samples.size();j++)
            {
                String toadd2s = "";
                String toadd1s = "";
                
                for (int k =0; k< group2samples.size()/group1samples.size();k++)
                {
                    if (j!=k)
                    {
                        /*if(i>0)
                            System.out.println(gcr.getAgroup1().get(i-1).getName()+" ");
                        else            
                            System.out.println("Все");
                        System.out.println("i="+i+" j="+j+" k="+k);*/
                        DAReport darep = new DAReport(npsReportList.get(i*group2samples.size()/group1samples.size()+j), 
                                                      npsReportList.get(i*group2samples.size()/group1samples.size()+k), 
                                                      thirdvarname, thirdvarname, 
                                                      confLevel, universe);
                        
                        //System.out.println("nps1 = "+darep.nps1+" nps2 = "+darep.nps2+" dasig = "+darep.minimumSigDif2s+" conc2s:"+darep.conclusion2s);
                        
                        String conc = darep.conclusion2s==null?"":darep.conclusion2s;
                        
                        if ((conc.equals("Different")))
                        {
                            if (darep.nps1>darep.nps2)
                            {
                                toadd2s+=" >"+(k+1);
                            }
                            /*if (darep.nps2>darep.nps1)
                            {
                                toadd2s+=" <"+(k+1);
                            }*/
                        }
                        
                        conc = darep.conclusion1s==null?"":darep.conclusion1s;
                        
                        if ((conc.equals("Different")))
                        {
                            if (darep.nps1>darep.nps2)
                            {
                                toadd1s+=" >"+(k+1);
                            }
                            if (darep.nps2>darep.nps1)
                            {
                                toadd1s+=" <"+(k+1);
                            }
                        }
                        
                        damatrix[i][j][k]=darep;
                        
                        //confInterval
                        
                        //confmatrix[j][k]=darep.confInterval2;
                        
                        /*if (j==0)
                        {
                            if (k==1)
                            {
                                confIntervalList.add(darep.confInterval1);
                                confIntervalList.add(darep.confInterval2);
                            }
                            else
                                confIntervalList.add(darep.confInterval2);
                        }*/
                    }
                    else
                    {
                        damatrix[i][j][k]=null;
                        //dastringarray2s[i*group1samples.size()+j]="";
                        //dastringarray1s[i*group1samples.size()+j]="";
                    }
                }
                NPSReport npsr = npsReportList.get(i*group2samples.size()/group1samples.size()+j);
                Double nci = npsr.getConfInterval(confLevel, universe);
                newconfIntervalList.add(nci);
                dastringarray2s[i*group2samples.size()/group1samples.size()+j]=toadd2s;
                dastringarray1s[i*group2samples.size()/group1samples.size()+j]=toadd1s;
            }
        }
        
        
        
        
        
        
        
        
        
        
        //отображение в консоли
        /*for (int i = 0; i<group1samples.size(); i++)
        {
            if (i==0)
                System.out.println("ГРУППА1 ВСЕ");
            else
                System.out.println("ГРУППА1 "+agroup1.get(i-1).getName());
            for (int j=i*group1samples.size(); j<(i+1)*group1samples.size(); j++)
            {
               
                System.out.println("группа2 "+agroup2.get(j%(group1samples.size()-1)).getName()+" Интервью "+group2samples.get(j).size());
            }
        }*/
        
        
        
        
        
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
