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
import static automatization.model.NPSReport.getAnswerGroupsWithNPSFromExcel;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 *
 * @author Дина
 */
public class Report 
{
    public final static int DEFAULTFPDIGITS = 1;
    private int fpDIGITS = DEFAULTFPDIGITS;

    
    private int repRow = 0;
    private String mainHeader="";
    private Properties reportProperties = new Properties();
    private Content content;
    private String reportType;
    private Object reportData;
    private UniqueList<String> rowHeaders = new UniqueList<>();
    private Map<String,List<? extends Object>> reportValues = new HashMap<>();
    private Map<String,String> answerCodeMap = null;
    private Map<String,Map<String,String>> rowAnswerCodeMap = null;
    private Map<String,String> rowTypeMap = new HashMap<>();
    private Map<String,List<Color>> colorMap = new HashMap<>();
    private List<String> sampleNames = new LinkedList<>();
    private int volumeWidth = 0;
    private int sampleWidth = 1;
    
    private boolean noFirstString = false;

    public int getRepRow() {
        return repRow;
    }
    
    public void setRepRow(int repRow) {
        this.repRow=repRow;
    }
    
    public String getMainHeader() {
        return mainHeader;
    }

    public void setMainHeader(String mainHeader) {
        this.mainHeader = mainHeader;
    }

    
    public Map<String, List<Color>> getColorMap() 
    {
        return colorMap;
    }
    
    void setFpDIGITS(int fpDIGITS) 
    {
        this.fpDIGITS = fpDIGITS;
    }
    
    public Map<String,String> getRowAnswerCodeMap(String rowheader)
    {
        if (rowAnswerCodeMap==null)
            return null;
        return rowAnswerCodeMap.get(rowheader);
    }

    public List<String> getSampleNames() {
        return sampleNames;
    }

    public void setSampleNames(List<String> sampleNames) {
        this.sampleNames = sampleNames;
    }
    
    public int getSampleWidth() {
        return sampleWidth;
    }

    
    public void setVolumeWidth(int volumeWidth) {
        this.volumeWidth = volumeWidth;
    }

    public void setSampleWidth(int sampleWidth) {
        this.sampleWidth = sampleWidth;
    }
    

    public int getVolumeWidth() {
        return volumeWidth;
    }
    
    
    
    public Map<String, String> getRowTypeMap() {
        return rowTypeMap;
    }
    
    
    public boolean isNoFirstString() 
    {
        return noFirstString;
    }
    
    
    public Properties getReportProperties() 
    {
        return reportProperties;
    }

    public void setReportProperties(Properties reportProperties) {
        this.reportProperties = reportProperties;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }
    
