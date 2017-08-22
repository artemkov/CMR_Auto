/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmrauto;

import automatization.exceptions.*;
import automatization.model.Content;
import automatization.model.UniqueList;

import com.ibm.statistics.plugin.*;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



/**
 *
 * @author Артем Ковалев
 */
public class DataProcessor 
{
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DataProcessor.class);
    private static final int CASESPERITER = 500;
    
    public static UniqueList<Content> getContentListFromExcel(XSSFSheet sheet, PatternType patternType) throws InvalidDataFileFormatException, InvalidDataPatternType 
    {
       
        UniqueList<Content> reslist = new UniqueList<>();
        
        {
            XSSFRow row = sheet.getRow(0);
            for (int i=0; i<row.getLastCellNum();i++)
            {
                
                XSSFCell c = row.getCell(i);
                if (c==null)
                {
                    
                    //System.out.println("Cell ("+row.getRowNum()+","+i+") is null");
                    continue;
                }
                Content content = getContent(c,patternType);
                if (content!=null)
                    reslist.add(content);
                //CellRangeAddress ra = getCellRangeAddress(c);
                
                /*if (ra!=null)
                {
                    System.out.print("Cell ("+c.getRowIndex()+","+c.getColumnIndex()+") is in Range ");
                    printRA(ra);
                }
                else
                {
                    System.out.print("Cell ("+c.getRowIndex()+","+c.getColumnIndex()+") is not in any range ");
                }*/
                
                
                
                
            }
            return reslist;
        } 
    }
    public static UniqueList<Map<Content,String>> getInterviewsListFromExcel(Path pathToData, PatternType patternType) throws InvalidDataFileFormatException, InvalidDataPatternType, DataFileIOException
    {
        
        try (FileInputStream inputStream = new FileInputStream(pathToData.toFile()))
        {
            XSSFWorkbook wb = new XSSFWorkbook(new BufferedInputStream (inputStream));
            XSSFSheet sheet = wb.getSheetAt(0);
            UniqueList<Content> contentList = getContentListFromExcel(sheet, patternType);
            if (contentList!=null)
            {
                
                return getInterviewsList(sheet,contentList,patternType);
                
            }
            else
                return null;
        }
        catch (IOException ex) 
        {
            System.out.println("Something wrong with datafile");
            throw new DataFileIOException();
        }
    }
    public static UniqueList<Map<Content,String>> getInterviewsFromSPSS2 (Path pathToData) throws DataFileIOException
    {
        log.info("Считывание данных из файла "+pathToData);
        int totalRecords=0;
        UniqueList<Map<Content,String>> interviewList = new UniqueList<>();
        try
        {
            StatsUtil.submit("GET FILE='"+pathToData.toString()+"'.");
            Cursor cursor = new Cursor();
            totalRecords = cursor.getCaseCount();
            UniqueList<String> variableNames = new UniqueList<>();
            Map<String,String> variableLabels = new HashMap<>();
            Map<String,Map<Double,String>> answerMaps = new HashMap<>();
            Map<String,Integer> variableFloatValueDecimals = new HashMap<>();
            Map<String,VariableFormat> variableFormatMap = new HashMap<>();
            for (int i=0; i<cursor.getVariableCount(); i++)
            {
                String vname=cursor.getVariableName(i); 
                
                variableNames.add(vname);
                VariableFormat form = cursor.getVariableFormat(i);
                variableFormatMap.put(vname,form);
                int fwidth = cursor.getVariableFormatWidth(i);
                int decimals = cursor.getVariableFormatDecimal(i);
                variableLabels.put(vname,cursor.getVariableLabel(i));
                int typ = cursor.getVariableType(i);
                if (typ==0)
                {
                    variableFloatValueDecimals.put(vname,decimals);
                    Map<Double,String> amap = cursor.getNumericValueLabels(i);
                    if ((amap!=null)&&(!amap.isEmpty()))
                    {
                        answerMaps.put(vname, amap);
                    }
                }
            }
            cursor.close();
            
            //DataUtil datautil = new DataUtil();
            //datautil.setConvertDateTypes(true);
            //Case[] data = datautil.fetchCases(false, 0 , Math.min(totalRecords, CASESPERITER));
            Case[] data = null;
            int currentBatch = 0;
            int recordCounter = 0;
            Content content=null;
            DataUtil datautil = new DataUtil();
            do
            {
                
                
                //datautil.setConvertDateTypes(true);
                int bufferlength = Math.min(totalRecords-currentBatch, CASESPERITER);
                data = datautil.fetchCases(false, currentBatch , bufferlength);
                int adder = data==null?0:data.length;
                System.out.println("reading from "+currentBatch+" to "+(currentBatch+adder)+" ; total: "+totalRecords);
                if (data==null)
                    break;
                  
                for (Case record: data)
                {
                    Map<Content,String> interview = new HashMap<>(); 
                    for (int i=0;i<record.getCellNumber();i++)
                    {
                        String varname = variableNames.get(i);
                        /*if (varname.equalsIgnoreCase("Q4_72"))
                            System.out.println("");*/
                        CellValueFormat format = record.getCellValueFormat(i);
                        VariableFormat varformat = variableFormatMap.get(varname);
                        
                        String value="";
                        try
                        {
                            content = new Content(variableNames.get(i));
                            content.setText(variableLabels.get(variableNames.get(i)));
                            content.setType("SINGLE/OPEN");
                            //Map<String, String> answerMap = getAnswerCodeMap(answerMaps.get(variableNames.get(i)));
                            //content.setAnswerCodeMap(answerMap);
                            switch (format)
                            {
                                case DOUBLE:
                                    Double d = record.getDoubleCellValue(i);
                                    if (answerMaps.get(variableNames.get(i))!=null)
                                    {
                                        content.setAnswerCodeMap(getAnswerCodeMap(answerMaps.get(variableNames.get(i))));
                                        if (d!=null)
                                        {
                                            long v = Math.round(d);
                                            
                                            value=""+v;
                                        }
                                        
                                    }
                                    else
                                    {   
                                        value=""+record.getDoubleCellValue(i);
                                    }
                                    
                                    if ((variableFloatValueDecimals.get(variableNames.get(i))!=null)
                                                &&(variableFloatValueDecimals.get(variableNames.get(i))==0))
                                    {
                                        if (d!=null)
                                        {
                                            long v = Math.round(d);
                                            value=""+v;
                                        }
                                    }
                                
                                    break;
                                case STRING:
                                        value=record.getStringCellValue(i);
                                        break;
                                case DATE:
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                        
                                        Calendar c = record.getDateCellValue(i);
                                        if (c!=null)
                                            value=sdf.format(c.getTime());
                                        break;
                                default:
                                        System.out.println("Value: "+value);
                            }
                            interview.put(content, value);
                        }
                        catch (StatsException e)
                        {
                            log.error("Cannot read variable '"+variableNames.get(i)+"' variable number "+i+" value. Record number "+recordCounter);
                        }
                        
                    }
                    recordCounter++;
                    interviewList.add(interview);
                    //System.out.println("Размер interviewList: "+(interviewList.size()*interviewList.getFirst().size()));
                    
                }
                currentBatch+=data.length;
                //int bufferlength = Math.min(totalRecords-currentBatch, CASESPERITER);
                //data = null;
                //data = datautil.fetchCases(false, currentBatch , bufferlength);
                
            }
            while (data!=null);
            datautil.release();
            return interviewList;
        }
        catch (StatsException ex) 
        {
            log.error("Ошибка чтения данных: "+ex.getMessage());
            ex.printStackTrace();
            throw new DataFileIOException();
        }
        
    }
    
    
    public static UniqueList<Map<Content,String>> getInterviewsFromSPSS (Path pathToData) throws DataFileIOException
    {
        log.info("Считывание данных из файла "+pathToData);
        
        UniqueList<Map<Content,String>> interviewList = new UniqueList<>();
        
        try 
        {
            //List<Map<VariableContent,String>> mylist = SPSSRead.getInterviewsFromSPSS(pathToData.toAbsolutePath());
            log.info("Путь к SPSS "+StatsUtil.getStatisticsPath()+". Версия плагина "+StatsUtil.getPlugInVersion());
            //System.out.println("");
            StatsUtil.start();
            log.info("Запущен StatsUtil.start()");
            
            StatsUtil.submit("GET FILE='"+pathToData.toString()+"'.");
            //StatsUtil.submit("dataset name "+pathToData.getFileName().toString());
            
            Cursor acursor = new Cursor("r");
            log.info("Создан cursor");
            log.info("Всего записей в "+pathToData.toString()+": "+acursor.getCaseCount());
            Case[] data = acursor.fetchCases(5);
            int batchcounter = 0;
            int casecounter=0;
            while (data!=null)
            {
                batchcounter+=data.length;
                
                System.out.println("done "+batchcounter+"\\"+acursor.getCaseCount());
                for (Case record:data)
                {
                    //System.out.println(record);
                    Map<Content,String> interview = new HashMap<>(); 
                    for (int i = 0; i<record.getCellNumber();i++)
                    {
                        String name = acursor.getVariableName(i);
                        String label = acursor.getVariableLabel(i);
                        CellValueFormat format = record.getCellValueFormat(i);
                       
                        //MissingType mtype = record.getMissingType(i);
                        int typ = acursor.getVariableType(i);
                        Map<Double,String> answerMap = null;
                        
                        if (typ==0)
                        {
                            answerMap = acursor.getNumericValueLabels(name);
                        }
                        Content content = new Content(name);
                        content.setText(label);
                        content.setType("SINGLE/OPEN");
                        String value="";
                        try
                        {
                            switch (format)
                            {
                                case DOUBLE:
                                    if (answerMap!=null)
                                    {
                                        Double d = record.getDoubleCellValue(i);
                                        if (d!=null)
                                        {
                                            long v = Math.round(d);
                                            content.setAnswerCodeMap(getAnswerCodeMap(answerMap));
                                            value=""+v;
                                        }
                                    }
                                    else
                                    {
                                        value=""+record.getDoubleCellValue(i);
                                    }
                                    break;
                                    case STRING:
                                        value=record.getStringCellValue(i);
                                        break;
                                    case DATE:
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                        Calendar c = record.getDateCellValue(i);
                                        value=sdf.format(c.getTime());
                                        break;
                                    default:
                                        System.out.println("Value: "+value);
                            }
                        }
                        catch (StatsException e)
                        {
                            log.error("Cannot read variable '"+name+"' van number "+i+" value. Record number "+casecounter);
                        }
                            
                        //System.out.println("Name:"+name+" Label:"+label+" Format:"+format+" Value:"+value);
                        interview.put(content, value);
                        
                    }
                    if (!interviewList.add(interview))
                        System.out.println("НЕ ДОБАВЛЕНО "+record);
                    casecounter++;
                }
                
                data = acursor.fetchCases(5);
                
            }
            
            
            acursor.close();
            //StatsUtil.submit("new file.");
            StatsUtil.stop();
            
            log.info("Данные считаны");
            return interviewList;
        } 
        catch (Exception ex) 
        {
            log.error("Ошибка чтения данных: "+ex.getMessage());
            ex.printStackTrace();
            throw new DataFileIOException();
        }
       
        
    }
    
    
    
    private static Content getContent(XSSFCell cell,PatternType patternType  ) throws InvalidDataFileFormatException, InvalidDataPatternType
    {
        Content content = new Content();
        if (cell.getRowIndex()!=0)
            throw new InvalidDataFileFormatException();
        switch(patternType)
        {
            case Type0:
                content.setName(cell.getStringCellValue());
                content.setText(cell.getStringCellValue());
                content.setType("SINGLE/OPEN");
                content.setFirstCol(cell.getColumnIndex());
                content.setLastCol(cell.getColumnIndex());
                return content;
            case Type1:
                CellRangeAddress ra = getCellRangeAddress(cell);
                if (ra!=null)
                {
                    if (ra.getLastRow()>1)
                        throw new InvalidDataFileFormatException();
                    if ((ra.getLastColumn()-ra.getFirstColumn()>1)&(ra.getLastRow()>0))
                        throw new InvalidDataFileFormatException();
                    content.setFirstCol(ra.getFirstColumn());
                    content.setLastCol(ra.getLastColumn());
                    
                    //одно значение в ячейке шириной 1 высотой 2
                    if (content.getFirstCol()==content.getLastCol())
                    {
                        
                        if (cell.getCellType()!=XSSFCell.CELL_TYPE_STRING)
                             throw new InvalidDataFileFormatException();
                        content.setName(cell.getStringCellValue());
                        content.setText(cell.getStringCellValue());
                        content.setType("SINGLE/OPEN");
                    }
                    
                    //ячейка шириной больше 1 (Таблица, множественный ответ)
                    if (content.getLastCol()-content.getFirstCol()>0)
                    {
                        
                        if (cell.getCellType()!=XSSFCell.CELL_TYPE_STRING)
                             throw new InvalidDataFileFormatException();
                        content.setName(cell.getStringCellValue());
                        content.setText(cell.getStringCellValue());
                        content.setType("MULTIPLE/TABLE");
                        XSSFRow r = cell.getSheet().getRow(1);
                        UniqueList<Content> childrenList = new UniqueList<>();
                        for (int i=content.getFirstCol(); i<=content.getLastCol();i++)
                        {
                            Content childrencontent = new Content();
                            childrencontent.setHasparent(true);
                            XSSFCell childrencell = r.getCell(i);
                            if ((childrencell!=null)&&(childrencell.getCellType()!=XSSFCell.CELL_TYPE_STRING))
                                throw new InvalidDataFileFormatException();
                            childrencontent.setName(childrencell.getStringCellValue());
                            childrencontent.setText(childrencell.getStringCellValue());
                            childrencontent.setType("MULTIPLE ANSWER/TABLE ROW QUESTION");
                            childrenList.add(i, childrencontent);
                        }
                        content.setChildrenList(childrenList);
                        
                    }
                }
                else
                {
                    if (cell.getCellType()!=XSSFCell.CELL_TYPE_STRING)
                        throw new InvalidDataFileFormatException();
                    content.setName(cell.getStringCellValue());
                    XSSFRow r = cell.getSheet().getRow(1);
                    XSSFCell cell2 = r.getCell(cell.getColumnIndex());
                    if (cell2.getCellType()!=XSSFCell.CELL_TYPE_STRING)
                        throw new InvalidDataFileFormatException();
                    content.setText(cell2.getStringCellValue());
                    content.setFirstCol(cell.getColumnIndex());
                    content.setLastCol(cell.getColumnIndex());
                    content.setType("SINGLE/OPEN");
                    
                }
                return content;
            
        }
        throw new InvalidDataPatternType();
        
    }
    
    public static Map<Content,String> getInterview (XSSFRow row, UniqueList<Content> contentList) throws InvalidDataFileFormatException
    {
        Map<Content,String> resmap = new HashMap<>(); 
        for (Content content: contentList)
        {
            String type = content.getType();
            int firstcol = content.getFirstCol();
            int lastcol = content.getLastCol();
            if (content.getType().equals("SINGLE/OPEN"))
            {
                if (content.getName().equalsIgnoreCase("Q1_6"))
                    System.out.print("");
                XSSFCell valuecell = row.getCell(lastcol);
                String value = "";
                if(valuecell !=null)
                 if ((valuecell.getCellType()==XSSFCell.CELL_TYPE_BLANK)||(valuecell.getCellType()==XSSFCell.CELL_TYPE_ERROR))
                    value=null;
                 else if (valuecell.getCellType()==XSSFCell.CELL_TYPE_NUMERIC)
                    value=valuecell.getNumericCellValue()+"";
                 else if (valuecell.getCellType()==XSSFCell.CELL_TYPE_STRING)
                 {
                    value = valuecell.getStringCellValue();
                    if (value.matches("^\\s*(\\d+)\\s*\\.?\\s*\"(.*)\"\\s*$"))
                    {
                        Pattern pattern = Pattern.compile("^\\s*(\\d+)\\s*\\.?\\s*\"(.*)\"\\s*$");
                        Matcher matcher = pattern.matcher(value);
                        if (matcher.matches())
                        {
                            String answercode = matcher.group(1);
                            String answertext = matcher.group(2).trim();
                            
                            value = answercode;
                            if (content.getAnswerCodeMap()==null)
                                content.setAnswerCodeMap(new HashMap<>());
                            content.getAnswerCodeMap().put(answercode, answertext);
                        }
                    }
                 }
                 else
                    throw new InvalidDataFileFormatException("RowNumber "+row.getRowNum()+" ColNumber "+lastcol+" Type "+valuecell.getCellType()+" Content "+content.getName());
                
                resmap.put(content, value);
                    
            }
        }
        return resmap;
        
    }
    
    public static UniqueList<Map<Content,String>> getInterviewsList (XSSFSheet sheet,UniqueList<Content> contentList ,PatternType patternType) throws InvalidDataFileFormatException
    {
        UniqueList<Map<Content,String>> reslist = new UniqueList<>();
        int startRow = 2;
        if (patternType==PatternType.Type0)
            startRow = 1;
        else if (patternType==PatternType.Type1)
            startRow = 2;
        else if (patternType==PatternType.Type2)
            startRow = 4;
        for (int i = startRow; i<=sheet.getLastRowNum();i++)
        {
            XSSFRow row = sheet.getRow(i);
            if (row==null)
                continue;
            Map<Content,String> interviewMap = getInterview(row,contentList);
            reslist.add(interviewMap);
        }
        return reslist;
    }
    
    private static CellRangeAddress getCellRangeAddress(XSSFCell cell)
    {
        if (cell==null)
            return null;
        XSSFSheet sheet = cell.getSheet();
        for (int i=0; i<sheet.getNumMergedRegions(); i++)
        {
            CellRangeAddress  ra = sheet.getMergedRegion(i);
            if (ra.isInRange(cell.getRowIndex(), cell.getColumnIndex()))
                return ra;
        }
        return null;
    }

    private static void printRA(CellRangeAddress ra) 
    {
        int firstrow = ra.getFirstRow();
        int lastrow = ra.getLastRow();
        int firstcol = ra.getFirstColumn();
        int lastcol = ra.getLastColumn();
        
        System.out.println("From ("+firstrow+","+firstcol+") To ("+lastrow+","+lastcol+")");
    }
    
    private static Map<String,String> getAnswerCodeMap(Map<Double,String> map)
    {
        Map<String,String> resmap = (map==null&&map.size()==0)?null:new HashMap<>();
        if (resmap==null) return null;
        for (Map.Entry<Double,String> entry: map.entrySet())
        {
            Double d = entry.getKey();
            long v = Math.round(d);
            resmap.put(v+"", entry.getValue());
        }
        return resmap;
    }

    static List<UniqueList<Map<Content, String>>> getSampleList(Path dataPath, PatternType ptype) throws IOException 
    {
        FilesToSampleListConverter conv = new FilesToSampleListConverter(ptype);
        try 
        {
            Files.walkFileTree(dataPath,null,0,conv);
            List<UniqueList<Map<Content, String>>> sampleList = conv.getSampleList();
            return sampleList;
        } 
        catch (IOException ex) 
        {
            log.info("error reading "+dataPath, ex);
            throw new IOException(ex);
        }
    }
    
    static Map<String,UniqueList<Map<Content, String>>> getSampleMap(Path dataPath, PatternType ptype) throws IOException 
    {
        FilesToSampleListConverter conv = new FilesToSampleListConverter(ptype);
        try 
        {
            EnumSet<FileVisitOption> opts = EnumSet.noneOf(FileVisitOption.class);
            log.info("Запущен StatsUtil.start()");
            StatsUtil.start();
            Files.walkFileTree(dataPath,opts,1,conv);
            StatsUtil.stop();
            log.info("Остановлен StatsUtil");
            Map<String,UniqueList<Map<Content, String>>> sampleMap = conv.getSampleMap();
            return sampleMap;
        } 
        catch (IOException ex) 
        {
            log.error("error reading "+dataPath, ex);
            throw new IOException(ex);
        } 
        catch (StatsException ex) 
        {
            log.error("SPSS Error in reading data from "+dataPath, ex);
            throw new IOException(ex);
        }
    }
            
    private static class FilesToSampleListConverter extends SimpleFileVisitor<Path>
    {
        private static final String SPSSpattern = "*.sav";
        private static final String XLSXpattern = "*.{xlsx,XLSX}";
        private List<UniqueList<Map<Content, String>>> sampleList = new LinkedList<>();
        private Map<String,UniqueList<Map<Content, String>>> sampleMap = new TreeMap<>();
        private final PathMatcher SPSSmatcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + SPSSpattern);
        private final PathMatcher XLSXmatcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + XLSXpattern);
        private int numMatches = 0;
        private PatternType ptype;
        
        
        public FilesToSampleListConverter(PatternType ptype)
        {
            this.ptype = ptype;
        
        }

        public List<UniqueList<Map<Content, String>>> getSampleList() {
            return sampleList;
        }

        public Map<String, UniqueList<Map<Content, String>>> getSampleMap() {
            return sampleMap;
        }
        
        public int getNumMatches() {
            return numMatches;
        }
 
        
        
        @Override
        public FileVisitResult visitFile(Path file,
                                   BasicFileAttributes attr) 
        {
            
            
            if (attr.isSymbolicLink()) 
            {
                return CONTINUE;
            } 
            else if (attr.isRegularFile()) 
            {
                System.out.format("Regular file: %s ", file);
                Path filename = file.getFileName();
                
                try
                {        
                    //log.info("Запущен StatsUtil.start()");
                    //StatsUtil.start();
                    if ((filename!=null)&&(SPSSmatcher.matches(filename)))
                    {
                        UniqueList<Map<Content, String>> interviewList = DataProcessor.getInterviewsFromSPSS2(file);
                        //sampleList.add(interviewList);
                        sampleMap.put(filename.toString(),interviewList);
                        numMatches++;
                        return CONTINUE;
                    }
                    //StatsUtil.stop();
                    //log.info("Остановлен StatsUtil");
                }
                catch (Exception e)
                {
                    log.info("Cannot get Interview List from "+file, e);
                }
                if ((filename!=null)&&(XLSXmatcher.matches(filename)))
                {
                    
                    try
                    {
                        UniqueList<Map<Content, String>> interviewList = DataProcessor.getInterviewsListFromExcel(file, ptype);
                        //sampleList.add(numMatches,interviewList);
                        sampleMap.put(filename.toString(),interviewList);
                        
                        
                        numMatches++;
                    }
                    catch (Exception e)
                    {
                        log.info("Cannot get Interview List from "+file, e);
                    }
                    return CONTINUE;
                }
                
                return CONTINUE;
            } 
            else 
            {
                return CONTINUE;
            }
            
        }
    }
}
