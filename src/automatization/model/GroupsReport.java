/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.GroupsFileNotFoundException;
import automatization.exceptions.TemplateFileIOException;
import automatization.exceptions.VariableNotFoundException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Дина
 */
public class GroupsReport 
{
    private List<AnswerGroup> groups;
    protected Map<AnswerGroup,Double> valuecountermap = new HashMap<>();
    private Content weightcontent=null;
    private boolean succeded;
    
    
    public GroupsReport(List<AnswerGroup> groups)
    {
        this.groups=groups;
    }
    public GroupsReport(List<AnswerGroup> groups, Content wc)
    {
        this.groups=groups;
        this.weightcontent=wc;
    }

    public Map<AnswerGroup, Double> getValuecountermap() 
    {
        return valuecountermap;
    }

    public List<AnswerGroup> getGroups() {
        return groups;
    }
    
    public AnswerGroup getGroupByName(String gname)
    {
        for (AnswerGroup ag: groups)
        {
            if (ag.getName().equals(gname))
                return ag;
        }
        return null;
    }
    
    public void populateGroups(UniqueList<Map<Content,String>> interviews, String varName) throws VariableNotFoundException
    {
        Set<Content> cset=null;
        boolean wcfound=false;
        Content content=ContentUtils.getContentByNameFromInterviewList(interviews, varName);
        if (weightcontent!=null)
        {
            if (ContentUtils.getContentByNameFromInterviewList(interviews,weightcontent.getName() )!=null)
            {
                wcfound=true;
            }
        }
        
        //Defining MapEntries
        for (AnswerGroup agroup: groups)
        {
            valuecountermap.put(agroup, 0.0);
        }
        
        if (content==null)
        {
            return;
        }
        
        for (Map<Content,String> interview: interviews)
        {
            String value = interview.get(content);
            for (AnswerGroup agroup: groups)
            {
                if (agroup.isAnswerInGroup(value))
                {
                    Double weight = 1.0;
                    
                    if (wcfound)
                    {
                        String weightvalue = interview.get(weightcontent);
                        try                    
                        {
                            weight = Double.parseDouble(weightvalue);
                        }
                        catch (NumberFormatException e)
                        {
                            weight = 0.0;
                        }
                    }
                    Double val = valuecountermap.get(agroup);
                    valuecountermap.put(agroup, val+weight);
                    
                }
            }
        }
    }
    
    
    
    public static List<AnswerGroup> constructAnswerGroupsFormContent(Content content)
    {
        List<AnswerGroup> ansGrList = new UniqueList<>();
        if ((content.getAnswerCodeMap()==null)||(content.getAnswerCodeMap().isEmpty()))
            return null;
        Map<String,String> codeMap = content.getAnswerCodeMap();
        Set<Map.Entry<String,String>> entrySet = codeMap.entrySet();
        TreeSet<Map.Entry<String,String>> treeeset = new TreeSet<>(new MapEntryComparator());
        treeeset.addAll(entrySet);
        for (Map.Entry<String,String> entry: treeeset)
        {
            String answer = entry.getKey();
            AnswerGroup ag = new AnswerGroup(answer);
            ag.addAnswer(answer);
            ansGrList.add(ag);
        }
        if (ansGrList.isEmpty())
            return null;
        return ansGrList;
    }
    
    public static List<AnswerGroup> constructAnswerGroupsFormContent(Content content, boolean useAnswerText)
    {
        List<AnswerGroup> ansGrList = new UniqueList<>();
        if ((content.getAnswerCodeMap()==null)||(content.getAnswerCodeMap().isEmpty()))
            return null;
               
        Map<String,String> codeMap = content.getAnswerCodeMap();
        Set<Map.Entry<String,String>> entrySet = codeMap.entrySet();
        
        TreeSet<Map.Entry<String,String>> treeeset = new TreeSet<>(new MapEntryComparator());
        treeeset.addAll(entrySet);
        
        for (Map.Entry<String,String> entry: treeeset)
        {
            String answer = entry.getKey();
            String text = entry.getValue();
            /*if (useAnswerText)
                if (text!=null)
                    answer+=" \""+content.getAnswerText(answer)+"\"";
            */
            AnswerGroup ag = new AnswerGroup(answer+(((text!=null)&&(!text.isEmpty()))?" \""+content.getAnswerText(answer)+"\"":answer));
            ag.addAnswer(answer);
            ansGrList.add(ag);
        }
        if (ansGrList.isEmpty())
            return null;
        return ansGrList;
    }
    
    public static List<AnswerGroup> constructAnswerGroupsFormContent(Content content,UniqueList<Map<Content,String>> interviews)
    {
        List<AnswerGroup> ansGrList = new UniqueList<>();
        TreeSet<String> uniqvalues = new TreeSet<>(new StringIntComparator());
                
        for(Map<Content,String> valmap : interviews )
        {   
            String value = valmap.get(content);
            if (value!=null)
            {
                if (uniqvalues.add(value))
                {
                    String answer = value;
                    AnswerGroup ag = new AnswerGroup(answer);
                    ag.addAnswer(answer);
                    ansGrList.add(ag);
                }
            }
        }
        if (ansGrList.isEmpty())
            return null;
        return ansGrList;
    }
    
