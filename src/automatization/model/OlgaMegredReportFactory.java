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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

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
        String weightContentName = null;
        weightContentName = getWeghtcontentnameFromProperties(weightContentName,properties);
        
        //Имя рабочей переменной (thirdvar)
        String content3Name =ReportUtils.getStringFromProperties("thirdvar",null,properties);
        
        //Инициализация заголовков ряда главной переменной и ряда базы
        String volumeheader = null, totalrowheader = null;
        
        //Значение переменной AddAll (добавлять или нет столбцы все для главной переменной)
        boolean addall = ReportUtils.getBooleanFromProperties("AddAll", false, properties);
        
        //Инициализация списка групп отчета NPS
        List<InterviewGroup> agList = null;
        
        //Округление действительных (цифры после запятой) из свойств
        int fpdigits = getFPDIGITSFromProperties(properties);
        report.setFpDIGITS(fpdigits);
        
        //
        TreeSet<String> uniquevalset = null;
        
        //Флаги отображения различных частей отчета
        boolean debugvals = getBooleanFromProperties("DebugVals",false,properties);
        boolean dontshowmean = getBooleanFromProperties("noMean",false,properties);
        boolean dontshowgroups = getBooleanFromProperties("noGroups",false,properties);
        boolean dontshownps = getBooleanFromProperties("noNPS",false,properties);
        boolean dontshowlinear = getBooleanFromProperties("noLinear",false,properties);
        
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
        
        int reportSamleWidth = 0;
        
        //------------------------
        //ГЛАВНЫЙ ЦИКЛ (ПО СЭМПЛАМ)
        //------------------------
        List<OlgaWeightedReport> prevowrList,curowrList = null; 
        for (int i=0; i<sampleList.size(); i++)
        {
            //Если нет второй переменной разбиения вернуть отчет пустой
            if (content2List==null)
                return null;
            //Инициализация списка отчетов и предыдущего для разных Content2
            prevowrList = curowrList; 
            curowrList = new ArrayList<>(content2List.size());
            
            //Ширина одного сэмпла
            int reportVolumeWidth = 0;
            
            int content2counter=0;//счетчик по списку content2List
            
            //_________________________________________________________________
            //_________________________________________________________________
            for (String content2Name: content2List)
            {
                //Получаем данные отчета OlgaWeightedReport для переменной content2Name
                properties.put("secondvar", content2Name);
                
                OlgaWeightedReport owr = new OlgaWeightedReport(sampleList.get(i), content, properties, level3node.findRootNode());
                OlgaWeightedReport prevowr = i>0?prevowrList.get(content2counter):null;
                reportVolumeWidth+=owr.gcr.getAgroup2().size();//Обновление значения ширины 1 сэмла отчета
                curowrList.add(owr);
                
                if (i==0)
                {
                    //Заголовок базы
                    boolean mustdrawtotal = getBooleanFromProperties("drawtotal",true,properties);
                    report.setNoFirstString(mustdrawtotal);
                
                    //Уникальные значения линейного отчета
                    uniquevalset = ContentUtils.getContentUniqueValuesFromSampleList(sampleList, owr.content3);
                
                    //Размер раздела сэмпла
                    report.setVolumeWidth(reportVolumeWidth);
                    
                    //Заголовок разделов (заголовок 1)
                    volumeheader = getStringFromProperties("VOLUMEHEADER", null, properties)==null?owr.content1.getName():
                        getStringFromProperties("VOLUMEHEADER", null, properties);                        
                    report.addRowHeader(volumeheader);
                    report.addRowType(volumeheader,"VOLUMEHEADER");
                    
                    //Заголовок имен переменных Content2Name 
                    totalrowheader =  getStringFromProperties("CONTENT2HEADER", null, properties)==null?"Переменные разбиения":
                        getStringFromProperties("CONTENT2HEADER", null, properties);
                    report.addRowHeader(totalrowheader);
                    report.addRowType(totalrowheader,"HEADER");
                    
                    //Заголовок значений переменных Content2Name 
                    totalrowheader =  getStringFromProperties("HEADER", null, properties)==null?owr.content2.getName():
                        getStringFromProperties("HEADER", null, properties);
                    report.addRowHeader(totalrowheader);
                    report.addRowType(totalrowheader,"HEADER");
                    
                    //Общее
                    report.addRowHeader("Размер выборки");
                    report.addRowType("Размер выборки","VALUE");
                        
                    report.addRowHeader("В группах");
                    report.addRowType("В группах","VALUE");
                    
                    //Средние
                    if (!dontshowmean)
                    {
                        report.addRowHeader("MEAN "+owr.content3.getName());
                        report.addRowType("MEAN "+owr.content3.getName(),"VALUE;DA;NOBOTTOMBORDER");
                
                        report.addRowHeader("MEAN DA "+owr.content3.getName());
                        report.addRowType("MEAN DA "+owr.content3.getName(),"VALUE;DA;NOCHANGEODD");
                        
                        if (debugvals)
                        {
                            report.addRowHeader("VARIANCE "+owr.content3.getName());
                            report.addRowType("VARIANCE "+owr.content3.getName(),"VALUE");
                        }
                
                        report.addRowHeader("SEMEAN "+owr.content3.getName());
                        report.addRowType("SEMEAN "+owr.content3.getName(),"VALUE;NOBOTTOMBORDER");
                        
                        report.addRowHeader("SEMEAN Conf. Interval_"+owr.content3.getName());
                        report.addRowType("SEMEAN Conf. Interval_"+owr.content3.getName(),"VALUE;NOCHANGEODD");
                
                        /*if (debugvals)
                        {
                            report.addRowHeader("DIFFERENCE "+owr.content3.getName());
                            report.addRowType("DIFFERENCE "+owr.content3.getName(),"VALUE");
                        
                            report.addRowHeader("STUDENT "+owr.content3.getName());
                            report.addRowType("STUDENT "+owr.content3.getName(),"VALUE");
                            
                            report.addRowHeader("SD");
                            report.addRowType("SD","VALUE");
                        }*/
                        
                        //Заполнение карты цветов для MEAN DA
                        report.getColorMap().put("MEAN DA "+owr.content3.getName(),Collections.nCopies(sampleList.size()*owr.group2samples.size(), Color.GREEN));
                    }
                    //Группы
                    if (!dontshowgroups)
                    {
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
                    
                    //NPS
                    if (!dontshownps)
                    {   
                        report.addRowHeader("NPS "+owr.content3.getName());
                        report.addRowType("NPS "+owr.content3.getName(),"VALUE;DA;PERCENTAGES;NOBOTTOMBORDER");
                        
                        report.addRowHeader("NPSDA 2s");
                        report.addRowType("NPSDA 2s","VALUE;NOCHANGEODD;DA");
                        
                        report.addRowHeader("ConfInt");
                        report.addRowType("ConfInt","VALUE;NOBOTTOMBORDER;");
                        
                        report.addRowHeader("ConfInt_");
                        report.addRowType("ConfInt","VALUE;NOCHANGEODD;");
                    }
                }
                
                content2counter++;
            }
            
            //размер сэмпла
            reportSamleWidth+=reportVolumeWidth;
            report.setSampleWidth(reportSamleWidth);
        }
        return report;
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