    public String getReportType() 
    {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Object getReportData () 
    {
        return reportData;
    }

    public void setReportData(Object reportData) 
    {
        this.reportData = reportData;
    }
    
    public void addProperty(String name, String value)
    {
        reportProperties.setProperty(name, value);
    }
    
    public void addProperty(String nameandvalue)
    {
        String[] temp = nameandvalue.split("=", 2);
        if (temp.length==2)
            reportProperties.setProperty(temp[0], temp[1]);
    }
    
    public void addProperties(Properties pr)
    {
        for (String propname: pr.stringPropertyNames())
        {
            reportProperties.setProperty(propname,pr.getProperty(propname));
        }
    }
            
    public String getProperty(String name)
    {
        return reportProperties.getProperty(name);
    }
    
    public boolean hasProperty(String name)
    {
        return reportProperties.containsKey(name);
    }

    public Map<String, String> getAnswerCodeMap() {
        return answerCodeMap;
    }
    
    public void addAnswerCode(String code, String text)
    {
        if (answerCodeMap==null)
            answerCodeMap = new HashMap<>();
        answerCodeMap.put(code,text);
    }
    
    public String getTag()
    {
        if (hasProperty("tag"))
            return getProperty("tag");
        
        if (hasProperty("Tag"))
            return getProperty("Tag");
            
        if (hasProperty("TAG"))
            return getProperty("TAG");
        
        return null;
    }
    
    public String getHeader()
    {
        if (hasProperty("header"))
            return getProperty("header");
        
        if (hasProperty("Header"))
            return getProperty("Header");
            
        if (hasProperty("HEADER"))
            return getProperty("HEADER");
        
        return null;
    }
    public int getFPDIGITS()
    {
        return fpDIGITS;
    }
    
    
    public int getFPDIGITSFromProperties()
    {
        if (reportProperties==null)
            return DEFAULTFPDIGITS;
        
        if (hasProperty("fpdigits"))
            return ReportUtils.getIntFromProperties("fpdigits", DEFAULTFPDIGITS, reportProperties);
        
        if (hasProperty("FPDIGITS"))
            return ReportUtils.getIntFromProperties("FPDIGITS", DEFAULTFPDIGITS, reportProperties);
        
        return DEFAULTFPDIGITS;
            
    }
    
    public String getMainHeaderFromProperties()
    {
        if (hasProperty("mainheader"))
            return getProperty("mainheader");
        
        if (hasProperty("MainHeader"))
            return getProperty("MainHeader");
            
        if (hasProperty("MAINHEADER"))
            return getProperty("MAINHEADER");
        
        return null;
    }

    public LinkedList<String> getRowHeaders() {
        return rowHeaders;
    }
    
    public void addRowHeader(String header)
    {
        rowHeaders.add(header);
    }

    public Map<String, List<? extends Object>> getReportValues() {
        return reportValues;
    }
    /**
     * Creates MultiSampled report data
     * @param level3node
     * @param content
     * @param sampleList
     * @return
     * @throws InvalidTemplateFileFormatException
     * @throws ReportParamsNotDefinedException
     * @throws ReportFileNotFoundException
     * @throws VariableNotFoundException
     * @throws GroupsFileNotFoundException
     * @throws InvalidFilterException
     * @throws IOException
     * @throws InvalidGroupsFileFormatException 
     */
    public Report getMultiSampleReportFromNode(TemplateNode<String> level3node, Content content, List<UniqueList<Map<Content,String>>> sampleList, List<String> sampleNames) throws InvalidTemplateFileFormatException, ReportParamsNotDefinedException, ReportFileNotFoundException, VariableNotFoundException, GroupsFileNotFoundException, InvalidFilterException, IOException, InvalidGroupsFileFormatException, NoSampleDataException
    {
        if (level3node==null)
        {
            throw new InvalidTemplateFileFormatException();
        }
        //тип
        this.setReportType(level3node.getData());
        this.repRow=level3node.getExcelRow();
        //свойства
        List<TemplateNode<String>> level4nodes = level3node.getChildren();
        //заголовки сэмплов
        this.sampleNames=sampleNames;
        
        
        if (level4nodes!=null)
        {
            level4nodes.stream().forEach((TemplateNode<String> paramnode) -> 
            {
                Report.this.addProperty(paramnode.getData());
            });
        }
        
               
        //добавление корневых свойств
        Properties rootProperties = level3node.getParams();
        this.reportProperties = ReportUtils.mergeProperties(rootProperties, reportProperties);
        
        if (hasProperty("drawtotal"))
        {
            if (getProperty("drawtotal").equalsIgnoreCase("false"))
            {
                noFirstString = true;
            }
            else
            {
                noFirstString = false;
            }
        }
        
        //Округление действительных (цифры после запятой)
        fpDIGITS = getFPDIGITSFromProperties();

        //Разделение сэмплов по переменной SplitBySample
        List<UniqueList<Map<Content,String>>> splittedSamples = new ArrayList<>();
        List<String> splittedNames = new ArrayList<>();
        if (hasProperty("SplitSamplesBy"))
        {
            String splitVarName = getProperty("SplitSamplesBy");
            splittedSamples = splitSamplesBy(splitVarName, sampleList, sampleNames,splittedNames);
            this.sampleNames=splittedNames;
        }
        else
        {
            splittedSamples=sampleList;
            splittedNames=sampleNames;
        }
        
        
        //Главный заголовок
        setMainHeader(getMainHeaderFromProperties());
        
        
        
        switch (reportType)
        {
            case "Text1":
                this.getReportProperties().setProperty("drawsample","false");
                String textdata = getProperty("text");
                if (textdata==null) textdata="";
                rowHeaders.add(textdata);
                rowTypeMap.put(textdata, "TEXT1");
                break;
            case "Text2":
                this.getReportProperties().setProperty("drawsample","false");
                textdata = getProperty("text");
                if (textdata==null) textdata="";
                rowHeaders.add(textdata);
                rowTypeMap.put(textdata, "TEXT2");
                break;
            case "Text3":
                this.getReportProperties().setProperty("drawsample","false");
                textdata = getProperty("text");
                if (textdata==null) textdata="";
                rowHeaders.add(textdata);
                rowTypeMap.put(textdata, "TEXT3");
                break;
            case "Text":
                this.getReportProperties().setProperty("drawsample","false");
                textdata = getProperty("text");
                if (textdata==null) textdata="";
                rowHeaders.add(textdata);
                rowTypeMap.put(textdata, "TEXT");
                break;
            case "Linear":
                
                //ReportFactory rf = new LinearReportFactory();
                //Report report = rf.makeReport(content, reportProperties, sampleList, sampleNames);
                
                String weightcontentname = reportProperties.getProperty("weightcontent");
                List<LinearReport> replist = new LinkedList<>();
                for (UniqueList<Map<Content,String>> sample : splittedSamples)
                {
                    Content wcon = ContentUtils.getContentByNameFromInterviewList(sample, weightcontentname);
                    LinearReport lr = new LinearReport(sample,content,wcon);
                    replist.add(lr);
                }
                
                //данные отчета
                //первая строка (всего)
                LinearReport oldlinr = null; 
                for (LinearReport lr: replist)
                {
                    Double total = lr.getTotal();
                    //для первого очета рисуем нулевой столбец
                    String totalrowheader = getHeader()==null?"Линейное распределение ответов":getHeader();
                    if (replist.indexOf(lr)==0)
                    {
                        rowHeaders.add(totalrowheader);
                        rowTypeMap.put(totalrowheader, "HEADER");
                    }
                    Double val1 = total;
                    List<Number> list = new LinkedList<>();
                    if (!hasProperty("HeaderPercentageOnly")||getProperty("HeaderPercentageOnly").equalsIgnoreCase("false"))
                        list.add(round(val1));
                    if (!hasProperty("HeaderTotalOnly")||getProperty("HeaderTotalOnly").equalsIgnoreCase("false"))
                        list.add(100);
                   
                    sampleWidth=list.size();
                    this.addToList(list, totalrowheader);
                    
                    for (String answer: lr.getUniqvalues())
                    {
                        
                        if ((content.getAnswerCodeMap()!=null)&&(content.getAnswerCodeMap().size()!=0))
                            if ((hasProperty("ShowAnswersText"))&&(getProperty("ShowAnswersText").equalsIgnoreCase("true")))
                                this.answerCodeMap=content.getAnswerCodeMap();
                        rowHeaders.add(answer);
                        rowTypeMap.put(answer, "VALUE,DA");
                        Double count = round(lr.getStatmap().get(answer));
                        Double percent = round(count*100.0/total);
                        if (oldlinr!=null)
                        {
                            double oldv = oldlinr.getTotal()!=0?oldlinr.getStatmap().getOrDefault(answer, 0.0)*100.0/oldlinr.getTotal():0.0;
                            double oldlrpercent = round(oldv);
                            Double davalue = ReportUtils.getNormDAVal(oldlrpercent, percent, oldlinr.getTotal(), lr.getTotal());
                            Color color = ReportUtils.getColorFromDiff(davalue);
                            addToColorMap(answer,color);
                        }
                        else
                        {
                            addToColorMap(answer,Color.BLACK);
                        }
                            
                        list = new LinkedList<>();
                        if (!hasProperty("DataPercentageOnly"))
                            list.add(count);
                        else if (getProperty("DataPercentageOnly").equalsIgnoreCase("false"))
                            list.add(count);
                        if (!hasProperty("DataCountOnly"))
                            list.add(percent);
                        else if (getProperty("DataCountOnly").equalsIgnoreCase("false"))
                            list.add(percent);
                        this.addToList(list, answer);
                    }
                    
                    
                    
                    //сортировка RowHeaders
                    List<String> rhval = rowHeaders.subList(1, rowHeaders.size());
                    rhval.sort(new StringIntComparator());
                    
                    oldlinr=lr;
                    
                }
                //Дописываем нули
                int maxSize = sampleWidth*sampleList.size();
                for (String rowheader: rowHeaders)
                {
                        List<? extends Object> vals = reportValues.get(rowheader);
                        if (vals.size()<maxSize)
                        {
                            List<Number> zerolist = new ArrayList<>();
                            for (int i = vals.size(); i<maxSize; i++)
                            {
                                zerolist.add(0);
                            }
                            addToList(zerolist, rowheader);
                            //System.out.println(rowheader+": "+reportValues.get(rowheader));
                        }
                }
                break;
            case "Regrouped Linear":
                if (!hasProperty("groupfile"))
                    throw new ReportParamsNotDefinedException();
                //Определяем Excel файл с настройками группы из ячеек параметров
                Path groupfilepath = Paths.get(getProperty("groupfile"));
                if (!Files.isReadable(groupfilepath))
                    throw new GroupsFileNotFoundException(groupfilepath.toAbsolutePath().toString());
                List<AnswerGroup> answerGroupsFromExcel;
                try 
                {
                    answerGroupsFromExcel = GroupsReport.getAnswerGroupsFromExcel(groupfilepath);
                } 
                catch (IOException ex) 
                {
                    throw new ReportFileNotFoundException();
                }
                List<GroupsReport> grlist = new LinkedList<>();
                for (UniqueList<Map<Content,String>> sample: sampleList)
                {
                    GroupsReport gr = new GroupsReport(answerGroupsFromExcel);
                    gr.populateGroups(sample, content.getName());
                    grlist.add(gr);
                }
                //для первого очета рисуем нулевой столбец
                String totalrowheader = getHeader()==null?"Перегруппированное распределение ответов":getHeader();
                rowHeaders.add(totalrowheader);
                rowTypeMap.put(totalrowheader, "HEADER");
                
                for (GroupsReport gr: grlist)
                {
                    Integer val1 = gr.getIntGroupedTotal();
                    List<Number> list = new LinkedList<>();
                    if (!hasProperty("HeaderPercentageOnly"))
                        list.add(val1);
                    else if (getProperty("HeaderPercentageOnly").equalsIgnoreCase("false"))
                        list.add(val1);
                    if (!hasProperty("HeaderTotalOnly"))
                        list.add(100);
                    else if (getProperty("HeaderTotalOnly").equalsIgnoreCase("false"))
                        list.add(100);
                    sampleWidth=list.size();
                    
                    this.addToList(list, totalrowheader);
                    for(AnswerGroup ag: gr.getGroups())
                    {
                        String gname = ag.getName();
                        rowHeaders.add(gname);
                        rowTypeMap.put(gname,"VALUE");
                        Double count = gr.getValuecountermap().get(ag);
                        Double percent = 0.0;
                        if (count==null)
                        {
                            count = 0.0;
                        }
                        else   
                        {
                            double temp = count*10000/gr.getIntGroupedTotal();
                            percent = temp/100.0;
                        }
                        
                        list = new LinkedList<>();
                        if (!hasProperty("DataPercentageOnly"))
                            list.add(count);
                        else if (getProperty("DataPercentageOnly").equalsIgnoreCase("false"))
                            list.add(count);
                        if (!hasProperty("DataCountOnly"))
                            list.add(percent);
                        else if (getProperty("DataCountOnly").equalsIgnoreCase("false"))
                            list.add(percent);
                        this.addToList(list, gname);
                    }
                }
                break;
            case "NPS":
                if (!hasProperty("groupfile"))
                    throw new ReportParamsNotDefinedException();
                //Определяем Excel файл с настройками группы из ячеек параметров
                groupfilepath = Paths.get(getProperty("groupfile"));
                if (!Files.isReadable(groupfilepath))
                    throw new GroupsFileNotFoundException(groupfilepath.toAbsolutePath().toString());
                /*try 
                {
                    answerGroupsFromExcel = GroupsReport.getAnswerGroupsFromExcel(groupfilepath);
                } 
                catch (IOException ex) 
                {
                    throw new ReportFileNotFoundException();
                }*/
                List<NPSReport> npsreplist = new LinkedList<>();
                
                for (UniqueList<Map<Content,String>> sample: sampleList)
                {
                    NPSReport npsreport = getAnswerGroupsWithNPSFromExcel(groupfilepath);
                    npsreport.computeNPS(sample, content.getName());
                    npsreplist.add(npsreport);
                }
                //для первого очета рисуем нулевой столбец
                totalrowheader = getHeader()==null?"Отчет NPS":getHeader();
                rowHeaders.add(totalrowheader);
                rowTypeMap.put(totalrowheader, "HEADER");
                for (NPSReport npsr: npsreplist)
                {
                    Double nps = npsr.getNps();
                    int total = npsr.getIntGroupedTotal();
                    List<Number> list = new LinkedList<>();
                    if ((!hasProperty("HeaderPercentageOnly"))||((hasProperty("HeaderPercentageOnly"))&&(getProperty("HeaderPercentageOnly").equalsIgnoreCase("true"))))
                        list.add(total);
                    if ((!hasProperty("HeaderTotalOnly"))||((hasProperty("HeaderTotalOnly"))&&(getProperty("HeaderTotalOnly").equalsIgnoreCase("true"))))
                        list.add(100);
                    sampleWidth=list.size();
                    this.addToList(list, totalrowheader);
                    
                    
                    list = new LinkedList<>();
                    if (!hasProperty("DataPercentageOnly"))
                        list.add(npsr.getTops());
                    else if (getProperty("DataPercentageOnly").equalsIgnoreCase("false"))
                        list.add(npsr.getTops());
                    if (!hasProperty("DataCountOnly"))
                        list.add(npsr.getTops()*10000/total/100.0);
                    else if (getProperty("DataCountOnly").equalsIgnoreCase("false"))
                        list.add(npsr.getTops()*10000/total/100.0);
                    this.addToList(list, "Top");
                    rowTypeMap.put("Top","VALUE");
                    
                    list = new LinkedList<>();
                    if (!hasProperty("DataPercentageOnly"))
                        list.add(npsr.getPassives());
                    else if (getProperty("DataPercentageOnly").equalsIgnoreCase("false"))
                        list.add(npsr.getPassives());
                    if (!hasProperty("DataCountOnly"))
                        list.add(npsr.getPassives()*10000/total/100.0);
                    else if (getProperty("DataCountOnly").equalsIgnoreCase("false"))
                        list.add(npsr.getPassives()*10000/total/100.0);
                    this.addToList(list, "Passive");
                    rowTypeMap.put("Passive","VALUE");
                    
                    list = new LinkedList<>();
                    if (!hasProperty("DataPercentageOnly"))
                        list.add(npsr.getBottoms());
                    else if (getProperty("DataPercentageOnly").equalsIgnoreCase("false"))
                        list.add(npsr.getBottoms());
                    if (!hasProperty("DataCountOnly"))
                        list.add(npsr.getBottoms()*10000/total/100.0);
                    else if (getProperty("DataCountOnly").equalsIgnoreCase("false"))
                        list.add(npsr.getBottoms()*10000/total/100.0);
                    this.addToList(list, "Bottom");
                    rowTypeMap.put("Bottom","VALUE");
                    
                    list = new LinkedList<>();
                    if (!hasProperty("DataPercentageOnly"))
                        list.add(npsr.getTops() - npsr.getBottoms());
                    else if (getProperty("DataPercentageOnly").equalsIgnoreCase("false"))
                        list.add(npsr.getTops() - npsr.getBottoms());
                    if (!hasProperty("DataCountOnly"))
                        list.add(nps);
                    else if (getProperty("DataCountOnly").equalsIgnoreCase("false"))
                        list.add(nps);
                    this.addToList(list, "NPS");
                    rowTypeMap.put("NPS","VALUE");
                }
                break;
            
                
                
                
                
            case "Cross":
                String secondvar;
                if (hasProperty("secondvar"))
                    secondvar = getProperty("secondvar");
                else
                    throw new ReportParamsNotDefinedException();
                
                if ((hasProperty("ShowAnswersText"))&&(getProperty("ShowAnswersText").equalsIgnoreCase("true")))
                {
                    if ((content.getAnswerCodeMap()!=null)&&(!content.getAnswerCodeMap().isEmpty()))
                    {
                        this.answerCodeMap=content.getAnswerCodeMap();
                    }
                    
                    Content content2 = ContentUtils.getContentByNameFromInterviewList(sampleList.get(0), secondvar);
                    if (content2==null)
                        throw new VariableNotFoundException(secondvar);
                    
                    
                    
                       
                    if ((content2.getAnswerCodeMap()!=null)&&(!content2.getAnswerCodeMap().isEmpty()))
                    {
                        Map<String,String> amap = content2.getAnswerCodeMap();
                        rowAnswerCodeMap = new HashMap<>();
                        rowAnswerCodeMap.put(content.getName()+"\\"+secondvar, amap);
                        
                    }
                    
                }
                UniqueList<String> answerslist1=new UniqueList<>();
                UniqueList<String> answerslist2=new UniqueList<>();
                for (int i=0; i<sampleList.size();i++)
                {
                    UniqueList<Map<Content, String>> interviewList = sampleList.get(i);
                    CrossReport crossreport = new CrossReport(interviewList, content.getName(), secondvar);
                    List<String> answerstoadd2= Arrays.asList(crossreport.getAllpossvalues2());
                    answerslist2.addAll(answerstoadd2);
                    
                    List<String> answerstoadd1= Arrays.asList(crossreport.getAllpossvalues1());
                    answerslist1.addAll(answerstoadd1);
                    
                    
                }
                answerslist1.sort(new StringIntComparator());
                answerslist2.sort(new StringIntComparator());
                for (int i=0; i<sampleList.size();i++)
                {
                    UniqueList<Map<Content, String>> interviewList = sampleList.get(i);
                    CrossReport crossreport = new CrossReport(interviewList, content.getName(), secondvar);
                    
                    if (i==0)
                    {
                        rowHeaders.add(content.getName()+"\\"+secondvar);
                        rowTypeMap.put(content.getName()+"\\"+secondvar,"HEADER");
                        
                        rowHeaders.addAll(answerslist1);
                        for (int j=0;j<answerslist1.size();j++)
                            rowTypeMap.put(answerslist1.get(j),"VALUE");
                        sampleWidth=answerslist2.size();
                    }
                    addStrToList(answerslist2, content.getName()+"\\"+secondvar);
                    
                    Integer[][] crossreportvalues = crossreport.getData();
                    Integer[][] finalreportvalues = new Integer[answerslist1.size()][answerslist2.size()];
                    
                    String[] answers1 = crossreport.getAllpossvalues1();
                    String[] answers2 = crossreport.getAllpossvalues2();
                    
                    for (int r = 0; r<finalreportvalues.length;r++)
                    {
                        fillRow(finalreportvalues,r,0);
                    }
                    
                    int a1index=0,a2index=0;
                    for (String a1: answerslist1)
                    {
                        int crrepindex1 = Arrays.binarySearch(answers1, a1);
                        if (crrepindex1<0)
                        {
                            
                            a1index++;
                            continue;
                        }
                        a2index=0;
                        for (String a2: answerslist2)
                        {
                            int crrepindex2 = Arrays.binarySearch(answers2, a2);
                            if (crrepindex2<0)
                            {
                                
                                a2index++;
                                continue;
                            }
                            Integer val = crossreportvalues[crrepindex1][crrepindex2];
                            finalreportvalues[a1index][a2index]=val;
                            a2index++;
                        }
                        a1index++;
                    }
                    
                    
                    for (int rn=0; rn<finalreportvalues.length; rn++)
                    {
                        List<Number> rowValues = Arrays.asList(finalreportvalues[rn]);
                        addToList(rowValues,answerslist1.get(rn));
                    }
                }
                break;
            case "Regrouped Cross":    
                answerslist2=null; 
                GroupsCrossReport gcr=null, oldgcr=null;
                List<GroupsCrossReport> gcrlist = new ArrayList<>();
                UniqueList<String> headers1 = new UniqueList<>();
                UniqueList<String> headers2 = new UniqueList<>();
                List<List<String>> crossgroups1 = new ArrayList<>();
                List<List<String>> crossgroups2 = new ArrayList<>();
                String baseheader = "База";
                for (int i=0;i<sampleList.size();i++)
                {
                    
                    gcr = new GroupsCrossReport(sampleList.get(i),content,reportProperties,level3node.findRootNode());
                    gcrlist.add(gcr);
                    List<String> grlist1 = new ArrayList<>();
                    List<String> grlist2 = new ArrayList<>();
                    for (InterviewGroup ag1: gcr.getAgroup1())
                    {
                        headers1.add(ag1.getName());
                        grlist1.add(ag1.getName());
                    }
                    crossgroups1.add(grlist1);
                    for (InterviewGroup ag2: gcr.getAgroup2())
                    {
                        headers2.add(ag2.getName());
                        grlist2.add(ag2.getName());
                    }
                    crossgroups2.add(grlist2);
                    /*if (gcr.isAgrop1fictive())
                        headers1.sort(new StringIntComparator());
                    if (gcr.isAgrop2fictive())
                        headers2.sort(new StringIntComparator());*/
                    
                }
                totalrowheader="Групповое кросс-распределение";
                for (int i=0;i<sampleList.size();i++)
                {
                    oldgcr=gcr;
                    gcr = gcrlist.get(i);
                    if (i==0)
                    {
                        oldgcr=null;
                        totalrowheader = getHeader()==null?content.getName()+"\\"+gcr.getContent2().getName():getHeader();
                        rowHeaders.add(totalrowheader);
                        rowTypeMap.put(totalrowheader,"HEADER;ANSWERTEXT");
                        
                        
                        //база
                        if (hasProperty("ShowBase")&&getProperty("ShowBase").equalsIgnoreCase("true"))
                        {
                            
                            if (hasProperty("BaseHeader"))
                                baseheader = getProperty("BaseHeader");
                            rowHeaders.add(baseheader);
                            rowTypeMap.put(baseheader,"HEADER");
                        }
                        
                        //Заголовки переменной 1 (строки)
                        for (String rowheader: headers1)
                        {
                            rowHeaders.add(rowheader);
                            rowTypeMap.put(rowheader,"VALUE,DA");
                            colorMap.put(rowheader, new ArrayList<Color>());
                        }
                        //Карты текста ответов
                        if ((hasProperty("ShowAnswersText"))&&(getProperty("ShowAnswersText").equalsIgnoreCase("true")))
                        {
                            if ((content.getAnswerCodeMap()!=null)&&(!content.getAnswerCodeMap().isEmpty()))
                            {
                                if (gcr.isAgrop1fictive())
                                {
                                    this.answerCodeMap=content.getAnswerCodeMap();
                                    
                                }
                            }
                            if ((gcr.getContent2()!=null)&&(gcr.getContent2().getAnswerCodeMap()!=null)&&(!gcr.getContent2().getAnswerCodeMap().isEmpty()))
                            {
                                if (gcr.isAgrop2fictive())
                                {
                                    Map<String,String> amap = gcr.getContent2().getAnswerCodeMap();
                                    rowAnswerCodeMap = new HashMap<>();
                                    rowAnswerCodeMap.put(totalrowheader, amap);
                                }
                            }
                        }
                        
                        answerslist2 = headers2;
                        sampleWidth= answerslist2.size();
                    }
                    addStrToList(answerslist2, totalrowheader);
                    
                    if (hasProperty("ShowBase")&&getProperty("ShowBase").equalsIgnoreCase("true"))
                    {
                        List<Number> baselist = 
                                Collections.nCopies(answerslist2.size(), round(ContentUtils.countWeights(gcr.getWeightContent(), sampleList.get(i))));
                        addToList(baselist, baseheader);
                    }
                    
                    double basesize=ContentUtils.countWeights(gcr.getWeightContent(), sampleList.get(i)),oldbasesize=0.0;
                    if (oldgcr!=null)
                    {
                        oldbasesize = ContentUtils.countWeights(oldgcr.getWeightContent(), sampleList.get(i));
                    }
                    Double[][]data = new Double[headers1.size()][headers2.size()];
                    Double[][]olddata = new Double[headers1.size()][headers2.size()];
                    
                    int indag1=0;
                    for (String header1: headers1)
                    {
                        int header1index = crossgroups1.get(i).indexOf(header1);
                        int indag2=0;
                        for (String header2: headers2)
                        {
                            int header2index = crossgroups2.get(i).indexOf(header2);
                            if ((header1index!=-1)&&(header2index!=-1))
                            {
                                data[indag1][indag2]=round(gcr.getData()[header1index][header2index]);
                            }
                            else
                            {
                                data[indag1][indag2]=0.0;
                                
                            }
                            //Цвет значимости
                            Double diff = null;
                            if (oldgcr!=null)
                            {
                                Double oldd = oldgcr.getData()[header1index][header2index];
                                Double d = gcr.getData()[header1index][header2index];
                                diff = ReportUtils.getNormDAVal(oldd, d, oldbasesize, basesize);
                                colorMap.get(header1).add(ReportUtils.getColorFromDiff(diff));
                            }
                            else
                                colorMap.get(header1).add(Color.BLACK);
                            indag2++;
                        }
                        addToList(Arrays.asList(data[indag1]), header1);
                        indag1++;
                    }
                }
                break;
            case"Mean":
                List<String> excludeList = null;
                List<Integer> meanheaderlist = new LinkedList<>();
                List<Number> meanlist = new LinkedList<>();
                totalrowheader="Выборка";
                if (hasProperty("ExcludeList"))
                {
                    String excludeListStr = getProperty("ExcludeList");
                    excludeList = Arrays.asList(excludeListStr.split("[,;]"));
                }
                for (int i =0; i<sampleList.size();i++)
                {
                    UniqueList<Map<Content, String>> interviewList = sampleList.get(i);
                    MeanReport meanreport = new MeanReport(interviewList, content.getName(), excludeList);
                    if (i==0)
                    {
                        totalrowheader = getHeader()==null?"Выборка":getHeader();
                        rowHeaders.add(totalrowheader);
                        rowTypeMap.put(totalrowheader,"HEADER");
                        rowHeaders.add("Среднее");
                        rowTypeMap.put("Среднее","VALUE");
                        sampleWidth=1;
                    }
                    meanheaderlist.add(meanreport.sampleSize);
                    double templ =  round(meanreport.mean);
                    meanlist.add(templ);
                }
                reportValues.put(totalrowheader,meanheaderlist);
                reportValues.put("Среднее",meanlist);
                break;
            case "ArithmeticMean":
                List<UniqueList<Map<Content,String>>> mfsamples = splittedSamples;
                List<String> mfnames = splittedNames;
                if (hasProperty("drawtotal"))
                {
                    if (getProperty("drawtotal").equalsIgnoreCase("false"))
                    {
                        noFirstString = true;
                    }
                    else
                    {
                        noFirstString = false;
                    }
                }
                for (int i=0;i<mfsamples.size();i++)
                {
                    sampleWidth=1;
                    UniqueList<Map<Content,String>> interviews = mfsamples.get(i);
                    ArithmeticMeanReport amr = new ArithmeticMeanReport(interviews, content, reportProperties, level3node);
                    List<String>rnlist=amr.rownamesList;
                    totalrowheader = getHeader()==null?"Размер базы":getHeader();
                    if (i==0)
                    {
                        
                        rowHeaders.add(totalrowheader);
                        rowTypeMap.put(totalrowheader,"HEADER");
                        
                        for (String name: rnlist)
                        {
                            rowHeaders.add(name);
                            rowTypeMap.put(name,"VALUE");
                        }
                    }
                    List<Number> baselist = new ArrayList<>();
                    baselist.add(round(amr.countSampleWeight(interviews)));
                    addToList(baselist, totalrowheader);
                    
                    for (int ii=0; ii<rnlist.size();ii++)
                    {
                        String name = rnlist.get(ii);
                        double val = amr.meanList.get(ii);
                        List<Number> valList = new ArrayList<>();
                        valList.add(round(val));
                        addToList(valList, name);
                    }
                    
                }
                break;
            case "MergedFrequency":
                mfsamples = splittedSamples;
                mfnames = splittedNames;
                MergedFrequencyReport mfreport = null, oldmfreport = null;
                if (hasProperty("drawtotal"))
                {
                    if (getProperty("drawtotal").equalsIgnoreCase("false"))
                    {
                        noFirstString = true;
                    }
                    else
                    {
                        noFirstString = false;
                    }
                }
                for (int i=0;i<mfsamples.size();i++)
                {
                    sampleWidth=1;
                    UniqueList<Map<Content,String>> interviews = mfsamples.get(i);
                    mfreport = new MergedFrequencyReport(interviews, content, reportProperties, level3node);
                    if (i>0)
                    {
                        oldmfreport = new MergedFrequencyReport(mfsamples.get(i-1), content, reportProperties, level3node);
                    }
                    List<Double> flist = mfreport.getFrequencies();
                    List<String> rnlist = mfreport.getRowNames();
                    List<Double> countslist = mfreport.getCounts();
                    Map<String,String> damap = mfreport.getDAStringsMap(this.getReportProperties());
                    totalrowheader = getHeader()==null?"Размер базы":getHeader();
                    if (i==0)
                    {
                        
                        rowHeaders.add(totalrowheader);
                        rowTypeMap.put(totalrowheader,"HEADER");
                        
                        for (String name: rnlist)
                        {
                            String name1=name;
                            if (damap.containsKey(name))
                                name1 = mfreport.getIndexByName(name)+". "+name;
                            rowHeaders.add(name1);
                            
                            rowTypeMap.put(name1,"VALUE, DA");
                            colorMap.put(name1, new ArrayList<Color>());
                        }
                        
                        
                    }
                    List<Number> baselist = new ArrayList<>();
                    double basesize = ContentUtils.countWeights(mfreport.getWeightContentName(), interviews);
                    baselist.add(ReportUtils.round(basesize, fpDIGITS));
                    addToList(baselist, totalrowheader);
                    
                    for (int ind = 0; ind<mfreport.getRowNames().size(); ind++)
                    {
                        
                        String name = rnlist.get(ind);
                        String reportname = damap.containsKey(name)?mfreport.getIndexByName(name)+". "+name:name;
                        Double value=0.0, freq=0.0;
                        double oldbasesize = 0.0;
                        Double oldfreq = 0.0;
                        
                        
                        List<Number> vallist = new ArrayList<>();
                        if ((this.hasProperty("showcounts")&& !this.getProperty("showcounts").equalsIgnoreCase("true"))||(!this.hasProperty("showcounts")))
                        {
                            if (oldmfreport!=null)
                        
                            {
                            
                                oldfreq = oldmfreport.getFrequencies().get(ind);
                            
                                oldbasesize = ContentUtils.countWeights(oldmfreport.getWeightContentName(), mfsamples.get(i-1));
                        
                            }
                        
                            freq = flist.get(ind);
                            vallist.add(round(freq));
                            Double diff = i==0?null:ReportUtils.getNormDAVal(oldfreq, freq, oldbasesize, basesize);
                            
                            colorMap.get(reportname).add(ReportUtils.getColorFromDiff(diff))  ;  
                            
                        }
                        if (this.hasProperty("showcounts"))
                        {
                            if (this.getProperty("showcounts").equalsIgnoreCase("true"))
                            {
                                value=countslist.get(ind);
                                vallist.add(round(value));
                                colorMap.get(reportname).add(Color.BLACK);
                            }
                        }
                        addToList(vallist, reportname);
                        
                        List<String> dastringlist = new ArrayList<>();
                        if ((!damap.isEmpty())&&(damap.containsKey(name)))
                        {
                            
                            String string = damap.getOrDefault(name, null);
                            
                            if (i==0)
                            {
                                rowHeaders.add("Значимости "+name);
                                rowTypeMap.put("Значимости "+name,"VALUE");
                            }
                            dastringlist.add(string);
                            addStrToList(dastringlist, "Значимости "+name);
                        }
                    }
                    
                }
                break;
                
            case "ComplexFrequency":
                List<UniqueList<Map<Content,String>>> cfsamples = sampleList;
                List<String> cfnames = new ArrayList<>();
                ComplexFrequencyReport cfreport = null, oldcfreport = null;
                if (hasProperty("SplitSamplesBy"))
                {
                    String splitVarName = getProperty("SplitSamplesBy");
                    
                    List<UniqueList<Map<Content,String>>> newSamples = splitSamplesBy(splitVarName, sampleList, sampleNames,cfnames);
                    cfsamples = newSamples;
                    this.sampleNames=cfnames;
                }
                if (hasProperty("drawtotal"))
                {
                    if (getProperty("drawtotal").equalsIgnoreCase("false"))
                    {
                        noFirstString = true;
                    }
                    else
                    {
                        noFirstString = false;
                    }
                }
                for (int i=0;i<cfsamples.size();i++)
                {
                    sampleWidth=1;
                    
                    UniqueList<Map<Content,String>> oldinterviews = null;
                    if (i>0)
                    {
                        oldinterviews = cfsamples.get(i-1);
                    }
                    UniqueList<Map<Content,String>> interviews = cfsamples.get(i);
                    
                    if (i>0)
                    {
                        oldcfreport = cfreport;
                    }
                    
                    cfreport = new ComplexFrequencyReport(interviews, content, reportProperties, level3node);
                    List<Double> flist = cfreport.getFrequencies();
                    List<String> rnlist = cfreport.getGroupNames();
                    List<Double> countslist = cfreport.getCounts();
                    totalrowheader = getHeader()==null?"Размер базы":getHeader();
                    if (i==0)
                    {
                        
                        rowHeaders.add(totalrowheader);
                        rowTypeMap.put(totalrowheader,"HEADER");
                        
                        for (String name: cfreport.getGroupNames())
                        {
                            rowHeaders.add(name);
                            rowTypeMap.put(name,"VALUE, DA");
                            colorMap.put(name, new ArrayList<Color>());
                            
                        }
                    }
                    List<Number> baselist = new ArrayList<>();
                    double basesize = ContentUtils.countWeights(cfreport.getWeightContentName(), interviews);
                    baselist.add(round(basesize));
                    addToList(baselist, totalrowheader);
                    for (int ind = 0; ind<cfreport.getGroupNames().size(); ind++)
                    {
                        String name = rnlist.get(ind);
                        Double value=0.0, freq=0.0, oldfreq = 0.0, oldbasesize = 0.0;
                        
                        List<Number> vallist = new ArrayList<>();
                        
                        if ((this.hasProperty("showcounts")&&this.getProperty("showcounts").equalsIgnoreCase("true"))||(!this.hasProperty("showcounts")))
                        {
                            if (oldcfreport!=null)
                        
                            {
                            
                                oldfreq = oldcfreport.getFrequencies().get(ind);
                            
                                oldbasesize = ContentUtils.countWeights(oldcfreport.getWeightContentName(), oldinterviews);
                        
                            }
                            
                            freq = flist.get(ind);
                            vallist.add(round(freq));
                            Double diff = i==0?null:ReportUtils.getNormDAVal(oldfreq, freq, oldbasesize, basesize);
                                
                            if ((diff==null)||(diff==0.0))
                            {
                                colorMap.get(name).add(Color.BLACK);
                            }
                            else if (diff<0.0)
                            {
                                colorMap.get(name).add(Color.BLUE);
                            }
                            else 
                            {
                                colorMap.get(name).add(Color.RED);
                            }
                        }
                        if (this.hasProperty("showcounts"))
                        {
                            if (this.getProperty("showcounts").equalsIgnoreCase("true"))
                            {
                                value=countslist.get(ind);
                                vallist.add(round(value));
                                colorMap.get(name).add(Color.BLACK);
                            }
                        }
                        addToList(vallist, name);
                        //update values with Internal DA Data
                        
                    }
                }
                break;
            case "OlgaLinear":
                List<UniqueList<Map<Content,String>>> samples=splittedSamples;
                OlgaLinearReport olr = null,
                previosolr = null;
                List<OlgaLinearReport> olrlist = new ArrayList<>();
                
                List<String> linearRepGroupNamesList = new UniqueList<>();
                String volumeheader = null;
                totalrowheader = null;
                
                //Предварительное определение групп
                for (int i=0;i<samples.size();i++)
                {
                    olr = new OlgaLinearReport(samples.get(i),content,reportProperties,level3node.findRootNode());
                    olrlist.add(olr);
                    
                    for (WeightedInterviewGroupReport wigr: olr.weightedInterviewGroupReportList)
                    {
                        for (InterviewGroup ig: wigr.groupslist)
                        {
                            linearRepGroupNamesList.add(ig.getName());
                                /*if (linearRepGroupNamesList.add(ig.getName()))
                                {
                                    rowHeaders.add(ig.getName());
                                    rowHeaders.add("Значимость "+ig.getName());
                                    rowTypeMap.put("Значимость "+ig.getName(),"VALUE;NOCHANGEODD;DA");
                                    rowTypeMap.put(ig.getName(),"VALUE;DA;NOBOTTOMBORDER;PERCENTAGES");
                                }*/
                        }
                    }
                }
                Collections.sort(linearRepGroupNamesList, new StringIntComparator());
                for (int i=0;i<samples.size();i++)
                {
                    if (i>0)
                        previosolr=olrlist.get(i-1);
                    
                    olr = olrlist.get(i);
                    
                    if (i==0)
                    {
                        if ((olr.getContent3()!=null)&
                             (this.mainHeader==null))  
                        {
                            String qtext = (olr.getContent3()!=null&&
                                    olr.getContent3().getText()!=null&&
                                    !olr.getContent3().getText().isEmpty())?" \""+olr.getContent3().getText()+"\"":"";
                            setMainHeader(olr.getContent3().getName()+qtext);
                        }
                        //Главный заголовок по умолчанию
                        else if (this.mainHeader==null)
                            setMainHeader("Линейное распределение групп");
                        
                        //Заголовок разделов (заголовок 1)
                        String volheader = null;
                        //System.out.println(this.reportProperties);
                        if (hasProperty("VOLUMEHEADER"))
                        {
                            volheader = getProperty("VOLUMEHEADER");
                        }
                        volumeheader = volheader==null?olr.getContent1().getName():volheader;                        
                        
                        //Заголовок 2
                        totalrowheader = getHeader()==null?olr.getContent2().getName():getHeader();
                        
                        
                        
                        rowHeaders.add(volumeheader);
                        rowTypeMap.put(volumeheader,"VOLUMEHEADER");
                        volumeWidth = olr.getGcr().getAgroup2().size();
                        sampleWidth = volumeWidth*(olr.group1samples.size());
                        
                        
                        rowHeaders.add(totalrowheader);
                        rowTypeMap.put(totalrowheader,"HEADER");
                        
                        
                        rowHeaders.add("Размер выборки");
                        rowTypeMap.put("Размер выборки","VALUE");
                        
                        rowHeaders.add("В группах");
                        rowTypeMap.put("В группах","VALUE");
                        
                        //Группы 3
                        for (String grname: linearRepGroupNamesList)
                        {
                            rowHeaders.add(grname);
                            rowHeaders.add("Значимость "+grname);
                            rowTypeMap.put("Значимость "+grname,"VALUE;NOCHANGEODD;DA");
                            rowTypeMap.put(grname,"VALUE;DA;NOBOTTOMBORDER;PERCENTAGES");
                        }
                        
                    }
                    
                    
                    
                    
                    List<String>volumenameslist=new ArrayList<>();
                    for(InterviewGroup ig: olr.getGcr().getAgroup1())
                    {
                        String name1 = ig.getName();
                        if (olr.getGcr().isAgrop1fictive())
                            if (olr.getContent1().getAnswerCodeMap()!=null)
                            {
                                name1 = olr.getContent1().getAnswerCodeMap().get(ig.getName());
                            }
                        if (name1.equals(""))
                            name1 = ig.getName();
                        volumenameslist.add(name1);
                    }
                    boolean addall=false;   
                    if (hasProperty("AddAll"))
                    {
                        if (getProperty("AddAll")!=null)  
                            if (getProperty("AddAll").equalsIgnoreCase("true"))
                            {
                                addall=true;
                                volumenameslist.add(0,"Все");
                                List<String> namelist = new ArrayList<>();
                                for (InterviewGroup ag2: olr.getGcr().getAgroup2())
                                {
                                    namelist.add(ag2.getName());
                                }
                                addStrToList(namelist, totalrowheader);
                            }
                    }
                    addStrToList(volumenameslist, volumeheader);
                    for (InterviewGroup ag1: olr.getGcr().getAgroup1())
                    {
                        List<String> namelist = new ArrayList<>();
                        for (InterviewGroup ag2: olr.getGcr().getAgroup2())
                        {
                            namelist.add(ag2.getName());
                        }
                        addStrToList(namelist, totalrowheader);
                    }
                    Content weightContent = olr.weightedInterviewGroupReportList.get(0).weightContent;
                    List<Number> samplecountlist = new ArrayList<>();
                        
                    for(int j=0;j<olr.group2samples.size();j++)
                    {
                        samplecountlist.add(ContentUtils.countWeights(weightContent, olr.group2samples.get(j)));
                    }
                    addToList(samplecountlist, "Размер выборки");
                    
                    
                    List<Number> groupedtotalcountlist = new ArrayList<>();
                    for(int j=0;j<olr.weightedInterviewGroupReportList.size();j++)
                    {
                            groupedtotalcountlist.add(olr.weightedInterviewGroupReportList.get(j).getTotalGroupedWeight());
                    }
                    addToList(groupedtotalcountlist, "В группах");
                    
                    if ((olr.weightedInterviewGroupReportList!=null)&&(!olr.weightedInterviewGroupReportList.isEmpty()))
                    {
                        int groups_size = linearRepGroupNamesList.size();
                        for (int j=0;j<groups_size;j++)
                        {
                            List<Number> valList = new ArrayList<>();
                            String groupName = linearRepGroupNamesList.get(j);
                            
                            for (int k=0;k<olr.weightedInterviewGroupReportList.size();k++)
                            {
                                WeightedInterviewGroupReport rep = olr.weightedInterviewGroupReportList.get(k);
                                InterviewGroup ig = rep.findGroupByName(groupName);
                                
                                Double size = rep.getTotalGroupedWeight(),oldsize=0.0;
                                Double val = rep.weightedCountmap.getOrDefault(ig, 0.0),oldval=null;
                                Double percent = size>0?val/size*100.0:0.0, oldpercent = null;
                                WeightedInterviewGroupReport oldrep = null;
                                if (previosolr!=null)
                                {
                                    oldrep = previosolr.weightedInterviewGroupReportList.get(k);
                                    InterviewGroup oldig = oldrep.findGroupByName(groupName);
                                    oldval = oldrep.weightedCountmap.getOrDefault(oldig, 0.0);
                                    oldsize = oldrep.getTotalGroupedWeight();
                                    oldpercent = oldsize>0?oldval/oldsize*100.0:0.0;
                                    Double davalue = ReportUtils.getNormDAVal(oldpercent, percent, oldsize, size);
                                    Color color = ReportUtils.getColorFromDiff(davalue);
                                    addToColorMap(groupName,color);
                                }
                                else
                                {
                                     addToColorMap(groupName,Color.BLACK);
                                }
                                valList.add(round(percent));
                                
                            }
                            addToList(valList, linearRepGroupNamesList.get(j));
                            //System.out.println(reportValues);
                            List<String> normDAlist = null;
                            if (olr.normDAMap.containsKey(groupName))
                            {
                                normDAlist=olr.normDAMap.get(groupName);
                                addStrToList(normDAlist, "Значимость "+groupName);
                                colorMap.put("Значимость "+groupName,Collections.nCopies(samples.size()*olr.group2samples.size(), Color.GREEN));
                            }
                            else
                            {
                                if (linearRepGroupNamesList.contains(groupName))
                                {
                                    normDAlist=new ArrayList<>();
                                    for (int c=0;c<olr.weightedInterviewGroupReportList.size();c++)
                                    {
                                        normDAlist.add("");
                                    }
                                    addStrToList(normDAlist, "Значимость "+groupName);
                                }
                            }
                    
                        }
                    }
                    previosolr=olr;
                    
                }
                break;
            case "OlgaMerged":
                ReportFactory rf = new OlgaMegredReportFactory();
                Report report = rf.makeReport(level3node,content, reportProperties,splittedSamples,splittedNames);
                return report;
                
            case "Olga1":
                rf = new Olga1ReportFactory();
                report = rf.makeReport(level3node,content, reportProperties,splittedSamples,splittedNames);
                return report;
                
            case "Olga":
                double universe=1000000.0;
                double confLevel=0.95;
                //Главный заголовок
                
                if (hasProperty("universe"))
                {
                    try
                    {
                        Double un = Double.parseDouble(getProperty("universe"));
                        universe = un;
                    }
                    catch (NumberFormatException e)
                    {
                        
                    }
                }
                if (hasProperty("conflevel"))
                {
                    try
                    {
                        Double un = Double.parseDouble(getProperty("conflevel"));
                        confLevel = un;
                    }
                    catch (NumberFormatException e)
                    {
                        
                    }
                }
                volumeheader = null;
                OlgaReport previosolga = null;
                OlgaReport olga = null;
                List<Color> cList = new ArrayList<>();
                List<Color> meanReportColorList = new ArrayList<>();
                    
                totalrowheader="";
                List<AnswerGroup> agList=null;
                Set<String> uniquevallist = null;
                samples=splittedSamples;
                
                for (int i=0;i<samples.size();i++)
                {
                    if (olga!=null)
                        previosolga=olga;
                    olga = new OlgaReport(samples.get(i),content,reportProperties,level3node.findRootNode());
                    
                    
                    
                    
                    if (i==0)
                    {
                        //Уникальные значения линейного отчета
                        uniquevallist = ContentUtils.getContentUniqueValuesFromSampleList(sampleList, olga.content3);

                        //Группы
                        agList = olga.npsReportList.get(0).getGroups();
                        
                        //Главный заголовок по умолчанию
                        String qtext = (olga.content3.getText()!=null&&!olga.content3.getText().isEmpty())?" \""+olga.content3.getText()+"\"":"";
                        setMainHeader("Значимости для "+olga.content3.getName()+qtext);
                        
                        //Заголовок разделов (заголовок 1)
                        String volheader = null;
                        if (hasProperty("VOLUMEHEADER"))
                        {
                            volheader = getProperty("VOLUMEHEADER");
                        }
                        volumeheader = volheader==null?olga.content1.getName():volheader;                        
                        
                        //Заголовок 2
                        totalrowheader = getHeader()==null?olga.content2.getName():getHeader();
                        
                        
                        
                        rowHeaders.add(volumeheader);
                        rowTypeMap.put(volumeheader,"VOLUMEHEADER");
                        volumeWidth = olga.gcr.getAgroup2().size();
                        sampleWidth = volumeWidth*(olga.group1samples.size());
                        
                        
                        //Последовательность строк в отчете
                        rowHeaders.add(totalrowheader);
                        rowTypeMap.put(totalrowheader,"HEADER");
                        
                        
                        rowHeaders.add("Размер выборки");
                        rowTypeMap.put("Размер выборки","VALUE");
                        
                        rowHeaders.add("В группах");
                        rowTypeMap.put("В группах","VALUE");
                        
                        
                        rowHeaders.add("MEAN "+olga.content3.getName());
                        rowHeaders.add("MEAN DA "+olga.content3.getName());
                        
                        if (getProperty("DebugVals")!=null&&getProperty("DebugVals").equalsIgnoreCase("true"))
                        {
                            rowHeaders.add("VARIANCE "+olga.content3.getName());
                        }
                    
                        rowHeaders.add("SEMEAN "+olga.content3.getName());
                        
                        rowHeaders.add("SEMEAN Conf. Interval "+olga.content3.getName());
                        
                        if (getProperty("DebugVals")!=null&&getProperty("DebugVals").equalsIgnoreCase("true"))
                        {
                             
                            rowHeaders.add("DIFFERENCE "+olga.content3.getName());
                        
                            rowHeaders.add("STUDENT "+olga.content3.getName());
                            
                            rowHeaders.add("SD");
                        }    
                    
                        rowHeaders.add("NPS "+olga.content3.getName());
                        rowTypeMap.put("NPS "+olga.content3.getName(),"VALUE;DA;PERCENTAGES;NOBOTTOMBORDER");
                        
                        rowHeaders.add("NPSDA 2s");
                        rowTypeMap.put("NPSDA 2s","VALUE;DA;NOCHANGEODD");
                        
                        rowHeaders.add("ConfInt");
                        rowTypeMap.put("ConfInt","VALUE;DA");
                        
                        
                        
                        for (AnswerGroup ag: agList)
                        {
                            rowHeaders.add(ag.getName());
                            rowTypeMap.put(ag.getName(),"VALUE;DA;PERCENTAGES;NOBOTTOMBORDER");
                            
                            rowHeaders.add("Значимости "+ag.getName());
                            rowTypeMap.put("Значимости "+ag.getName(),"VALUE;NOCHANGEODD;DA");
                        }
                        
                        //Инициализируем карты цветов для величин с измерением значимости
                        colorMap.put("MEAN DA "+olga.content3.getName(),Collections.nCopies(samples.size()*olga.group2samples.size(), Color.GREEN));
                        //colorMap.put("MEAN "+olga.content3.getName(),Collections.nCopies(samples.size()*olga.group2samples.size(), Color.BLACK));
                    }
                    
                    List<String>volumenameslist=new ArrayList<>();
                    for(InterviewGroup ag: olga.gcr.getAgroup1())
                    {
                        String name1 = ag.getName();
                        if (olga.gcr.isAgrop1fictive())
                            if (olga.content1.getAnswerCodeMap()!=null)
                            {
                                name1 = olga.content1.getAnswerCodeMap().get(ag.getName());
                            }
                        if (name1.equals(""))
                            name1 = ag.getName();
                        volumenameslist.add(name1);
                    }
                    
                    
                    if (hasProperty("AddAll"))
                    {
                      if (getProperty("AddAll")!=null)  
                        if (getProperty("AddAll").equalsIgnoreCase("true"))
                        {
                            volumenameslist.add(0,"Все");
                            List<String> namelist = new ArrayList<>();
                            for (InterviewGroup ag2: olga.gcr.getAgroup2())
                            {
                                namelist.add(ag2.getName());
                            }
                            addStrToList(namelist, totalrowheader);
                        }
                    }
                    
                    addStrToList(volumenameslist, volumeheader);
                    
                    for(int j=0;j<olga.group2samples.size();j++)
                    {
                        if (previosolga!=null)
                        {
                                NPSReport rep1 = previosolga.npsReportList.get(j);
                                NPSReport rep2 = olga.npsReportList.get(j);
                                
                                
                                if (rep1!=null&&rep2!=null)
                                {    
                                    DAReport crossdarep = new DAReport(rep1, rep2, olga.content2.getName(), olga.content2.getName(), confLevel, universe);
                                    Double nps1 = rep1.getNps();
                                    Double nps2 = rep2.getNps();
                                    double da = crossdarep.minimumSigDif2s;
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
                    
                    //System.out.println(this.reportValues);
                    for (InterviewGroup ag1: olga.gcr.getAgroup1())
                    {
                        String name1="";
                        if (olga.gcr.isAgrop1fictive())
                            if (olga.content1.getAnswerCodeMap()!=null)
                            {
                                name1 = olga.content1.getAnswerCodeMap().get(ag1.getName());
                            }
                        if ((name1==null)||(name1.isEmpty()))
                            name1 = ag1.getName();
                        List<String> namelist = new ArrayList<>();
                        for (InterviewGroup ag2: olga.gcr.getAgroup2())
                        {
                            
                            String name2="";
                            
                            
                            if (olga.gcr.isAgrop2fictive())
                                if (olga.content2.getAnswerCodeMap()!=null)
                                {
                                    name2 = olga.content2.getAnswerCodeMap().get(ag2.getName());
                                }
                            if ((name2==null)||(name2.isEmpty()))
                                name2 = ag2.getName();
                            namelist.add(name2);
                            
                        }
                        addStrToList(namelist, totalrowheader);
                    }
                    
                    
                    
                    
                    
                    List<Number> samplecountlist = new ArrayList<>();
                    for(int j=0;j<olga.group2samples.size();j++)
                        samplecountlist.add(olga.group2samples.get(j).size());
                    addToList(samplecountlist, "Размер выборки");
                    
                    
                    List<Number> groupedtotalcountlist = new ArrayList<>();
                    for(int j=0;j<olga.npsReportList.size();j++)
                    {
                        groupedtotalcountlist.add(olga.npsReportList.get(j).getIntGroupedTotal());
                    }
                    addToList(groupedtotalcountlist, "В группах");
                    List<String> ganormDAlist = null;
                    Map<String,List<Number>> grValList = new HashMap<>();
                    for (AnswerGroup ag: agList)
                    {
                        String gname = ag.getName();
                        List<Number> valList = new ArrayList<>();
                        for (int k=0;k<olga.npsReportList.size();k++)
                        {
                            NPSReport currentrep = olga.npsReportList.get(k);
                            Double size = currentrep.getIntGroupedTotal()+0.0;
                            Double count = currentrep.getValuecountermap().get(currentrep.getGroupByName(gname));
                            Double percent = size>0?count/size*100.0:0.0;
                            NPSReport oldrep = null;
                            if (previosolga!=null)
                            {
                                oldrep=previosolga.npsReportList.get(k);
                                Double oldsize = oldrep.getIntGroupedTotal()+0.0;
                                Double oldcount = oldrep.getValuecountermap().get(oldrep.getGroupByName(gname));
                                Double oldpercent = oldsize>0?oldcount/oldsize*100.0:0.0;
                                Double davalue = ReportUtils.getNormDAVal(oldpercent, percent, oldsize, size);
                                Color color = ReportUtils.getColorFromDiff(davalue);
                                addToColorMap(gname,color);
                            }
                            else
                            {
                                addToColorMap(gname,Color.BLACK);
                            }
                            valList.add(round(percent));
                            
                        }
                        grValList.put(gname, valList);
                        //System.out.println(""+this.reportValues);
                        addToList(valList,gname);
                        
                        if (olga.ganormDAMap.containsKey(gname))
                        {
                            ganormDAlist=olga.ganormDAMap.get(gname);
                            addStrToList(ganormDAlist, "Значимости "+gname);
                            colorMap.put("Значимости "+gname,Collections.nCopies(samples.size()*olga.group2samples.size(), Color.GREEN));
                        }
                    }
                    
                    List<Number> npscountlist = new ArrayList<>();
                    for(int j=0;j<olga.npsReportList.size();j++)
                    {
                        Double nps = olga.npsReportList.get(j).getNps()==null?0:olga.npsReportList.get(j).getNps();
                        npscountlist.add(round(nps));
                    }
                    addToList(npscountlist, "NPS "+olga.content3.getName());
                    
                    
                    
                    List<String> da2slist = new ArrayList<>();
                    for(int j=0;j<olga.dastringarray2s.length;j++)
                    {
                        String da = olga.dastringarray2s[j];
                        da2slist.add(da);
                    }
                    addStrToList(da2slist,"NPSDA 2s");
                    
                    
                    
                    List<Number> confIntlist = new ArrayList<>();
                    //List<Number> oldconfIntlist = new ArrayList<>();
                    for(int j=0;j<olga.newconfIntervalList.size();j++)
                    {
                        //Double ci = olga.confIntervalList.get(j);
                        //oldconfIntlist.add(round(ci));
                        Double newci=olga.newconfIntervalList.get(j);
                        confIntlist.add(ReportUtils.round(newci,fpDIGITS));
                    }
                    addToList(confIntlist, "ConfInt");
                    
                    
                    
                    
                    if (getProperty("DebugVals")!=null&&getProperty("DebugVals").equalsIgnoreCase("true"))
                    {
                        rowTypeMap.put("SD","VALUE");
                        List<Number> sdcountlist = new ArrayList<>();
                        for(int j=0;j<olga.npsReportList.size();j++)
                        {
                            sdcountlist.add(ReportUtils.round(olga.npsReportList.get(j).getStandartDeviation(),fpDIGITS));
                        }
                        addToList(sdcountlist, "SD");
                    }
                    
                    
                    
                    //Линейный отчет
                    if ((olga.linearReportList!=null)&&(!olga.linearReportList.isEmpty()))
                    {
                        
                        String rowname=null;
                        for (String val:uniquevallist)
                        {
                            if (hasProperty("ShowAnswersText")&&getProperty("ShowAnswersText").equalsIgnoreCase("true")&&
                                    (olga.content3.getAnswerCodeMap()!=null)&&(olga.content3.getAnswerCodeMap().isEmpty()==false))
                            {    
                                String answer = olga.content3.getAnswerCodeMap().get(val);
                                if ((answer!=null)&&(!answer.isEmpty()))
                                    rowname=answer;
                                else
                                    rowname = val;
                            }
                            else
                            {
                                rowname = val;
                            }
                            
                            rowHeaders.add(rowname);
                            if (olga.normDAMap!=null&&olga.normDAMap.containsKey(val))
                            {
                                rowHeaders.add("Значимость "+rowname);
                                rowTypeMap.put("Значимость "+rowname,"VALUE;NOCHANGEODD;DA");
                                rowTypeMap.put(rowname,"VALUE;DA;NOBOTTOMBORDER;PERCENTAGES");
                            }
                            else
                            {
                                rowTypeMap.put(rowname,"VALUE;DA;PERCENTAGES");
                            }
                            
                            
                            
                            List<Number> lineartlist = new ArrayList<>();
                            List<String> normDAlist = new ArrayList<>();
                            List<Color> colList = new ArrayList<>();
                            for(int j=0;j<olga.linearReportList.size();j++)
                            {
                                LinearReport lr = olga.linearReportList.get(j);
                                LinearReport oldlr = previosolga!=null?previosolga.linearReportList.get(j):null;
                                
                                double v = lr.getTotal()!=0?lr.getStatmap().getOrDefault(val, 0.0)*100.0/lr.getTotal():0.0;
                                double lrpercent = round(v);
                                
                                if (oldlr!=null)
                                {
                                    double oldv = oldlr.getTotal()!=0?oldlr.getStatmap().getOrDefault(val, 0.0)*100.0/oldlr.getTotal():0.0;
                                    double oldlrpercent = round(oldv);
                                    Double davalue = ReportUtils.getNormDAVal(oldlrpercent, lrpercent, oldlr.getTotal(), lr.getTotal());
                                    Color color = ReportUtils.getColorFromDiff(davalue);
                                    addToColorMap(rowname,color);
                                }
                                else
                                {
                                    addToColorMap(rowname,Color.BLACK);
                                }
                                
                                lineartlist.add(lrpercent);
                            }
                            addToList(lineartlist, rowname);
                            
                            
                            
                            
                            
                            if (olga.normDAMap.containsKey(val))
                            {
                                normDAlist=olga.normDAMap.get(val);
                                addStrToList(normDAlist, "Значимость "+rowname);
                                colorMap.put("Значимость "+rowname,Collections.nCopies(samples.size()*olga.group2samples.size(), Color.GREEN));
                            }
                        }
                    }
                    
                   
                    //Отчет по средним   
                    rowTypeMap.put("MEAN "+olga.content3.getName(),"VALUE;DA;NOBOTTOMBORDER");
                    List<Number> olgameanlist = new ArrayList<>();
                    for(int j=0;j<olga.meanReportList.size();j++)
                    {
                        olgameanlist.add(ReportUtils.round((olga.meanReportList.get(j).meanList.get(0)),fpDIGITS));
                    }
                    addToList(olgameanlist, "MEAN "+olga.content3.getName());
                    
                    rowTypeMap.put("MEAN DA "+olga.content3.getName(),"VALUE;DA;NOCHANGEODD");
                    addStrToList(olga.meandastringList, "MEAN DA "+olga.content3.getName());
                    colorMap.put("MEAN DA "+olga.content3.getName(),Collections.nCopies(samples.size()*olga.group2samples.size(), Color.GREEN));
                    
                    //Динамические изменения средних (значимости)
                    if (previosolga!=null)
                        for(int j=0;j<olga.meanReportList.size();j++)
                        {
                            ArithmeticMeanReport curmeanrep = olga.meanReportList.get(j);
                            ArithmeticMeanReport prevmeanrep = previosolga.meanReportList.get(j);
                        
                        
                            Double curval = curmeanrep.meanList.get(0);
                            Double prevval = prevmeanrep.meanList.get(0);
                            Double curweight = curmeanrep.sizeList.get(0);
                            Double prevweight = prevmeanrep.sizeList.get(0);
                            Double curdisperse = Math.pow(curmeanrep.varianceList.get(0), 2);
                            Double prevdisperse = Math.pow(prevmeanrep.varianceList.get(0),2);
                        
                            Double davalue  = ReportUtils.getStudentDAVal2(prevval,curval,prevdisperse,curdisperse,prevweight,curweight,1-confLevel);
                            Color color = ReportUtils.getColorFromDiff(davalue);
                            meanReportColorList.add(color);
                            
                        }
                    else
                        for(int j=0;j<olga.meanReportList.size();j++)
                        {
                            meanReportColorList.add(Color.BLACK);
                        }
                    
                    
                    if (getProperty("DebugVals")!=null&&getProperty("DebugVals").equalsIgnoreCase("true"))
                    {
                        rowTypeMap.put("VARIANCE "+olga.content3.getName(),"VALUE");
                        List<Number> olgavariancelist = new ArrayList<>();
                        for(int j=0;j<olga.meanReportList.size();j++)
                        {
                            olgavariancelist.add(ReportUtils.round((olga.meanReportList.get(j).varianceList.get(0)), fpDIGITS));
                        }   
                        addToList(olgavariancelist, "VARIANCE "+olga.content3.getName());
                    }
                    
                    rowTypeMap.put("SEMEAN "+olga.content3.getName(),"VALUE");
                    List<Number> olgasemeanlist = new ArrayList<>();
                    rowTypeMap.put("SEMEAN Conf. Interval "+olga.content3.getName(),"VALUE");
                    List<Number> olgaconfintsemeanlist = new ArrayList<>();
                    for(int j=0;j<olga.meanReportList.size();j++)
                    {
                        olgasemeanlist.add(ReportUtils.round((olga.meanReportList.get(j).semeanList.get(0)),fpDIGITS));
                        olgaconfintsemeanlist.add(ReportUtils.round(olga.meanReportList.get(j).semeanList.get(0)*1.96,fpDIGITS));
                    }
                    addToList(olgasemeanlist, "SEMEAN "+olga.content3.getName());
                    addToList(olgaconfintsemeanlist, "SEMEAN Conf. Interval "+olga.content3.getName());
                    
                    if (getProperty("DebugVals")!=null&&getProperty("DebugVals").equalsIgnoreCase("true"))
                    {
                        rowTypeMap.put("DIFFERENCE "+olga.content3.getName(),"VALUE");
                        addStrToList(olga.davaluestringList, "DIFFERENCE "+olga.content3.getName());
                    
                        rowTypeMap.put("STUDENT "+olga.content3.getName(),"VALUE");
                        addStrToList(olga.daststringList, "STUDENT "+olga.content3.getName());
                    }
                    
                    
                    
                }
                colorMap.put("MEAN "+olga.content3.getName(), meanReportColorList);
                colorMap.put("NPS "+olga.content3.getName(), cList);
                break;
        }
        return this;
    }
    
    
    
    
    
    
    
    
    void addToColorMap (String key, Color val)
    {
        List<Color> cl = colorMap.get(key);
        if (cl==null)
        {
            cl = new ArrayList<>();
            cl.add(val);
            colorMap.put(key,cl);
        }
        else
        {
            cl.add(val);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    List<Number> addToList (List<Number> listToAdd, String mapKey)
    {
        List<? extends Object> currentlist = reportValues.get(mapKey);
        List<Number> newlist = new LinkedList<>();
        int maxsize = getMaxListSize();
        if (currentlist!=null)
        {
            
            for (Object no: currentlist)
            {
                Number n = (Number) no;
                newlist.add(n);
            }
            if (listToAdd.size()+newlist.size()<maxsize)
            {
                int sz = listToAdd.size();
                for (int i = newlist.size();i<maxsize-sz;i++)
                {
                    newlist.add(i,0);
                }
            }
            newlist.addAll(listToAdd);
            reportValues.put(mapKey, newlist);
        }
        else
        {
            rowHeaders.add(mapKey);
            newlist.addAll(listToAdd);
            int sz = newlist.size();
            for (int i=0;i<maxsize-sz;i++)
                newlist.add(0,0);
            reportValues.put(mapKey, newlist);
        }
        return newlist;
        
    }
    List<String> addStrToList (List<String> listToAdd, String mapKey)
    {
        List<? extends Object> currentlist = reportValues.get(mapKey);
        List<String> newlist = new LinkedList<>();
        if (currentlist!=null)
        {
            for (Object no: currentlist)
            {
                String str = (String) no;
                newlist.add(str);
            }
            newlist.addAll(listToAdd);
            reportValues.put(mapKey, newlist);
        }
        else
        {
            rowHeaders.add(mapKey);
            int maxsize = getMaxListSize();
            if (this.rowTypeMap.containsKey(mapKey))
            {
                if (this.rowTypeMap.get(mapKey).equalsIgnoreCase("VOLUMEHEADER"))
                {
                    maxsize=this.getSampleWidth()/this.getVolumeWidth();
                }
            }
            
            newlist.addAll(listToAdd);
            for (int i=0;i<maxsize-newlist.size();i++)
                newlist.add(0,"");
            reportValues.put(mapKey, newlist);
        }
        return newlist;
        
    }
    private int getMaxListSize()
    {
        int maxsize=0;
        if (reportValues.isEmpty())
            return 0;
        for (Map.Entry<String,List<? extends Object>>entry:reportValues.entrySet())
        {
            int size = entry.getValue().size();
            if (size>maxsize)
                maxsize=size;
        }
        return maxsize;
    }
    
    private void fillRow(Integer[][] finalreportvalues, int rownumber, int value) 
    {
        if (finalreportvalues!=null)
            if (finalreportvalues.length>rownumber)
            {
                Arrays.fill(finalreportvalues[rownumber], value);
            }
    }
    
    private void fillColumn(Integer[][] finalreportvalues, int colnumber, int value) 
    {
        if (finalreportvalues!=null)
            if (finalreportvalues[0].length>colnumber)
            {
                for (int j=0; j<finalreportvalues.length;j++)
                    finalreportvalues[j][colnumber]=value;
            }
    }
    
    List<UniqueList<Map<Content, String>>> splitSamplesBy(String splitVarName, List<UniqueList<Map<Content, String>>> sampleList, List<String> sampleNames, List<String> newNames) throws VariableNotFoundException 
    {
        Content splitContent = ContentUtils.getContentByNameFromInterviewList(sampleList.get(0), splitVarName);
        
        if (splitContent==null)
            throw new VariableNotFoundException(splitVarName);
        List<AnswerGroup> agroups = GroupsReport.constructAnswerGroupsFormContent(splitContent);
        List<UniqueList<Map<Content, String>>> newSampleList = new ArrayList<>();
        int i = 0;
        for(UniqueList<Map<Content, String>> interviews: sampleList)
        {
            List<UniqueList<Map<Content, String>>> samples = Filter.splitByAnswerGroups(interviews, agroups, splitContent);
            String oldName = sampleNames.get(i);
            int j=0;
            for (UniqueList<Map<Content, String>> gsamps: samples)
            {
                if (!gsamps.isEmpty())
                {
                    newSampleList.add(gsamps);
                    newNames.add(oldName+": "+agroups.get(j).getName());
                }
                j++;
            }
            i++;
        }
        return newSampleList;
    }
    private List<String> updateSplittedSampleNames(String splitVarName, List<UniqueList<Map<Content, String>>> sampleList, List<String> sampleNames) throws VariableNotFoundException 
    {
        List<String> newNames = new ArrayList<>();
        Content splitContent = ContentUtils.getContentByNameFromInterviewList(sampleList.get(0), splitVarName);
        if (splitContent==null)
            throw new VariableNotFoundException(splitVarName);
        if ((splitContent.getAnswerCodeMap()==null)||(splitContent.getAnswerCodeMap().isEmpty()))
            throw new VariableNotFoundException(splitVarName);
        int i=0;
        for (String oldName: sampleNames)
        {
            
            for (Map.Entry<String,String>entry:splitContent.getAnswerCodeMap().entrySet())
            {
                newNames.add(oldName+": "+splitVarName+" ("+entry.getKey()+" "+entry.getValue()+")");
                
                i++;
            }
        }
        return newNames;
    }
    
    public boolean isNoSampleHeader() 
    {
        if (hasProperty("drawsample"))
        {
            if (getProperty("drawsample").equalsIgnoreCase("false"))
            {
                return true;
            }
        }
        return false;
    }
    double round(double d)
    {
        return round_p(d);
    }
    double round_p(double d)
    {
        boolean percentages = false;
        int percentages_fpsigns=DEFAULTFPDIGITS;
        String v = reportProperties.getProperty("percentages");
        v=v==null?reportProperties.getProperty("Percentages"):v;
        v=v==null?reportProperties.getProperty("PERCENTAGES"):v;
        String fpDigitsByExcel = reportProperties.getProperty("FPDIGITSBYEXCEL");
        fpDigitsByExcel=fpDigitsByExcel==null?reportProperties.getProperty("fpdigitsbyexcel"):fpDigitsByExcel;
        if ((fpDigitsByExcel!=null)&&(fpDigitsByExcel.equalsIgnoreCase("true")))
        {
            if ((v!=null)&&v.equalsIgnoreCase("true"))
                return d/100;
            return d;
        }  
        
        if ((v!=null)&&v.equalsIgnoreCase("true"))
        {
            String percentages_fpsigns_s = reportProperties.getProperty("PERC_FPSigns");
            if (percentages_fpsigns_s!=null)
            {
                try
                {
                    percentages_fpsigns=Integer.parseInt(percentages_fpsigns_s);
                }
                catch (NumberFormatException nfe)
                {
                    reportProperties.setProperty("PERC_FPSigns", String.valueOf(fpDIGITS));
                }
            }
            else
            {
                reportProperties.setProperty("PERC_FPSigns", String.valueOf(fpDIGITS));
            }
            return d/100;
          
        }
        return ReportUtils.round(d, fpDIGITS);
    }
    
    long round0(double d)
    {
        return Math.round(d);
    }
    
    public void setNoFirstString(boolean noFirstString) 
    {
        this.noFirstString = noFirstString;
    }
    
    public void addRowType(String rowHeader, String rowType)
    {
        this.rowTypeMap.put(rowHeader, rowType);
    }
           
    
    
    @Override
    public String toString()
    {
        String tag = null;
        tag = this.getTag();
        //List<? extends Object> first = this.reportValues.get(rowHeaders.getFirst());
        return "Type:"+this.getReportType()+" ReportRow = "+this.repRow +" "+(tag==null?"":" Tag:"+tag)+" Samples:"+this.sampleNames.size()+" Size:"+this.rowHeaders.size()+"*"+this.sampleWidth+" per sample";
    }

    
    
    

    

    

    

    
    
    
    
}