    public static List<AnswerGroup> getAnswerGroupsFromExcel(Path file) throws GroupsFileNotFoundException, IOException
    {
        List<AnswerGroup> groupList=new LinkedList<>();
        
        try (FileInputStream inputStream = new FileInputStream(file.toFile()))
        {
            XSSFWorkbook wb = new XSSFWorkbook(new BufferedInputStream (inputStream));
            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFRow r = sheet.getRow(0);
            
            if(r.getCell(0).getCellType()==Cell.CELL_TYPE_STRING && r.getCell(0).getStringCellValue().equals("Groups"))
            {
                int rowcount = 0;
                do
                {
                    rowcount++;
                    r=sheet.getRow(rowcount);
                }
                while (r!=null && r.getCell(0).getCellType()!=Cell.CELL_TYPE_STRING);
                
                //читаем названия групп
                List<String> groups = new LinkedList<>();
                int numbofcells = r.getLastCellNum();
                int nofcounter=0;
                for (int i=0;i<numbofcells;i++)
                {
                    if (r.getCell(i).getCellType()==Cell.CELL_TYPE_STRING)
                    {
                        String gname = r.getCell(i).getStringCellValue();
                        groups.add(gname);
                        nofcounter++;
                    }
                }
                
                for (String gname: groups)
                {
                    groupList.add(new AnswerGroup(gname));
                }


                //Заполняем карту значений
                rowcount++;
                r=sheet.getRow(rowcount);
                
                while (r==null || !(r.getCell(0).getCellType()==Cell.CELL_TYPE_STRING && r.getCell(0).getStringCellValue().equals("endGroups")))
                {
                    //Цикл по колонкам со списками групп
                    for(int i = 0;i<nofcounter&&r!=null;i++)
                    {
                        String val=null;
                        XSSFCell c = r.getCell(i);
                        if (c==null)
                            continue;
                        if (c.getCellType()==Cell.CELL_TYPE_STRING)
                        {
                            val = c.getStringCellValue();
                        }
                        else if (c.getCellType()==Cell.CELL_TYPE_NUMERIC)
                        {
                            val = dtolong(c.getNumericCellValue())+"";
                        }
                        AnswerGroup ag = groupList.get(i);
                        if (val!=null)
                            ag.addAnswer(val);
                    }
                    rowcount++;
                    r=sheet.getRow(rowcount);
                }
                
                //
            }
        }
        catch (IOException ex) 
        {
            System.out.println("Something wrong with groupsfile");
            throw new GroupsFileNotFoundException(file.toAbsolutePath().toString());
        }
        return groupList;
    }
    
    public int getIntGroupedTotal()
    {
        int total=0;
        if ((groups!=null)&&(getValuecountermap()!=null)&&(!getValuecountermap().isEmpty()))
        for (AnswerGroup ag: this.groups)
        {
            if (getValuecountermap().get(ag)!=null)
                total+=this.getValuecountermap().get(ag);
        }
        return total;
    }
    
    static Number dtolong (double d)
    {
        if (d-Math.floor(d)==0)
        {
            return new Long(Math.round(d));
        }
        else
        {
            return d;
        }
    }
    
    public static List<InterviewGroup> getInterviewGroupsFromExcel(Path file, Content content) throws GroupsFileNotFoundException, IOException
    {
        List<InterviewGroup> groupList=new LinkedList<>();
        
        try (FileInputStream inputStream = new FileInputStream(file.toFile()))
        {
            XSSFWorkbook wb = new XSSFWorkbook(new BufferedInputStream (inputStream));
            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFRow r = sheet.getRow(0);
            
            if(r.getCell(0).getCellType()==Cell.CELL_TYPE_STRING && r.getCell(0).getStringCellValue().equals("Groups"))
            {
                int rowcount = 0;
                do
                {
                    rowcount++;
                    r=sheet.getRow(rowcount);
                }
                while (r!=null && r.getCell(0).getCellType()!=Cell.CELL_TYPE_STRING);
                
                //читаем названия групп
                List<String> groups = new LinkedList<>();
                int numbofcells = r.getLastCellNum();
                int nofcounter=0;
                for (int i=0;i<numbofcells;i++)
                {
                    if (r.getCell(i).getCellType()==Cell.CELL_TYPE_STRING)
                    {
                        String gname = r.getCell(i).getStringCellValue();
                        groups.add(gname);
                        nofcounter++;
                    }
                }
                
                for (String gname: groups)
                {
                    groupList.add(new InterviewGroup(gname,""));
                }


                //Заполняем карту значений
                rowcount++;
                r=sheet.getRow(rowcount);
                
                while (r==null || !(r.getCell(0).getCellType()==Cell.CELL_TYPE_STRING && r.getCell(0).getStringCellValue().equals("endGroups")))
                {
                    //Цикл по колонкам со списками групп
                    for(int i = 0;i<nofcounter&&r!=null;i++)
                    {
                        String val=null;
                        XSSFCell c = r.getCell(i);
                        if (c==null)
                            continue;
                        if (c.getCellType()==Cell.CELL_TYPE_STRING)
                        {
                            val = c.getStringCellValue();
                        }
                        else if (c.getCellType()==Cell.CELL_TYPE_NUMERIC)
                        {
                            val = dtolong(c.getNumericCellValue())+"";
                        }
                        else
                        {
                            continue;
                        }
                        
                        InterviewGroup ig = groupList.get(i);
                        Pattern p = Pattern.compile("^\\d+$");
                        Matcher m = p.matcher(val);
                        if (m.matches())
                        {
                            if (content!=null)
                                ig.addCondition(val, content.getName());
                        }
                        else
                        {
                            ig.addCondition(val);
                        }
                    }
                    rowcount++;
                    r=sheet.getRow(rowcount);
                }
                
                //
            }
        }
        catch (IOException ex) 
        {
            System.out.println("Something wrong with groupsfile");
            throw new GroupsFileNotFoundException(file.toAbsolutePath().toString());
        }
        return groupList;
    }
    
