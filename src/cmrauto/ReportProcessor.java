/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmrauto;

import automatization.exceptions.GroupsFileNotFoundException;
import automatization.exceptions.InvalidFilterException;
import automatization.exceptions.InvalidGroupsFileFormatException;
import automatization.exceptions.InvalidTemplateFileFormatException;
import automatization.exceptions.NoSampleDataException;
import automatization.exceptions.ReportFileNotFoundException;
import automatization.exceptions.ReportParamsNotDefinedException;
import automatization.exceptions.TemplateFileIOException;
import automatization.exceptions.VariableNotFoundException;
import automatization.model.AnswerGroup;
import automatization.model.Content;
import automatization.model.ContentUtils;
import automatization.model.Filter;
import automatization.model.GroupsReport;
import automatization.model.NPSReport;
import automatization.model.Report;
import automatization.model.ReportUtils;
import automatization.model.StringIntComparator;
import automatization.model.TemplateNode;
import automatization.model.UniqueList;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Артем Ковалев
 */
public class ReportProcessor 
{
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReportProcessor.class);
    private static void loadStyles (XSSFWorkbook wb, Properties properties)
    {
            
            //1
            //Стиль ячейки borderboldStyle
        
            
            CellStyle borderboldStyle = wb.createCellStyle();
            borderboldStyle.setAlignment(CellStyle.ALIGN_CENTER);
            borderboldStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            borderboldStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            borderboldStyle.setBorderBottom(CellStyle.BORDER_THIN);
            borderboldStyle.setBorderLeft(CellStyle.BORDER_THIN);
            borderboldStyle.setBorderRight(CellStyle.BORDER_THIN);
            borderboldStyle.setBorderTop(CellStyle.BORDER_THIN);
            XSSFFont font= wb.createFont();
            font.setFontHeightInPoints((short)10);
            font.setFontName("Arial");
            font.setColor(IndexedColors.BLACK.getIndex());
            font.setBold(true);
            font.setItalic(false);
            borderboldStyle.setFont(font);
        
            //2
            //Стиль ячейки borderStyleOdd
            CellStyle borderStyleOdd = wb.createCellStyle();
            borderStyleOdd.setAlignment(CellStyle.ALIGN_CENTER);
            borderStyleOdd.setBorderBottom(CellStyle.BORDER_THIN);
            borderStyleOdd.setBorderLeft(CellStyle.BORDER_THIN);
            borderStyleOdd.setBorderRight(CellStyle.BORDER_THIN);
            borderStyleOdd.setBorderTop(CellStyle.BORDER_THIN);
            borderStyleOdd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            borderStyleOdd.setFillPattern(CellStyle.SOLID_FOREGROUND);
            
            XSSFFont font2= wb.createFont();
            font2.setFontHeightInPoints((short)10);
            font2.setFontName("Arial");
            font2.setColor(IndexedColors.BLACK.getIndex());
            font2.setBold(false);
            borderStyleOdd.setFont(font2);
            
            //3
            //Стиль ячейки borderStyleNotOdd
            CellStyle borderStyleNotOdd = wb.createCellStyle();
            borderStyleNotOdd.setAlignment(CellStyle.ALIGN_CENTER);
            borderStyleNotOdd.setBorderBottom(CellStyle.BORDER_THIN);
            borderStyleNotOdd.setBorderLeft(CellStyle.BORDER_THIN);
            borderStyleNotOdd.setBorderRight(CellStyle.BORDER_THIN);
            borderStyleNotOdd.setBorderTop(CellStyle.BORDER_THIN);
            borderStyleNotOdd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            borderStyleNotOdd.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            borderStyleNotOdd.setFillPattern(CellStyle.LESS_DOTS);
            borderStyleNotOdd.setFont(font2);
            
            //4
            //Стиль ячейки headerStyle
            CellStyle headerStyle = wb.createCellStyle();
            XSSFFont hfont= wb.createFont();
            hfont.setFontHeightInPoints((short)14);
            hfont.setFontName("Arial");
            hfont.setColor(IndexedColors.DARK_BLUE.getIndex());
            hfont.setBold(false);
            hfont.setItalic(true);
            headerStyle.setFont(hfont);
            
            //5
            //Стиль ячейки headerStyle2
            CellStyle headerStyle2 = wb.createCellStyle();
            headerStyle2.setWrapText(true);
            XSSFFont hfont2= wb.createFont();
            hfont2.setFontHeightInPoints((short)12);
            hfont2.setFontName("Arial");
            hfont2.setColor(IndexedColors.BLUE_GREY.getIndex());
            hfont2.setBold(true);
            hfont2.setItalic(true);
            headerStyle2.setFont(hfont2);
            
            
            //6
            //Стиль ячейки baseStyle
            CellStyle baseStyle = wb.createCellStyle();
            XSSFFont basefont= wb.createFont();
            basefont.setFontHeightInPoints((short)12);
            basefont.setFontName("TimesNewRoman");
            basefont.setColor(IndexedColors.BROWN.getIndex());
            basefont.setBold(true);
            basefont.setItalic(false);
            baseStyle.setFont(basefont);
            
            //7
            //Стиль ячейки sampleHeader
            CellStyle sampleHeaderStyle = wb.createCellStyle();
            sampleHeaderStyle.setBorderLeft(CellStyle.BORDER_DOUBLE);
            sampleHeaderStyle.setBorderRight(CellStyle.BORDER_DOUBLE);
            sampleHeaderStyle.setBorderTop(CellStyle.BORDER_DOUBLE);
            sampleHeaderStyle.setBorderBottom(CellStyle.BORDER_DOUBLE);
            sampleHeaderStyle.setWrapText(false);
            XSSFFont sampleHeaderFont= wb.createFont();
            sampleHeaderFont.setFontHeightInPoints((short)12);
            sampleHeaderFont.setFontName("Colibri");
            sampleHeaderFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            sampleHeaderFont.setBold(true);
            sampleHeaderFont.setItalic(true);
            //sampleHeaderFont.setUnderline(FontUnderline.SINGLE);
            sampleHeaderStyle.setFont(sampleHeaderFont);
            
            //8
            //Стиль ячейки borderStyleOddEnd1
            CellStyle borderStyleOddEnd1 = wb.createCellStyle();
            borderStyleOddEnd1.setAlignment(CellStyle.ALIGN_CENTER);
            borderStyleOddEnd1.setBorderBottom(CellStyle.BORDER_THIN);
            borderStyleOddEnd1.setBorderLeft(CellStyle.BORDER_THIN);
            borderStyleOddEnd1.setBorderRight(CellStyle.BORDER_DOUBLE);
            borderStyleOddEnd1.setBorderTop(CellStyle.BORDER_THIN);
            borderStyleOddEnd1.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            borderStyleOddEnd1.setFillPattern(CellStyle.SOLID_FOREGROUND);
            borderStyleOddEnd1.setFont(font2);
            
            //9
            //Стиль ячейки borderStyleNotOddEnd1
            CellStyle borderStyleNotOddEnd1 = wb.createCellStyle();
            borderStyleNotOddEnd1.setAlignment(CellStyle.ALIGN_CENTER);
            borderStyleNotOddEnd1.setBorderBottom(CellStyle.BORDER_THIN);
            borderStyleNotOddEnd1.setBorderLeft(CellStyle.BORDER_THIN);
            borderStyleNotOddEnd1.setBorderRight(CellStyle.BORDER_DOUBLE);
            borderStyleNotOddEnd1.setBorderTop(CellStyle.BORDER_THIN);
            borderStyleNotOddEnd1.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            borderStyleNotOddEnd1.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            borderStyleNotOddEnd1.setFillPattern(CellStyle.LESS_DOTS);
            borderStyleNotOddEnd1.setFont(font2);
            
            //10
            //Стиль ячейки borderboldStyleEnd1
            CellStyle borderboldStyleEnd1 = wb.createCellStyle();
            borderboldStyleEnd1.setAlignment(CellStyle.ALIGN_CENTER);
            borderboldStyleEnd1.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            borderboldStyleEnd1.setFillPattern(CellStyle.SOLID_FOREGROUND);
            borderboldStyleEnd1.setBorderBottom(CellStyle.BORDER_THIN);
            borderboldStyleEnd1.setBorderLeft(CellStyle.BORDER_THIN);
            borderboldStyleEnd1.setBorderRight(CellStyle.BORDER_DOUBLE);
            borderboldStyleEnd1.setBorderTop(CellStyle.BORDER_THIN);
            borderboldStyleEnd1.setFont(font);
            
            //11
            //Стиль ячейки borderStyleOddEnd2
            CellStyle borderStyleOddEnd2 = wb.createCellStyle();
            borderStyleOddEnd2.setAlignment(CellStyle.ALIGN_CENTER);
            borderStyleOddEnd2.setBorderBottom(CellStyle.BORDER_THIN);
            borderStyleOddEnd2.setBorderLeft(CellStyle.BORDER_THIN);
            borderStyleOddEnd2.setBorderRight(CellStyle.BORDER_MEDIUM);
            borderStyleOddEnd2.setBorderTop(CellStyle.BORDER_THIN);
            borderStyleOddEnd2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            borderStyleOddEnd2.setFillPattern(CellStyle.SOLID_FOREGROUND);
            borderStyleOddEnd2.setFont(font2);
            
            //12
            //Стиль ячейки borderStyleNotOddEnd2
            CellStyle borderStyleNotOddEnd2 = wb.createCellStyle();
            borderStyleNotOddEnd2.setAlignment(CellStyle.ALIGN_CENTER);
            borderStyleNotOddEnd2.setBorderBottom(CellStyle.BORDER_THIN);
            borderStyleNotOddEnd2.setBorderLeft(CellStyle.BORDER_THIN);
            borderStyleNotOddEnd2.setBorderRight(CellStyle.BORDER_MEDIUM);
            borderStyleNotOddEnd2.setBorderTop(CellStyle.BORDER_THIN);
            borderStyleNotOddEnd2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            borderStyleNotOddEnd2.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            borderStyleNotOddEnd2.setFillPattern(CellStyle.LESS_DOTS);
            borderStyleNotOddEnd2.setFont(font2);
            
            //13
            //Стиль ячейки borderboldStyleEnd2
            CellStyle borderboldStyleEnd2 = wb.createCellStyle();
            borderboldStyleEnd2.setAlignment(CellStyle.ALIGN_CENTER);
            borderboldStyleEnd2.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            borderboldStyleEnd2.setFillPattern(CellStyle.SOLID_FOREGROUND);
            borderboldStyleEnd2.setBorderBottom(CellStyle.BORDER_THIN);
            borderboldStyleEnd2.setBorderLeft(CellStyle.BORDER_THIN);
            borderboldStyleEnd2.setBorderRight(CellStyle.BORDER_MEDIUM);
            borderboldStyleEnd2.setBorderTop(CellStyle.BORDER_THIN);
            borderboldStyleEnd2.setFont(font);
            
            //14
            //Стиль ячейки volumeHeader
            CellStyle volumeHeaderStyle = wb.createCellStyle();
            volumeHeaderStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
            volumeHeaderStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
            volumeHeaderStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
            volumeHeaderStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
            volumeHeaderStyle.setAlignment(CellStyle.ALIGN_CENTER);
            volumeHeaderStyle.setWrapText(false);
            volumeHeaderStyle.setFillForegroundColor(IndexedColors.PINK.getIndex());
            volumeHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            XSSFFont volumeHeaderFont= wb.createFont();
            volumeHeaderFont.setFontHeightInPoints((short)11);
            volumeHeaderFont.setFontName("Colibri");
            volumeHeaderFont.setColor(IndexedColors.BLACK.getIndex());
            volumeHeaderFont.setBold(false);
            volumeHeaderFont.setItalic(false);
            volumeHeaderStyle.setFont(volumeHeaderFont);
            
             //15
            //Стиль ячейки volumeHeader
            CellStyle volumeHeaderStyleEnd = wb.createCellStyle();
            volumeHeaderStyleEnd.setBorderLeft(CellStyle.BORDER_MEDIUM);
            volumeHeaderStyleEnd.setBorderRight(CellStyle.BORDER_DOUBLE);
            volumeHeaderStyleEnd.setBorderTop(CellStyle.BORDER_MEDIUM);
            volumeHeaderStyleEnd.setBorderBottom(CellStyle.BORDER_MEDIUM);
            volumeHeaderStyleEnd.setAlignment(CellStyle.ALIGN_CENTER);
            volumeHeaderStyleEnd.setWrapText(false);
            volumeHeaderStyleEnd.setFillForegroundColor(IndexedColors.PINK.getIndex());
            volumeHeaderStyleEnd.setFillPattern(CellStyle.SOLID_FOREGROUND);
            volumeHeaderStyleEnd.setFont(volumeHeaderFont);
            
            XSSFFont greenFont= wb.createFont();
            greenFont.setColor(IndexedColors.GREEN.getIndex());
            
            XSSFFont blueFont= wb.createFont();
            blueFont.setColor(IndexedColors.BLUE.getIndex());
            
            XSSFFont redFont= wb.createFont();
            redFont.setColor(IndexedColors.RED.getIndex());
            
            XSSFFont text1Font= wb.createFont();
            text1Font.setColor(IndexedColors.RED.getIndex());
            text1Font.setFontName("Arial");
            text1Font.setFontHeightInPoints((short)11);
            
            XSSFFont text2Font= wb.createFont();
            text2Font.setColor(IndexedColors.BLACK.getIndex());
            text2Font.setFontName("Cambria");
            text2Font.setFontHeightInPoints((short)15);
            
            XSSFFont text3Font= wb.createFont();
            text3Font.setColor(IndexedColors.DARK_TEAL.getIndex());
            text3Font.setFontName("Georgia");
            text3Font.setFontHeightInPoints((short)13);
            text3Font.setBold(true);
            
            
            
    }
    private static String findValidFilename (String fileName)
    {
        String validFilename=null;
        try
        {
            if (Files.deleteIfExists(Paths.get(fileName)))
                log.info("File '"+fileName+"'was detected and deleted");
            validFilename=fileName;
            return validFilename;
        }
        catch (IOException e)
        {
            System.out.println(fileName + " занят другим процессом");
        }
        
        String filename = fileName.replace(".xlsx", "");
        int counter = 1;
        while (validFilename==null)
        {
            String filenameToCheck = filename+"("+counter+").xlsx";
            try
            {
                if (Files.deleteIfExists(Paths.get(filenameToCheck)))
                    log.info("File '"+filenameToCheck+"'was detected and deleted");
                validFilename=filenameToCheck;
                return validFilename;
            }
            catch (IOException e)
            {
                System.out.println(filenameToCheck + " занят другим процессом");
                counter++;
            }
        }
        return validFilename;
    }
    
    public static String createMultiSampleReport(TemplateNode<String> rootNode, Map<String,UniqueList<Map<Content,String>>> sampleMap) throws VariableNotFoundException, InvalidFilterException, IOException, ReportParamsNotDefinedException, InvalidTemplateFileFormatException, ReportFileNotFoundException, GroupsFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException
    {
        String fileName = rootNode.getData();
        if (!fileName.endsWith(".xlsx"))
            fileName+=".xlsx";
        
        
        fileName = findValidFilename(fileName);
        log.info("ReportFile: "+fileName);
        
        
        XSSFSheet sheet;
        Integer curRowNumb = 0;
        
        List<Report> reportList = new LinkedList<>();
        try (FileOutputStream outputStream = new FileOutputStream(fileName))
        {
            XSSFWorkbook wb = new XSSFWorkbook();
            loadStyles(wb,null);
            sheet = wb.createSheet();
            sheet.setColumnWidth(0, 85*256);
            Properties level0properties = rootNode.getParams();
            //Обработка шаблона
            for (TemplateNode<String> level1Node: rootNode.getChildren())
            {
                String variableName = level1Node.getData();
                List<UniqueList<Map<Content,String>>> sampleList = new LinkedList<>();
                List<String> sampleNames = new LinkedList<>();
                UniqueList<Map<Content,String>> interviewList = null;
                Properties level1properties = level1Node.getParams();
                Properties combined = ReportUtils.mergeProperties(level0properties, level1properties);
                
                for (Map.Entry<String, UniqueList<Map<Content,String>>> entry:sampleMap.entrySet())
                {
                    interviewList=entry.getValue();
                    if (ContentUtils.getContentByNameFromInterviewList(interviewList, variableName)==null)
                    {
                        throw new VariableNotFoundException(variableName,entry.getKey());
                    }
                    else
                    {
                        sampleList.add(interviewList);
                        sampleNames.add(entry.getKey());
                    }
                }
                
                
            
                //Поиск контента
                Set<Content> cset = interviewList.getFirst().keySet();
                Iterator<Content> csetiterator = cset.iterator();
                Content content = null;
                while(csetiterator.hasNext())
                {
                    Content c = csetiterator.next();
                    if (c.getName().equalsIgnoreCase(variableName))
                    {
                        content = c;
                        break;
                    }
                }
                if (!((level1Node.getZerolevelParams()!=null)&&(level1Node.getZerolevelParams().contains("HIDDEN"))))
                {
                    curRowNumb = drawVariableHeader(sheet,curRowNumb,content);
                    log.info("Variable header for "+variableName+" was drawn");
                }
                else
                {
                    log.info("Variable header for "+variableName+" was hidden");   
                }
                for (TemplateNode<String> level2Node : level1Node.getChildren())
                {
                    //фильтр
                    String filterString = level2Node.getData();
                    
                    List<UniqueList<Map<Content,String>>> filteredSampleList = Filter.filter(sampleList, filterString);
                    if (filteredSampleList==null||(filteredSampleList.get(0)==null&filteredSampleList.size()==1))
                    {
                        log.info("Filter '"+filterString+"' returned empty dataset. No reports will be drawn!!!");
                        continue;
                    }
                    
                    if (!((level2Node.getZerolevelParams()!=null)&&(level2Node.getZerolevelParams().contains("HIDDEN"))))
                    {
                        curRowNumb = drawBaseHeader(sheet,curRowNumb,level2Node);
                        log.info("BaseHeader "+level2Node.getData()+" was drawn");
                    }
                    else
                        log.info("BaseHeader "+level2Node.getData()+" was hidden");
                    
                    Properties level2properties = level2Node.getParams();
                    Properties lastcombined = ReportUtils.mergeProperties(combined, level2properties);
                    
                    //Отчеты составление
                    for (TemplateNode<String> level3Node : level2Node.getChildren())
                    {
                        Report report = new Report();
                        level3Node.setParams(lastcombined);
                        report=report.getMultiSampleReportFromNode(level3Node, content, filteredSampleList, sampleNames);
                        reportList.add(report);
                        if (!((level3Node.getZerolevelParams()!=null)&&(level3Node.getZerolevelParams().contains("HIDDEN"))))
                        {
                            log.info("Drawing <"+report+">...");
                            curRowNumb = drawReport(report,sheet,curRowNumb);
                            log.info("Report <"+report+"> was drawn");
                        }
                        else
                        {
                            log.info("Report <"+report+"> was not drawn, cause it's hidden");
                        }
                             
                        
                    }
                }
            }
            wb.write(outputStream);
            return fileName;
        }
        catch (GroupsFileNotFoundException gfne) 
        {
            gfne.getMessage();
            throw new TemplateFileIOException();
        }
        catch (IOException ex) 
        {
            System.out.println("Something wrong with templatefile");
            throw new TemplateFileIOException();
        }
    }
    
    /*public static void createMultiSampleReport(TemplateNode<String> rootNode, List<UniqueList<Map<Content,String>>> sampleList, List<String> sampleNames) throws VariableNotFoundException, InvalidFilterException, IOException, ReportParamsNotDefinedException, InvalidTemplateFileFormatException, ReportFileNotFoundException, GroupsFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException
    {
        String fileName = rootNode.getData();
        if (!fileName.endsWith(".xlsx"))
            fileName+=".xlsx";
        log.info("ReportFile: "+fileName);
        if (Files.deleteIfExists(Paths.get(fileName)))
            log.info("File '"+fileName+"'was detected and deleted");
        
        XSSFSheet sheet;
        Integer curRowNumb = 0;
        Integer reportsCounter=0;
        int startColumn = 0;
        List<Report> reportList = new LinkedList<>();
        try (FileOutputStream outputStream = new FileOutputStream(fileName))
        {
            XSSFWorkbook wb = new XSSFWorkbook();
            loadStyles(wb,null);
            sheet = wb.createSheet();
            sheet.setColumnWidth(0, 85*256);
            //Обработка шаблона
            for (TemplateNode<String> level1Node: rootNode.getChildren())
            {
                String variableName = level1Node.getData();
                UniqueList<Map<Content,String>> interviewList = sampleList.get(0);
                if (!interviewList.getFirst().containsKey(new Content(variableName)))
                {
                    throw new VariableNotFoundException(variableName);
                }
            
                //Поиск контента
                Set<Content> cset = interviewList.getFirst().keySet();
                Iterator<Content> csetiterator = cset.iterator();
                Content content = null;
                while(csetiterator.hasNext())
                {
                    Content c = csetiterator.next();
                    if (c.getName().equals(variableName))
                    {
                        content = c;
                        break;
                    }
                }
                curRowNumb = drawVariableHeader(sheet,curRowNumb,content);
                log.info("Variable header for "+variableName+" was drawn");
                for (TemplateNode<String> level2Node : level1Node.getChildren())
                {
                    //фильтр
                    String filterString = level2Node.getData();
                    List<UniqueList<Map<Content,String>>> filteredSampleList = Filter.filter(sampleList, filterString);
                    curRowNumb = drawBaseHeader(sheet,curRowNumb,level2Node);
                    log.info("Header "+level2Node.getData()+" was drawn");
                    //Отчеты составление
                    for (TemplateNode<String> level3Node : level2Node.getChildren())
                    {
                        Report report = new Report();
                        report.getMultiSampleReportFromNode(level3Node, content, filteredSampleList, sampleNames);
                        reportList.add(report);
                        log.info("Drawing <"+report+">...");
                        curRowNumb = drawReport(report,sheet,curRowNumb);
                        log.info("Report <"+report+"> was drawn");
                    }
                }
            }
            wb.write(outputStream);
        }
        catch (IOException ex) 
        {
            System.out.println("Something wrong with templatefile");
            throw new TemplateFileIOException();
        }
    }*/
    
    /*public static void createReport(TemplateNode<String> rootNode, UniqueList<Map<Content,String>> interviewList) throws VariableNotFoundException, InvalidFilterException, IOException, ReportParamsNotDefinedException, InvalidTemplateFileFormatException, ReportFileNotFoundException, GroupsFileNotFoundException, InvalidGroupsFileFormatException
    {
        String fileName = rootNode.getData();
        
        
        if (!fileName.endsWith(".xlsx"))
            fileName+=".xlsx";
        log.info("ReportFile: "+fileName);
        if (Files.deleteIfExists(Paths.get(fileName)))
            log.info("File '"+fileName+"'was detected and deleted");   
        
        XSSFSheet sheet;
        Integer curRowNumb = 0;
        Integer reportsCounter=0;
        List<Report> reportList = new LinkedList<>();
        
        try (FileOutputStream outputStream = new FileOutputStream(fileName))
        {
            XSSFWorkbook wb = new XSSFWorkbook();
            loadStyles(wb,null);
            sheet = wb.createSheet();
            sheet.setColumnWidth(0, 85*256);
            
            //Обработка шаблона
            for (TemplateNode<String> level1Node: rootNode.getChildren())
            {
                String variableName = level1Node.getData();
                if (!interviewList.getFirst().containsKey(new Content(variableName)))
                {
                    throw new VariableNotFoundException(variableName);
                }
            
                //Поиск контента
                Set<Content> cset = interviewList.getFirst().keySet();
                Iterator<Content> csetiterator = cset.iterator();
                Content content = null;
                while(csetiterator.hasNext())
                {
                    Content c = csetiterator.next();
                    if (c.getName().equals(variableName))
                    {
                        content = c;
                        break;
                    }
                }
                if (!level1Node.getZerolevelParams().contains("HIDDEN"))
                {
                    curRowNumb = drawVariableHeader(sheet,curRowNumb,content);
                    log.info("Variable header for "+variableName+" was drawn");
                }
                for (TemplateNode<String> level2Node : level1Node.getChildren())
                {
                    //фильтр
                    String filterString = level2Node.getData();
                    UniqueList<Map<Content,String>> filteredInterviewList = Filter.filter(interviewList, filterString);
                    if (!level2Node.getZerolevelParams().contains("HIDDEN"))
                    {
                        curRowNumb = drawBaseHeader(sheet,curRowNumb,level2Node);
                        log.info("Header "+level2Node.getData()+" was drawn");
                    }
                    
                    //Отчеты составление
                    for (TemplateNode<String> level3Node : level2Node.getChildren())
                    {
                        Report report = new Report();
                        report.getReportFromNode(level3Node, content, filteredInterviewList);
                        reportList.add(report);
                        log.info("Drawing <"+report+">...");
                        curRowNumb = drawReport(report,sheet,curRowNumb);
                        log.info("Report <"+report+"> was drawn");
                    }
                    
                    
                }
            }
            //Рисование отчета
            for (Report r:reportList)
            {
                
            }
            
            wb.write(outputStream);
        }
        catch (IOException ex) 
        {
            System.out.println("Something wrong with templatefile");
            throw new TemplateFileIOException();
        }
    }*/

    private static Integer drawVariableHeader (XSSFSheet sheet, Integer curRowNumb, Content cont)
    {
        XSSFCellStyle headerStyle = sheet.getWorkbook().getCellStyleAt((short)4);
        XSSFCellStyle headerStyle2 = sheet.getWorkbook().getCellStyleAt((short)5);
        
        curRowNumb+=2;
        
        //Название переменной
        XSSFRow row = sheet.createRow(curRowNumb++);
        XSSFCell cell = row.createCell(0);
        cell.setCellStyle(headerStyle);
        cell.setCellValue(cont.getName());
        
        //Текст вопроса
        row = sheet.createRow(curRowNumb++);
        
        cell = row.createCell(0);
        
        cell.setCellStyle(headerStyle2);
        cell.setCellValue(cont.getText());
        
        return curRowNumb;
    }
    
    private static Integer drawBaseHeader (XSSFSheet sheet, Integer curRowNumb, TemplateNode<String> baseNode)
    {
        XSSFCellStyle headerStyle = sheet.getWorkbook().getCellStyleAt((short)6);
        //База
        XSSFRow row = sheet.createRow(curRowNumb);
        XSSFCell cell = row.createCell(0);
        cell.setCellStyle(headerStyle);
        cell.setCellValue("База: "+baseNode.getData());
        
        return curRowNumb;
    }
    
    private static Integer drawReport(Report report, XSSFSheet sheet, Integer curRowNumb)
    {
        boolean isOdd = false;
        boolean isFirst = true;
        boolean noBottomFlag=false, noTopFlag = false;
        //curRowNumb++;
        
        //Главный заголовок
        XSSFRow r = sheet.createRow(curRowNumb);
        XSSFCell maincell = r.createCell(0);
        XSSFCellStyle mainstyle = sheet.getWorkbook().getCellStyleAt((short)7);
        maincell.setCellStyle(mainstyle);
        maincell.setCellValue(report.getMainHeader());
        
        
        //Ряд сэмлов
        if ((report.getSampleNames()!=null)&&(!report.getSampleNames().isEmpty())&&(!report.isNoSampleHeader()))
        {
        
            int samplesize = report.getSampleNames().size();
            
        
            for (int i =0;i<samplesize;i++)
            {
                
                XSSFCell cell = r.createCell(1+i*report.getSampleWidth());
                XSSFCellStyle style = sheet.getWorkbook().getCellStyleAt((short)7);
                
                cell.setCellStyle(style);
                int handler = sheet.addMergedRegion(new CellRangeAddress(
                    curRowNumb, //first row (0-based)
                    curRowNumb, //last row  (0-based)
                    1+i*report.getSampleWidth(), //first column (0-based)
                    (i+1)*report.getSampleWidth()  //last column  (0-based)
                ));
                
                cell.setCellValue(report.getSampleNames().get(i));
            }
            curRowNumb++;
        }
        
        
        
        
        for (String rowheader: report.getRowHeaders())
        {
            String rowType = report.getRowTypeMap().get(rowheader);
            if (rowType==null)
                System.err.println("rowType not defined!!!");
            if ((isFirst)&&(report.isNoFirstString()))
            {
                isFirst=false;
                continue;
            }
            if (rowType.startsWith("TEXT"))
            {
                XSSFFont newfont = null;
                if (rowType.equals("TEXT1"))
                {
                    newfont = sheet.getWorkbook().getFontAt((short)11);
                }
                else if (rowType.equals("TEXT2"))
                {
                    newfont = sheet.getWorkbook().getFontAt((short)12);
                }       
                else if (rowType.equals("TEXT3"))
                {
                    newfont = sheet.getWorkbook().getFontAt((short)13);
                }
                else
                {
                    String fontName = report.getProperty("fontname");
                    if ((fontName==null)||(fontName.isEmpty()))
                    {
                        fontName=sheet.getWorkbook().getFontAt((short)0).getFontName();
                    }
                
                
                    String fontSize = report.getProperty("fontsize");
                    short fsz = 15;
                    if ((fontSize!=null)&&(!fontSize.isEmpty()))
                    {
                        try 
                        {
                            fsz = Short.parseShort(fontSize);
                        }
                        catch (NumberFormatException nfe)
                        {
                        
                        }
                    }
                    String fontColor = report.getProperty("fontcolor");
                    if (fontColor == null)
                        fontColor = "BLACK";
                    
                
                
                    newfont= sheet.getWorkbook().createFont();
                    newfont.setFontHeightInPoints(fsz);
                    newfont.setFontName(fontName);
                    newfont.setColor(IndexedColors.valueOf(fontColor).getIndex());
                }   
                
                
                XSSFCell cell = r.createCell(0);
                CellUtil.setFont(cell, sheet.getWorkbook(), newfont);
                String text = report.getProperty("text");
                if (text!=null)
                {
                    cell.setCellValue(text);
                }
                curRowNumb++;
                continue;
                
            }
            if (noTopFlag)
            {
                noTopFlag=false;
            }
            
            if (noBottomFlag)
            {
                noTopFlag=true;
                noBottomFlag=false;
                
            }
            if (rowType.contains("NOBOTTOMBORDER"))
            {
                noBottomFlag=true;
            }
            
            
            if (rowType.contains("VOLUMEHEADER"))
            {
                List <? extends Object> values = report.getReportValues().get(rowheader);
                XSSFRow row = sheet.createRow(curRowNumb);
                XSSFCell cell = row.createCell(0);
                XSSFCellStyle style = sheet.getWorkbook().getCellStyleAt((short)14);
                XSSFCellStyle style_end = sheet.getWorkbook().getCellStyleAt((short)15);
                cell.setCellStyle(style_end);
                cell.setCellValue(rowheader);
                
                int volumewidth = report.getVolumeWidth();
                int samplewidth = report.getSampleWidth();
                int column=1;
                for (int i=0; i<values.size(); i++)
                {
                    
                    int cellNumb = 1+i*volumewidth;
                    cell = row.createCell(cellNumb);
                    
                    
                    int handler = sheet.addMergedRegion(new CellRangeAddress(
                        curRowNumb, //first row (0-based)
                        curRowNumb, //last row  (0-based)
                        cellNumb, //first column (0-based)
                        (i+1)*volumewidth  //last column  (0-based)
                    ));
                    int ratio =samplewidth/ volumewidth;
                    int lastColumnN = (i+1)*volumewidth;
                    XSSFCell lastcell = row.createCell(lastColumnN);
                    if (lastColumnN%samplewidth==0)
                        lastcell.setCellStyle(style_end);
                    else
                        lastcell.setCellStyle(style);
                    
                    if ((i+1)%ratio==0)
                        cell.setCellStyle(style_end);
                    else
                        cell.setCellStyle(style);
                    cell.setCellValue((String)values.get(i));
                }
                curRowNumb++;
                continue;
            }
            
            List <? extends Object> values = report.getReportValues().get(rowheader);
            XSSFRow row = sheet.createRow(curRowNumb++);
            XSSFCell cell = row.createCell(0);
            
            if (!rowType.contains("NOCHANGEODD"))
                isOdd=!isOdd;
            XSSFCellStyle style = getStyle(curRowNumb,rowheader,report,sheet,0,isOdd);
            cell.setCellStyle(style);
            CellUtil.setAlignment(cell, sheet.getWorkbook(),CellStyle.ALIGN_LEFT);
            if (noBottomFlag)
                CellUtil.setCellStyleProperty(cell, sheet.getWorkbook(), CellUtil.BORDER_BOTTOM, CellStyle.BORDER_NONE);
            if (noTopFlag)
                CellUtil.setCellStyleProperty(cell, sheet.getWorkbook(), CellUtil.BORDER_TOP, CellStyle.BORDER_NONE);
            cell.setCellValue(rowheader);
            
            
            
            if (report.getAnswerCodeMap()!=null)
                if ((report.getAnswerCodeMap().get(rowheader)!=null)&&
                    (!report.getAnswerCodeMap().get(rowheader).isEmpty()))
                    cell.setCellValue(rowheader+" \""+report.getAnswerCodeMap().get(rowheader)+"\"");
            List<Color> colList = null;
            if (isDARow(report,rowheader))
            {
                Map<String,List<Color>> colMap = report.getColorMap();
                if (colMap.containsKey(rowheader))
                {
                    colList=colMap.get(rowheader);
                }
            }
            int column = 1;
            
            for (Object value: values)
            {
                
                style = getStyle(curRowNumb,rowheader,report,sheet,column,isOdd);
                cell = row.createCell(column);
                
                //XSSFCellStyle clonedstyle = (XSSFCellStyle)style.clone();
                
                cell.setCellStyle(style);
                if (noBottomFlag)
                    CellUtil.setCellStyleProperty(cell, sheet.getWorkbook(), CellUtil.BORDER_BOTTOM, CellStyle.BORDER_NONE);
                if (noTopFlag)
                    CellUtil.setCellStyleProperty(cell, sheet.getWorkbook(), CellUtil.BORDER_TOP, CellStyle.BORDER_NONE);
                if ((colList!=null))
                {
                    try 
                    {
                        colList.get(column-1);
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        style.getFont().setColor(IndexedColors.BLACK.getIndex());
                        continue;
                    }
                    
                    
                    if (colList.get(column-1)==Color.GREEN)
                    {
                        XSSFFont font2 = sheet.getWorkbook().getFontAt((short)8);
                        CellUtil.setFont(cell, sheet.getWorkbook(), font2);
                    }
                    else if (colList.get(column-1)==Color.BLUE)
                    {
                        XSSFFont font2 = sheet.getWorkbook().getFontAt((short)9);
                        CellUtil.setFont(cell, sheet.getWorkbook(), font2);
                    }
                    else if (colList.get(column-1)==Color.RED)
                    {
                        XSSFFont font2 = sheet.getWorkbook().getFontAt((short)10);
                        CellUtil.setFont(cell, sheet.getWorkbook(), font2);
                    }
                    else
                    {
                        XSSFFont font2 = sheet.getWorkbook().getFontAt((short)0);
                        CellUtil.setFont(cell, sheet.getWorkbook(), font2);
                    }
                }
                else
                {
                    style.getFont().setColor(IndexedColors.BLACK.getIndex());
                }
                    
                
                
                if (isAnswerRow(report,rowheader))
                {
                    Map<String,String> acodemap = report.getRowAnswerCodeMap(rowheader);
                    if (acodemap!=null)
                    {
                        String answer = acodemap.get(value);
                        if ((answer!=null)&&(!answer.isEmpty()))
                        {
                            value=(String)value+" \""+answer+"\"";
                        }
                    }
                }
                
                if (value==null)
                    cell.setCellValue("");
                else
                {
                    if (value.getClass()==Double.class)
                        cell.setCellValue((Double)value);
                    if (value.getClass()==Integer.class)
                        cell.setCellValue((Integer)value);
                    if (value.getClass()==String.class)
                        cell.setCellValue((String)value);
                }
                
                column++;
            }
            isFirst=false;
        }
        return curRowNumb;
    }
    
    private static XSSFCellStyle getStyle(Integer curRowNumb, String rowheader, Report report, XSSFSheet sheet, int column, boolean isOdd) 
    {
        XSSFCellStyle style;
        Properties properties = report.getReportProperties();
        
        XSSFCellStyle borderStyleOdd = sheet.getWorkbook().getCellStyleAt((short)2);
        XSSFCellStyle borderStyleNotOdd = sheet.getWorkbook().getCellStyleAt((short)3);
        XSSFCellStyle borderboldStyle = sheet.getWorkbook().getCellStyleAt((short)1);   
        
        XSSFCellStyle borderStyleOddEnd1 = sheet.getWorkbook().getCellStyleAt((short)8);
        XSSFCellStyle borderStyleNotOddEnd1 = sheet.getWorkbook().getCellStyleAt((short)9);
        XSSFCellStyle borderboldStyleEnd1 = sheet.getWorkbook().getCellStyleAt((short)10);
        
        XSSFCellStyle borderStyleOddEnd2 = sheet.getWorkbook().getCellStyleAt((short)11);
        XSSFCellStyle borderStyleNotOddEnd2 = sheet.getWorkbook().getCellStyleAt((short)12);
        XSSFCellStyle borderboldStyleEnd2 = sheet.getWorkbook().getCellStyleAt((short)13);
        
        Map<String,String> rowTypeMap = report.getRowTypeMap();
        
        String fpDigitsByExcel = properties.getProperty("FPDIGITSBYEXCEL");
        boolean useExcelDigitsFormatting = false;
        
        int fpd = report.getFPDIGITS();
        fpDigitsByExcel=fpDigitsByExcel==null?properties.getProperty("fpdigitsbyexcel"):fpDigitsByExcel;
        if ((fpDigitsByExcel!=null)&&(fpDigitsByExcel.equalsIgnoreCase("true")))
        {
            useExcelDigitsFormatting=true;
        }
        
        
        if ((rowTypeMap.isEmpty())||(!rowTypeMap.containsKey(rowheader))||(rowTypeMap.get(rowheader).contains("VALUE")))
        {
            DataFormat df =  sheet.getWorkbook().createDataFormat();
            boolean percentages = false;
            int percentages_fpsigns = 0;
            String v = properties.getProperty("percentages");
            v=v==null?properties.getProperty("Percentages"):v;
            v=v==null?properties.getProperty("PERCENTAGES"):v;
            
            if (v!=null)
            {    
                if (v.equalsIgnoreCase("true"))
                {
                    percentages=true;
                    String vv = properties.getProperty("PERC_FPSigns", Integer.toString(fpd));
                    try
                    {
                        percentages_fpsigns=Integer.parseInt(vv);
                    }
                    catch (NumberFormatException nfe)
                    {
                        percentages_fpsigns=fpd;
                    }
                }
            
            }
            
            if (isOdd)
            {
                style = borderStyleOdd;
                if ((report.getVolumeWidth()>0)&&(column%report.getVolumeWidth()==0))
                    style = borderStyleOddEnd2;
                else
                    style = borderStyleOdd;
                if ((column)%report.getSampleWidth()==0)
                    style = borderStyleOddEnd1;
            }
            else
            {
                style = borderStyleNotOdd;
                if ((report.getVolumeWidth()>0)&&(column%report.getVolumeWidth()==0))
                    style = borderStyleNotOddEnd2;
                else
                    style = borderStyleNotOdd;
                if ((column)%report.getSampleWidth()==0)
                    style = borderStyleNotOddEnd1;
                
                    
            }
            if ((rowTypeMap.get(rowheader).contains("PERCENTAGES")&&column>0)&&percentages)
            {
                String zeroes="";
                for (int i=0;i<percentages_fpsigns;i++)
                    zeroes+="0";
                style.setDataFormat(df.getFormat("0."+zeroes+"%"));
            }
            else
            {
                if (useExcelDigitsFormatting)
                {
                    String zeroes="";
                    for (int i=0;i<fpd;i++)
                        zeroes+="0";
                    style.setDataFormat(df.getFormat("0."+zeroes));
                }
                else
                    style.setDataFormat(df.getFormat("General"));    
            }
            
            return style;
        }
        else if (rowTypeMap.get(rowheader).contains("HEADER"))
        {
            style = borderboldStyle;
            
            
            
            if ((report.getVolumeWidth()>0)&&(column%report.getVolumeWidth()==0))
                style =borderboldStyleEnd2;
            else
                style = borderboldStyle;
            if ((column)%report.getSampleWidth()==0)
                style = borderboldStyleEnd1;
            
                
            return style;
        }
        else if (rowTypeMap.get(rowheader).contains("ANSWERTEXT"))
        {
            style = borderboldStyle;
            if ((report.getVolumeWidth()>0)&&(column%report.getVolumeWidth()==0))
                style =borderboldStyleEnd2;
            else
                style = borderboldStyle;
            if ((column)%report.getSampleWidth()==0)
                style = borderboldStyleEnd1;
            
                
            return style;
        }
        else
        {
            if (isOdd)
            {
                style = borderStyleOdd;
                if ((report.getVolumeWidth()>0)&&(column%report.getVolumeWidth()==0))
                    style = borderStyleOddEnd2;
                else
                    style = borderStyleOdd;
                if ((column)%report.getSampleWidth()==0)
                    style = borderStyleOddEnd1;
                
                    
            }
            else
            {
                style = borderStyleNotOdd;
                if ((report.getVolumeWidth()>0)&&(column%report.getVolumeWidth()==0))
                    style = borderStyleNotOddEnd2;
                else
                    style = borderStyleNotOdd;
                if ((column)%report.getSampleWidth()==0)
                    style = borderStyleNotOddEnd1;
                
                    
            }
            return style;
        }
    }
    private static boolean isAnswerRow(Report report,String rowheader)
    {
        Map<String,String> rowTypeMap = report.getRowTypeMap();
        try
        {
        
            if (rowTypeMap==null)
                return false;
            else if (rowTypeMap.isEmpty())
                return false;
            else if ((rowTypeMap.get(rowheader)!=null)&&(rowTypeMap.get(rowheader).contains("ANSWERTEXT")))
                return true;
            return false;
        }
        catch(NullPointerException ex)
        {
            System.out.println(report);
            System.out.println(rowheader);
            return false;
        }
    }

    private static boolean isDARow(Report report, String rowheader) 
    {
        
        Map<String,String> rowTypeMap = report.getRowTypeMap();
        try
        {
            if ((rowTypeMap!=null)&&(!rowTypeMap.isEmpty())&&rowTypeMap.get(rowheader).contains("DA"))
            {
                return true;
            }
            return false;
        }
        catch(NullPointerException ex)
        {
            System.out.println(report);
            System.out.println(rowheader);
            return false;
        }
        
    }

    
    
    
}
