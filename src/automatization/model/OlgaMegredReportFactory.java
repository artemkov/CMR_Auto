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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

/**
 *
 * @author Дина
 */
public class OlgaMegredReportFactory implements ReportFactory
{

    @Override
    public Report makeReport(TemplateNode<String> level3node, Content content, Properties properties, List<UniqueList<Map<Content, String>>> sampleList, List<String> sampleNames) throws InvalidTemplateFileFormatException, ReportParamsNotDefinedException, VariableNotFoundException, InvalidFilterException, IOException, GroupsFileNotFoundException, ReportFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException 
    {
        
        //Создание отчета.
        //Установка простых параметров: тип, номер строки в Excel шаблоне, свойства, главный контент,
        //список названий сэмплов
        Report report = new Report();
        report.setReportType(level3node.getData());
        report.setRepRow(level3node.getExcelRow());
        report.addProperties(properties);
        report.setContent(content);
        report.setSampleNames(sampleNames);
        
        //дефолтные параметры для расчета NPS из свойств
        double universe=1000000.0;
        double confLevel=0.95;
        universe = getUniverseFromProperties(universe,properties);
        confLevel = getConflevelFromProperties(confLevel, properties);
        
        //Имя весовой переменной из свойств
        String weightContentName = getWeghtcontentnameFromProperties(null,properties);
        
        //Имя рабочей переменной (thirdvar)
        String content3Name =ReportUtils.getStringFromProperties("thirdvar",null,properties);
        
        //Инициализация заголовков ряда главной переменной, названий переменных из content2List и их значений
        String volumeheader = null, content2namesheader = null, content2valsheader = null;
        
        //Значение переменной AddAll (добавлять или нет столбцы все для главной переменной)
        boolean addall = ReportUtils.getBooleanFromProperties("AddAll", false, properties);
        
        //Инициализация списка групп отчета NPS
        List<InterviewGroup> agList = null;
        
        //Округление действительных (цифры после запятой) из свойств
        int fpdigits = getFPDIGITSFromProperties(properties);
        report.setFpDIGITS(fpdigits);
        
        //Все значения рабочей переменной (thirdvar)
        TreeSet<String> uniquevalset = null;
        
        //Флаги отображения различных частей отчета
        boolean debugvals = getBooleanFromProperties("DebugVals",false,properties);
        boolean dontshowmean = getBooleanFromProperties("noMean",false,properties);
        boolean dontshowgroups = getBooleanFromProperties("noGroups",false,properties);
        boolean dontshownps = getBooleanFromProperties("noNPS",false,properties);
        boolean dontshowlinear = getBooleanFromProperties("noLinear",false,properties);
        
        if (getBooleanFromProperties("LinearOnly",false,properties))
        {
            dontshowmean=true;
            dontshowgroups=true;
            dontshownps=true;
        }
        
        //Список цветов шрифта ячеек для динамической значимости в отчете по средним
        List<Color> mcList = new ArrayList<>();
        //Список цветов шрифта ячеек для динамической значимости в отчете NPS
        List<Color> cList = new ArrayList<>();
        //список имен переменных Content2
        //если есть "content2List" то вносим его, если "content2"
        List<String> content2List = null;
        String content2ListStr = properties.getProperty("content2List");
        content2ListStr = content2ListStr==null?properties.getProperty("secondvarList"):content2ListStr;
        if (content2ListStr!=null)
        {
            content2List = Arrays.asList(content2ListStr.split("[,;]"));
        }
        else
        {
            
            String content2Name = properties.getProperty("secondvar");
            if (content2Name!=null&&!content2Name.isEmpty())
            {
                content2List = Arrays.asList(content2Name);
            }
        }
        
        //список значений Group1
        List<String> group1namesList = new ArrayList<>();
        
        
        int reportSamleWidth = 0;
        
        int content1valscount = 0;
        
        String firstContent2Name=null;
        List<OlgaWeightedReport> prevowrList,curowrList = null;
        
        //Карта значений по переменным Content2
        Map<String,List<OlgaWeightedReport>> content2reportMap = new HashMap<>(); 
        
        //Карта ширин для переменных из content2List
        Map<String,Integer> cont2columnWidthMap = new HashMap<>();
        
        //------------------------
        //ГЛАВНЫЙ ЦИКЛ (ПО СЭМПЛАМ)
        //------------------------
        for (int i=0; i<sampleList.size(); i++)
        {
            //Если нет второй переменной то исключение
            if (content2List==null)
                throw new ReportParamsNotDefinedException("Не заданы параметры content2List или secondvar");
            //Инициализация списка отчетов и предыдущего для разных Content2
            prevowrList = curowrList; 
            curowrList = new ArrayList<>(content2List.size());
            
            int content2counter=0;//счетчик по списку content2List
            
            //_________________________________________________________________
            //_________________________________________________________________
            for (String content2Name: content2List)
            {
                //Получаем данные отчета OlgaWeightedReport для переменной content2Name
                /*Properties p2 = new Properties(properties);
                p2.setProperty("secondvar", content2Name);
                p2=ReportUtils.mergeProperties(p2, properties);*/
                properties.put("secondvar", content2Name);
                
                
                OlgaWeightedReport owr = new OlgaWeightedReport(sampleList.get(i), content, properties, level3node.findRootNode());
                
                
                
                OlgaWeightedReport prevowr = i>0?prevowrList.get(content2counter):null;
                curowrList.add(owr);
                if (i==0)
                    content2reportMap.put(content2Name, new ArrayList<OlgaWeightedReport>());
                content2reportMap.get(content2Name).add(owr);
                
                //Занесение значений в карту ширин сэмла
                if (i==0)
                    cont2columnWidthMap.put(content2Name, owr.gcr.getAgroup2().size());
                
                if ((i==0)&&(content2counter==0))
                {
                    //Главный заголовок по умолчанию
                    String qtext = (owr.content3!=null&&owr.content3.getText()!=null&&!owr.content3.getText().isEmpty())?" \""+owr.content3.getText()+"\"":"";
                    report.setMainHeader("Значимости для "+qtext);
                    
                    //Имя первой переменной в Content2List
                    firstContent2Name=content2Name;
                    
                    //значения главной группы разбиения (Content1)
                    group1namesList = owr.gcr.getIgroup1().stream().map(igroup->igroup.getName()).collect(toList());
                    
                    content1valscount=group1namesList.size();

                    //Заголовок базы
                    boolean mustdrawtotal = getBooleanFromProperties("drawtotal",true,properties);
                    report.setNoFirstString(!mustdrawtotal);
                
                    //Уточнение имени рабочей переменной (большие или маленькие буквы)
                    if (owr.content3!=null)
                        content3Name=owr.content3.getName();
                    
                    //Уникальные значения линейного отчета
                    if (!dontshownps)
                    {
                        //Если нет каких-то значений Content3 во всех сэмплах, то они не войдут в таблицу
                        
                        uniquevalset = ContentUtils.getContentUniqueValuesFromSampleList(sampleList, owr.content3);
                        //uniquevalset= owr.content3.getAnswerCodeMap().values();
                    }
                        
                    else
                    {
                        List<InterviewGroup> glist = owr.wig_LinearReportList.get(0).groupslist;
                        Set<String> uvals = glist.stream().map(ig0->ig0.getName()).collect(Collectors.toSet());
                        
                        uniquevalset= new TreeSet(new StringIntComparator());
                        uniquevalset.addAll(uvals);
                    }
                    //Заголовок разделов (заголовок 1)
                    volumeheader = getStringFromProperties("VOLUMEHEADER", null, properties)==null?owr.content1.getName():
                        getStringFromProperties("VOLUMEHEADER", null, properties);                        
                    report.addRowHeader(volumeheader,"VOLUMEHEADER");
                    
                    //Заголовок имен переменных Content2Name 
                    content2namesheader =  getStringFromProperties("CONTENT2NAMESHEADER", null, properties)==null?"Переменные разбиения":
                        getStringFromProperties("CONTENT2NAMESHEADER", null, properties);
                    report.addRowHeader(content2namesheader,"VOLUMEHEADER");
 
                 
                    //Заголовок значений переменных Content2Name 
                    content2valsheader =  getStringFromProperties("CONTENT2VALSHEADER", null, properties)==null?"Значения переменных разбиения":
                        getStringFromProperties("CONTENT2VALSHEADER", null, properties);
                    report.addRowHeader(content2valsheader,"HEADER");
                    
                    
                    //Общее
                    report.addRowHeader("Размер выборки","HEADER");
                    
                    report.addRowHeader("В группах","HEADER");
                    
                    //Средние
                    if (!dontshowmean)
                    {
                        report.addRowHeader("MEAN "+content3Name);
                        report.addRowType("MEAN "+content3Name,"VALUE;DA;NOBOTTOMBORDER");
                
                        report.addRowHeader("MEAN DA "+content3Name);
                        report.addRowType("MEAN DA "+content3Name,"VALUE;DA;NOCHANGEODD");
                        
                        if (debugvals)
                        {
                            report.addRowHeader("VARIANCE "+content3Name);
                            report.addRowType("VARIANCE "+content3Name,"VALUE;DA");
                            
                            report.addRowHeader("SEMEAN "+content3Name);
                            report.addRowType("SEMEAN "+content3Name,"VALUE;DA");
                        }
                
                        
                        
                        report.addRowHeader("SEMEAN Conf. Interval "+owr.content3.getName());
                        report.addRowType("SEMEAN Conf. Interval "+owr.content3.getName(),"VALUE;NOBOTTOMBORDER");
                        
                        report.addRowHeader("SEMEAN Conf. Interval_"+owr.content3.getName());
                        report.addRowType("SEMEAN Conf. Interval_"+owr.content3.getName(),"VALUE;NOCHANGEODD");
                
                        if (debugvals)
                        {
                            
                            //report.addRowHeader("DIFFERENCE "+owr.content3.getName(),"VALUE");
                        
                            //report.addRowHeader("STUDENT "+owr.content3.getName(),"VALUE");
                            
                            //report.addRowHeader("SD","VALUE");
                        }
                        
                        
                    }
                    //Группы
                    if (!dontshowgroups)
                    {
                        //NPS
                        if (!dontshownps)
                        {   
                            report.addRowHeader("NPS "+content3Name,"VALUE;DA;PERCENTAGES;NOBOTTOMBORDER");
                                                
                            report.addRowHeader("NPSDA 2s","VALUE;NOCHANGEODD;DA");
                        
                            report.addRowHeader("ConfInt","VALUE;NOBOTTOMBORDER");
                        
                            report.addRowHeader("ConfInt_","VALUE;NOCHANGEODD");
                            
                            //Группы NPS
                            agList = owr.wig_NPSReportList.get(0).groupslist;   
                            List<String> gnameList = agList.stream().map(InterviewGroup::getName).collect(toList());
                            //gnameList.sort(new StringIntComparator());
                            for (String name: gnameList)
                            {
                                report.addRowHeader(name,"VALUE;DA;PERCENTAGES;NOBOTTOMBORDER");
                                report.addRowHeader("Значимости "+name,"VALUE;NOCHANGEODD;DA");
                            }
                        }
                        //noNPS=true
                        else
                        {
                            
                        }
                        
                        
                        
                    }
                    
                    
                }
                //--------------------------------------
                //   КОНЕЦ ОФОРМЛЕНИЯ ЗАГОЛОВКОВ РЯДОВ
                //--------------------------------------
                
                content2counter++;
            }//------------------------------------
            //     КОНЕЦ ЦИКЛА ПО CONTENT2LIST
            //-------------------------------------
            
            
            //Общие настройки для всех переменных Content2
            if (i==0)
            {
                //число разделов по значениям Content1 (в одном сэмпле)
                
                //размер раздела по значению переменной Content1;
                report.setVolumeWidth(countVolumePartWidth(cont2columnWidthMap));
                //Заполнение карты цветов для MEAN DA
                report.getColorMap().put("MEAN DA "+content3Name,
                        Collections.nCopies
                        (
                                //общий размер отчета (всего колонок с данными по всем сэмплам)
                                content1valscount*      //Число значений Content1
                                sampleList.size()*      //Число сэмплов
                                report.getVolumeWidth() //Размер (в колонках) отчета по одному значению Content1
                                , 
                                Report.DEFAULTPOSITIVECOLOR
                        )
                );
                report.getColorMap().put("NPSDA 2s",
                        Collections.nCopies
                        (
                                //общий размер отчета (всего колонок с данными по всем сэмплам)
                                content1valscount*      //Число значений Content1
                                sampleList.size()*      //Число сэмплов
                                report.getVolumeWidth() //Размер (в колонках) отчета по одному значению Content1
                                , 
                                Report.DEFAULTPOSITIVECOLOR
                        )
                );
                
            }
            
            //Заголовки колонок
            OlgaWeightedReport owr = curowrList.get(0);
            List<String>volumenameslist=new ArrayList<>();
            for (InterviewGroup ag1: owr.gcr.getAgroup1())
            {
                //-----------РАЗДЕЛЫ-Сontent1Vals
                String name1 = ag1.getName();
                if (owr.gcr.isAgrop1fictive())
                    if (owr.content1.getAnswerCodeMap()!=null)
                    {
                        name1 = owr.content1.getAnswerCodeMap().get(ag1.getName());
                    }
                if (name1.equals(""))
                    name1 = ag1.getName();
                volumenameslist.add(name1);
                if (addall)
                {
                    volumenameslist.add(0,"Все");
                    List<String> namelist = new ArrayList<>();
                    for (InterviewGroup ag2: owr.gcr.getAgroup2())
                    {
                        namelist.add(ag2.getName());
                    }
                    report.addStrToList(namelist, volumeheader);
                }
                //--------------------------------
                
                //----------РАЗДЕЛЫ-Content2Names
                report.addStrToList(content2List, content2namesheader);
                //--------------------------------
                    
                //----------РАЗДЕЛЫ-Content2Vals
                for (String var2name: content2List)
                {
                    List<InterviewGroup> ig2list = content2reportMap.get(var2name).get(0).gcr.getAgroup2();
                    
                    List<String> addtolist = ig2list.stream().map(InterviewGroup::getName).collect(toList());
                    report.addStrToList(
                        addtolist, 
                    content2valsheader);
                }
                //--------------------------------
            }
            report.addStrToList(volumenameslist, volumeheader);
            
            //-----------------------------------------------------------------
            //Добавление данных в отчет
            //-----------------------------------------------------------------
            final List<InterviewGroup> ig1List = owr.gcr.getAgroup1();
            
            


            //--------------------Размер выборки-------------------------------
            List<Number> rvlist = new ArrayList<>();
            for (InterviewGroup ig1: ig1List)
            {
                String ig1name = ig1.getName();
                for (String var2name: content2List)
                {
                    owr = content2reportMap.get(var2name).get(i);
                    final List<InterviewGroup> ig2List = owr.gcr.getAgroup2();
                    for (InterviewGroup ig2: ig2List)
                    {
                        String ig2name = ig2.getName();
                        double value = owr.gcr.getInterviewsByGroupNames(ig1name, ig2name);
                        rvlist.add(report.round_noperc(value));
                        //System.out.println(ig1name+": "+var2name+": "+ig2name+": "+value);
                    }
                }
            }
            report.addToList(rvlist, "Размер выборки");
            //------------------------------------------------------------------
            
            


            //---------------------В группах------------------------------------
            List<Number> groupedtotalcountlist = new ArrayList<>();
            int ig1counter=0;
            for (InterviewGroup ig1: ig1List)
            {
                String ig1name = ig1.getName();
                int var2counter=0;
                for (String var2name: content2List)
                {
                    owr = content2reportMap.get(var2name).get(i);
                    final List<InterviewGroup> ig2List = owr.gcr.getAgroup2();
                    int var2valcounter=0;
                    for (InterviewGroup ig2: ig2List)
                    {
                        String ig2name = ig2.getName();
                        double value = 0;
                        if (!dontshownps)
                            value = owr.wig_NPSReportList.get(var2valcounter+ig1counter*ig2List.size()).getGroupedTotal();
                        else if (!dontshowlinear)
                            value = owr.wig_LinearReportList.get(var2valcounter+ig1counter*ig2List.size()).getTotalGroupedWeight();
                           
                        groupedtotalcountlist.add(report.round_noperc(value));
                        var2valcounter++;
                    }
                    var2counter++;
                }
                ig1counter++;
            }
            report.addToList(groupedtotalcountlist, "В группах");
            //------------------------------------------------------------------
            
                        
            //--------------------------Средние---------------------------------
            if (!dontshowmean)
            {
                List<Number> semeanlist = new ArrayList<>();
                List<Number> varmeanlist = new ArrayList<>();
                List<Number> meanlist = new ArrayList<>();
                List<String> meandastringlist = new ArrayList<>();
                List<Number> olgaconfintsemeanlist = new ArrayList<>();
                List<String> olgaconfintsemeanlist_ = new ArrayList<>();
                
                
                ig1counter=0;
                for (InterviewGroup ig1: ig1List)
                {
                    String ig1name = ig1.getName();
                    int var2counter=0;
                    for (String var2name: content2List)
                    {
                        owr = content2reportMap.get(var2name).get(i);
                        
                        final List<InterviewGroup> ig2List = owr.gcr.getAgroup2();
                        int var2valcounter=0;
                        for (InterviewGroup ig2: ig2List)
                        {
                            String ig2name = ig2.getName();
                            int index = var2valcounter+ig1counter*ig2List.size();
                            ArithmeticMeanReport currentmeanreport=owr.meanReportList.get(index);
                            String dastring = owr.meandastringList.get(index);
                            Double curval = currentmeanreport.meanList.get(0);
                            Double cursemean = currentmeanreport.semeanList.get(0);
                            Double curweight = currentmeanreport.sizeList.get(0);
                            Double curvar = currentmeanreport.varianceList.get(0);
                            Double curdisperse = Math.pow(currentmeanreport.varianceList.get(0), 2);
                            meandastringlist.add(dastring);
                            meanlist.add(report.round_noperc(curval));
                            semeanlist.add(report.round_noperc(cursemean));
                            olgaconfintsemeanlist.add(report.round_noperc(cursemean*1.96));
                            olgaconfintsemeanlist_.add("");
                            varmeanlist.add(report.round_noperc(curvar));
                            if (i>0)
                            {
                                OlgaWeightedReport previosowr = content2reportMap.get(var2name).get(i);
                                ArithmeticMeanReport previosmeanreport=previosowr.meanReportList.get(index);
                                Double prevval = previosmeanreport.meanList.get(0);
                                Double prevweight = previosmeanreport.sizeList.get(0);
                                Double prevdisperse = Math.pow(previosmeanreport.varianceList.get(0),2);
                                Double davalue  = ReportUtils.getStudentDAVal2(prevval,curval,prevdisperse,curdisperse,prevweight,curweight,1-confLevel);
                                Color color = ReportUtils.getColorFromDiff(davalue);
                                mcList.add(color);
                            }
                            else
                            {
                                mcList.add(Report.DEFAULTCOLOR);
                            }
                            
                            var2valcounter++;
                        }
                        var2counter++;
                    }
                    ig1counter++;
                }
                report.addToList(olgaconfintsemeanlist,"SEMEAN Conf. Interval "+content3Name);
                report.addStrToList(olgaconfintsemeanlist_,"SEMEAN Conf. Interval_"+content3Name);
                report.addStrToList(meandastringlist, "MEAN DA "+content3Name);
                report.addToList(meanlist, "MEAN "+content3Name);
                report.getColorMap().put("MEAN "+content3Name, mcList);
                
                if (debugvals)
                {
                    report.addToList(semeanlist, "SEMEAN "+content3Name);
                    report.addToList(varmeanlist, "VARIANCE "+content3Name);
                }
                
                
                
            }
            //------------------------------------------------------------------
            
            
            //---------------------GROUPS&NPS-----------------------------------
            if (!dontshownps)
            {
                
                List<Number> npsvallist = new ArrayList<>();//для NPS
                List<String> da2slist = new ArrayList<>();//для значимости NPS
                List<Number> confintlist = new ArrayList<>();//для Conf Int
                List<String> confintlist_ = new ArrayList<>();//затычка для Conf Int
                
                Map<String,List<String>> ganormDAMap = new HashMap<>();//для значимости групп
                Map<String,List<Number>> grValMap = new HashMap<>();//для групп
                agList.forEach(group->{
                    grValMap.put(group.getName(),new ArrayList<>());
                    ganormDAMap.put(group.getName(),new ArrayList<>());
                        });
                
                ig1counter=0;
                for (InterviewGroup ig1: ig1List)
                {
                    String ig1name = ig1.getName();
                    int var2counter=0;
                    for (String var2name: content2List)
                    {
                        owr = content2reportMap.get(var2name).get(i);
                        
                        final List<InterviewGroup> ig2List = owr.gcr.getAgroup2();
                        int var2valcounter=0;
                        for (InterviewGroup ig2: ig2List)
                        {
                            int index = var2valcounter+ig1counter*ig2List.size();
                            NPSgetter curNPSrep = owr.wig_NPSReportList.get(index);
                            
                            //NPS-----------------------------------------------
                            if (!dontshownps)
                            {
                                Double curnpsval = curNPSrep==null?null:curNPSrep.getNps();
                                Double confint = curNPSrep.getConfInterval(confLevel, universe);
                                npsvallist.add(report.round(curnpsval==null?0.0:curnpsval));
                                confintlist.add(report.round_noperc(confint));
                                confintlist_.add("");
                            
                                String da = owr.dastringarray2s[index];
                                da2slist.add(da);
                                
                                if (i>0)
                                {
                                    OlgaWeightedReport prevowr = content2reportMap.get(var2name).get(i-1);
                                    NPSgetter prevNPSrep = prevowr.wig_NPSReportList.get(index);
                                    Double prevnpsval = curNPSrep.getNps();
                                    
                                    //da2slist.add(da);
                                    if (prevNPSrep!=null&&curNPSrep!=null)
                                    {    
                                        DAReport crossdarep = new DAReport(prevNPSrep, curNPSrep, var2name, var2name, confLevel, universe);
                                        String conclusion2s = crossdarep.conclusion2s;
                                        if ((conclusion2s!=null)&&(conclusion2s.equals("Different")))
                                            if (curnpsval>prevnpsval)
                                            {
                                                cList.add(Report.DEFAULTPOSITIVECOLOR);
                                            }
                                            else
                                            {
                                                cList.add(Report.DEFAULTNEGATIVECOLOR);
                                            }
                                            else
                                                cList.add(Report.DEFAULTCOLOR);
                                    }
                                    else
                                        cList.add(Report.DEFAULTCOLOR);
                                }
                                else
                                    cList.add(Report.DEFAULTCOLOR);
                            }
                            
                            
                            //ГРУППЫ--------------------------------------------
                            for (InterviewGroup ag: agList)
                            {
                                String gname = ag.getName();
                                List<Number> valList = new ArrayList<>();
                                WeightedInterviewGroupNPSReport currentrep = (WeightedInterviewGroupNPSReport)curNPSrep,oldrep;
                                Double size = currentrep.getGroupedTotal();
                                Double count = currentrep.weightedCountmap.getOrDefault(currentrep.findGroupByName(gname),0.0);
                                Double percent = size>0?count/size*100.0:0.0;
                                grValMap.get(gname).add(report.round(percent));
                                if (i>0)
                                {
                                    oldrep=content2reportMap.get(var2name).get(i-1).wig_NPSReportList.get(index);
                                    Double oldsize = currentrep.getGroupedTotal();
                                    Double oldcount = oldrep.weightedCountmap.getOrDefault(oldrep.findGroupByName(gname),0.0);
                                    Double oldpercent = oldsize>0?oldcount/oldsize*100.0:0.0;
                                    Double davalue = ReportUtils.getNormDAVal(oldpercent, percent, oldsize, size);
                                    Color color = ReportUtils.getColorFromDiff(davalue);
                                    report.addToColorMap(gname,color);
                                }
                                else
                                {
                                    report.addToColorMap(gname,Report.DEFAULTCOLOR);
                                }
                                //нормальные значимости групп
                                if (owr.ganormDAMap.containsKey(gname))
                                {
                                    String ganormDAval=owr.ganormDAMap.get(gname).get(index);
                                    ganormDAMap.get(gname).add(ganormDAval);
                                    report.addToColorMap("Значимости "+gname, Report.DEFAULTPOSITIVECOLOR);
                                }
                            }
                            var2valcounter++;
                        }
                        var2counter++;
                    }
                    ig1counter++;
                }
                
                for (InterviewGroup ag: agList)
                {
                    report.addToList(grValMap.get(ag.getName()),ag.getName());
                    report.addStrToList(ganormDAMap.get(ag.getName()), "Значимости "+ag.getName());
                }
                
                      
                {
                    report.addToList(npsvallist, "NPS "+content3Name);
                    report.addStrToList(da2slist,"NPSDA 2s");
                    report.addToList(confintlist,"ConfInt");
                    report.addStrToList(confintlist_, "ConfInt_");
                    report.getColorMap().put("NPS "+content3Name, cList);
                }
                
                
                
                
            }
            //------------------------------------------------------------------
            
            //------Линейный----------------------------------------------------
            if (!dontshowlinear)
            {
                String rowname=null;
                
                Map<String,String> valtorownameMap = new HashMap<>();//карта код ответа -> заголовок ряда отчета
                Map<String,List<String>> lrnormDAMap = new HashMap<>();//для значимости групп
                Map<String,List<Number>> lrValMap = new HashMap<>();//для групп
                List<Number> lineartlist = new ArrayList<>();
                List<String> normDAlist = new ArrayList<>();
                
                
                
                if (!dontshownps)
                for (String val:uniquevalset)
                {
                    
                        
                    if (report.hasProperty("ShowAnswersText")&&report.getProperty("ShowAnswersText").equalsIgnoreCase("true")&&
                       (owr.content3.getAnswerCodeMap()!=null)&&(owr.content3.getAnswerCodeMap().isEmpty()==false))
                    {    
                        String answer = owr.content3.getAnswerCodeMap().get(val);
                        if ((answer!=null)&&(!answer.isEmpty()))
                            rowname=answer;
                        else
                            rowname = val;
                    }
                    else
                        rowname = val;
                    valtorownameMap.put(val, rowname);
                    report.addRowHeader(rowname);
                    lrValMap.put(rowname,new ArrayList<>());
                    
                        if (owr.linormDAMap!=null&&owr.linormDAMap.containsKey(val))
                        {
                            report.addRowHeader("Значимость "+rowname,"VALUE;NOCHANGEODD;DA");
                            report.addRowType(rowname,"VALUE;DA;NOBOTTOMBORDER;PERCENTAGES");
                            lrnormDAMap.put(rowname,new ArrayList<>());
                            report.getColorMap().put("Значимость "+rowname,
                                Collections.nCopies(
                                    //общий размер отчета (всего колонок с данными по всем сэмплам)
                                    content1valscount*      //Число значений Content1
                                    sampleList.size()*      //Число сэмплов
                                    report.getVolumeWidth() //Размер (в колонках) отчета по одному значению Content1
                                    ,Report.DEFAULTPOSITIVECOLOR)
                                );
                        }
                        else
                        {
                            report.addRowType(rowname,"VALUE;DA;PERCENTAGES");
                        }
                }
                else
                {
                    for (String val:uniquevalset)
                    {
                        
                        report.addRowHeader(val, "VALUE;DA;NOBOTTOMBORDER;PERCENTAGES");
                        
                        lrValMap.put(val,new ArrayList<>()); 
                        if (owr.linormDAMap!=null&&owr.linormDAMap.containsKey(val))
                        {
                            report.addRowHeader("Значимость "+val, "VALUE;NOCHANGEODD;DA");
                            lrnormDAMap.put(val,new ArrayList<>());
                            report.getColorMap().put("Значимость "+val,
                                Collections.nCopies(
                                    //общий размер отчета (всего колонок с данными по всем сэмплам)
                                    content1valscount*      //Число значений Content1
                                    sampleList.size()*      //Число сэмплов
                                    report.getVolumeWidth() //Размер (в колонках) отчета по одному значению Content1
                                    ,Report.DEFAULTPOSITIVECOLOR)
                                );
                        }
                        
                    }
                }
                
                ig1counter=0;
                for (InterviewGroup ig1: ig1List)
                {
                    String ig1name = ig1.getName();
                    int var2counter=0;
                    for (String var2name: content2List)
                    {
                        owr = content2reportMap.get(var2name).get(i);
                        OlgaWeightedReport previosowr = null;
                        if (i>0) 
                            previosowr= content2reportMap.get(var2name).get(i-1);
                        final List<InterviewGroup> ig2List = owr.gcr.getAgroup2();
                        int var2valcounter=0;
                        for (InterviewGroup ig2: ig2List)
                        {
                            int index = var2valcounter+ig1counter*ig2List.size();
                            WeightedInterviewGroupReport lr = owr.wig_LinearReportList.get(index);
                            WeightedInterviewGroupReport oldlr = previosowr!=null?previosowr.wig_LinearReportList.get(index):null;
                            
                            for (String val:uniquevalset)
                            {
                                String rowheader=val;
                                if (!dontshownps)
                                {
                                    rowheader=valtorownameMap.getOrDefault(val, val);
                                }
                                else
                                {
                                    System.out.print("");
                                }
                                double countcur = lr.weightedCountmap.getOrDefault(lr.findGroupByName(val), 0.0);
                                double weightcur = lr.getTotalGroupedWeight();
                                double v = weightcur!=0?countcur*100.0/weightcur:0.0;
                                double lrpercent = report.round(v);
                                if (oldlr!=null)
                                {
                                    double countold = oldlr.weightedCountmap.getOrDefault(oldlr.findGroupByName(val), 0.0);
                                    double weightold = oldlr.getTotalGroupedWeight();
                                    double oldv = weightold!=0?countold*100.0/weightold:0.0;
                                    double oldlrpercent = report.round(oldv);
                                    Double davalue = ReportUtils.getNormDAVal(oldlrpercent, lrpercent, weightold, weightcur);
                                    Color color = ReportUtils.getColorFromDiff(davalue);
                                    report.addToColorMap(rowheader,color);
                                }
                                else
                                {
                                    report.addToColorMap(rowheader,Report.DEFAULTCOLOR);
                                }
                                if (lrnormDAMap.containsKey(rowheader))
                                {
                                    String daval =owr.linormDAMap.get(val).get(index);
                                    lrnormDAMap.get(rowheader).add(daval);
                                }
                                lrValMap.get(rowheader).add(lrpercent);
                                
                            }
                            
                            
                            var2valcounter++;
                            
                        }
                        var2counter++;
                    }
                    ig1counter++;
                }
                for (String val:uniquevalset)
                {
                    String rowheader=valtorownameMap.getOrDefault(val, val);
                    lineartlist = lrValMap.get(rowheader);
                    if (lrnormDAMap.containsKey(rowheader))
                    {
                        normDAlist=lrnormDAMap.get(rowheader);
                        report.addStrToList(normDAlist, "Значимость "+rowheader);
                        
                    }
                    report.addToList(lineartlist, rowheader);
                }
                
            }
            
            
            
            System.out.println("Новый сэмпл:"+(i+1));
            
        }
        //------------------------------------
        //     КОНЕЦ ГЛАВНОГО ЦИКЛА (по сэмплам)
        //-------------------------------------
        //размер сэмпла
        report.setSampleWidth(report.getVolumeWidth()*content1valscount);
        
        report.setColumnHeaderWidthMap(cont2columnWidthMap);
        
        return report;
    }
    
    private int countVolumePartWidth( Map<String,Integer> cont2columnWidthMap)
    {
        return cont2columnWidthMap.entrySet().stream().mapToInt(entry->entry.getValue()).sum();
    }
    
    public int getFPDIGITSFromProperties(Properties reportProperties)
    {
        if (reportProperties==null)
            return Report.DEFAULTFPDIGITS;
        
        if (reportProperties.containsKey("fpdigits"))
            return ReportUtils.getIntFromProperties("fpdigits", Report.DEFAULTFPDIGITS, reportProperties);
        
        if (reportProperties.containsKey("FPDIGITS"))
            return ReportUtils.getIntFromProperties("FPDIGITS", Report.DEFAULTFPDIGITS, reportProperties);
        
        return Report.DEFAULTFPDIGITS;
            
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