    public static List<InterviewGroup> constructInterviewGroupsFromContent(Content content,UniqueList<Map<Content,String>> interviews)
    {
        List<InterviewGroup> intGrList = new UniqueList<>();
        TreeSet<String> uniqvalues = new TreeSet<>(new StringIntComparator());
                
        for(Map<Content,String> valmap : interviews )
        {   
            String value = valmap.get(content);
            if (value!=null)
            {
                if (uniqvalues.add(value))
                {
                    String answer = value;
                    AnswerGroup ag = new AnswerGroup(answer);
                    ag.addAnswer(answer);
                    intGrList.add(new InterviewGroup(ag,content));
                }
            }
        }
        if (intGrList.isEmpty())
            return null;
        return intGrList;
    }
    
    public static List<InterviewGroup> constructInterviewGroupsFormContent(Content content, boolean useAnswerText)
    {
        List<InterviewGroup> intGrList = new UniqueList<>();
        if ((content.getAnswerCodeMap()==null)||(content.getAnswerCodeMap().isEmpty()))
            return null;
               
        Map<String,String> codeMap = content.getAnswerCodeMap();
        Set<Map.Entry<String,String>> entrySet = codeMap.entrySet();
        
        TreeSet<Map.Entry<String,String>> treeeset = new TreeSet<>(new MapEntryComparator());
        treeeset.addAll(entrySet);
        
        for (Map.Entry<String,String> entry: treeeset)
        {
            String answer = entry.getKey();
            String text = entry.getValue();
            /*if (useAnswerText)
                if (text!=null)
                    answer+=" \""+content.getAnswerText(answer)+"\"";
            */
            AnswerGroup ag = new AnswerGroup(answer+(((text!=null)&&(!text.isEmpty()))?" \""+content.getAnswerText(answer)+"\"":answer));
            ag.addAnswer(answer);
            intGrList.add(new InterviewGroup(ag,content));
        }
        if (intGrList.isEmpty())
            return null;
        return intGrList;
    }
    
    public static List<InterviewGroup> constructInterviewGroupsFormContent(Content content,UniqueList<Map<Content,String>> interviews, boolean useAnswerText)
    {
        List<InterviewGroup> intGrList = new UniqueList<>();
        TreeSet<String> uniqvalues = new TreeSet<>(new StringIntComparator());
        Map<String,String> codeMap = content.getAnswerCodeMap();
        
        Set<Map.Entry<String,String>> entrySet = null;
        TreeSet<Map.Entry<String,String>> treeeset = new TreeSet<>(new MapEntryComparator());
        if (codeMap!=null)
        {
            entrySet = codeMap.entrySet();
            treeeset.addAll(entrySet);
        }
                
        for(Map<Content,String> valmap : interviews )
        {   
            String value = valmap.get(content);
            if (value!=null)
            {
                uniqvalues.add(value);
            }
        }
        for (Map.Entry<String,String> entry: treeeset)
        {
            String code = entry.getKey();
            uniqvalues.add(code);
        }
        for (String answer:uniqvalues)
        {
            String a1 = answer;
            if (useAnswerText)
            {
                String text = codeMap.get(answer);
                if (text!=null&&!text.isEmpty())
                {
                    a1+=" \""+content.getAnswerText(answer)+"\"";
                }
                else
                {
                    a1=answer;
                    if (answer.isEmpty())
                    {
                        a1="MISSING";
                    }
                }
                
            }
            AnswerGroup ag = new AnswerGroup(a1);
            ag.addAnswer(answer);
            intGrList.add(new InterviewGroup(ag,content));
        }
        
        
        if (intGrList.isEmpty())
            return null;
        return intGrList;
    }
    
    public static List<InterviewGroup> constructInterviewGroupsFormContent(Content content)
    {
        return constructInterviewGroupsFormContent(content,false);
    }
}
