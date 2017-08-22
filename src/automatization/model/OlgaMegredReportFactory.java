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
        if (content2ListStr!=null)
        {
            content2List = Arrays.asList(content2ListStr.split("[,;]"));
        }
        else
        {
            String content2Name = properties.getProperty("content2");
            if (content2Name!=null&&!content2Name.isEmpty())
            {
                content2List = Arrays.asList(content2Name);
            }
        }
        
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
            
            
            
            int content2counter=0;//счетчик по списку content2List
            //----------------------------------------------
            //ЦИКЛ ПО ПЕРЕМЕННЫМ УКАЗАННЫМ В "content2List"
            //----------------------------------------------
            for (String content2Name: content2List)
            {
                //Получаем данные отчета OlgaWeightedReport для переменной content2Name
                properties.put("secondvar", content2Name);
                OlgaWeightedReport owr = new OlgaWeightedReport(sampleList.get(i), content, properties, level3node.findRootNode());
                OlgaWeightedReport previosowr = i>0?prevowrList.get(content2counter):null;
                curowrList.add(owr);
            }
        }
        return report;
    }
    
    
    
    public int getFPDIGITSFromProperties(Properties reportProperties)
    {
        if (reportProperties==null)
            return Report.DEFAULTFPDIGITS;
        
        if (reportProperties.contains("fpdigits"))
            return ReportUtils.getIntFromProperties("fpdigits", Report.DEFAULTFPDIGITS, reportProperties);
        
        if (reportProperties.contains("FPDIGITS"))
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
