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
        List<OlgaReport> olrlist = new ArrayList<>();
        TreeSet<String> uniquevallist;
        String volumeheader = null, totalrowheader = null;
        //Главный заголовок
                
        universe = getUniverseFromProperties(universe,properties);
        confLevel = getConflevelFromProperties(confLevel, properties);
        weightContentName = getWeghtcontentnameFromProperties(weightContentName,properties); 
        
        for (int i=0; i<sampleList.size(); i++)
        {
            OlgaWeightedReport owr = new OlgaWeightedReport(sampleList.get(i), content, properties, level3node);
            OlgaReport olr = new OlgaReport(sampleList.get(i), content, properties, level3node.findRootNode());
            olrlist.add(olr);
            if (i==0)
            {
                //Заголовок разделов (заголовок 1)
                volumeheader = getStringFromProperties("VOLUMEHEADER", null, properties)==null?olr.content1.getName():
                        getStringFromProperties("VOLUMEHEADER", null, properties);                        
                        
                //Заголовок 2
                totalrowheader =  getStringFromProperties("HEADER", null, properties)==null?olr.content2.getName():
                        getStringFromProperties("HEADER", null, properties);
                
                //Заголовок базы
                boolean mustdrawtotal = getBooleanFromProperties("drawtotal",true,properties);
                report.setNoFirstString(mustdrawtotal);
                
                //Уникальные значения линейного отчета
                uniquevallist = ContentUtils.getContentUniqueValuesFromSampleList(sampleList, olr.content3);
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
