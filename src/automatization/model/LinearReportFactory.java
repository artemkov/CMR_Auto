/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automatization.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

/**
 *
 * @author Дина
 */
public class LinearReportFactory implements ReportFactory
{

    @Override
    public Report makeReport
        (TemplateNode<String> level3node, Content content, Properties properties, List<UniqueList<Map<Content, String>>> sampleList, List<String> sampleNames) {
            
            Report report = new Report();
            report.addProperties(properties);
            report.setContent(content);
            report.setSampleNames(sampleNames);
            
            
            

            //Получаем данные отчета по сэмплам
            List<LinearReport> replist = new LinkedList<>();
            for (UniqueList<Map<Content,String>> sample : sampleList)
            {
                LinearReport lr = new LinearReport(sample,content);
                replist.add(lr);
            }
            
            
            
            //Заголовок
            String totalrowheader = report.getHeader();
            totalrowheader=totalrowheader==null?"Линейное распределение ответов на "+content.getName():totalrowheader;
            report.addRowHeader(totalrowheader);
            report.getRowTypeMap().put(totalrowheader, "HEADER");
            
            //Определение длины отчета в ячейках
            if (report.hasProperty("HeaderTotalOnly")||report.hasProperty("HeaderPercentageOnly"))
                report.setSampleWidth(1);
            else
                report.setSampleWidth(2);
            
            

            //Значения
            TreeSet<String> contentValuesSet = ContentUtils.getContentUniqueValuesFromSampleList(sampleList, content);
            Iterator<String> iterator = contentValuesSet.iterator();
            while (iterator.hasNext())
            {
                
                String rname = iterator.next();
                
                //Вставляем текст ответов если ShowAnswersText=true
                if ((content.getAnswerCodeMap()!=null)&&(content.getAnswerCodeMap().size()!=0))
                    if ((report.hasProperty("ShowAnswersText"))&&(report.getProperty("ShowAnswersText").equalsIgnoreCase("true")))
                    {
                        String ans = content.getAnswerCodeMap().get(rname);
                        report.addAnswerCode(rname, content.getAnswerCodeMap().get(rname));
                    }
                        
                report.addRowHeader (rname);
                report.getRowTypeMap().put(rname, "VALUE");
            }
            iterator = contentValuesSet.iterator();
            for(LinearReport lr: replist)
            {
                //total
                Double total = lr.getTotal();
                List<Number> list = new LinkedList<>();
                if (!report.hasProperty("HeaderPercentageOnly"))
                    list.add(total);
                else if (report.getProperty("HeaderPercentageOnly").equalsIgnoreCase("false"))
                    list.add(total);
                if (!report.hasProperty("HeaderTotalOnly"))
                    list.add(100);
                else if (report.getProperty("HeaderTotalOnly").equalsIgnoreCase("false"))
                    list.add(100);
                report.addToList(list, totalrowheader);
                
                //data
                while(iterator.hasNext())
                {
                    String answer = iterator.next();
                    Double count = lr.getStatmap().get(answer);
                    count = count == null?0:count;
                    Double percent = total!=0?(count*10000/total)/100.0:0;
                    
                    list = new LinkedList<>();
                    if (!report.hasProperty("DataPercentageOnly"))
                        list.add(count);
                    else if (report.getProperty("DataPercentageOnly").equalsIgnoreCase("false"))
                        list.add(count);
                    if (!report.hasProperty("DataCountOnly"))
                        list.add(percent);
                    else if (report.getProperty("DataCountOnly").equalsIgnoreCase("false"))
                        list.add(percent);
                    report.addToList(list, answer);
                }
            }
            
            
            
            return report;
    }
    
}
