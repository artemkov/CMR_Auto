/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.GroupsFileNotFoundException;
import automatization.exceptions.VariableNotFoundException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Артем Ковалев
 */
public class NPSReport extends GroupsReport
{
    private List<AnswerGroup> topgList = new ArrayList<>(); 
    private List<AnswerGroup> bottomgList = new ArrayList<>();
    private Double nps = null;
    
    public NPSReport(List<AnswerGroup> groups) 
    {
        super(groups);
    }

    public Double getNps() {
        return nps;
    }

    
    public List<AnswerGroup> getTopgList() {
        return topgList;
    }

    public List<AnswerGroup> getBottomgList() {
        return bottomgList;
    }
    
    public void addTop (AnswerGroup ag)
    {
        if (this.getGroups().contains(ag))
            topgList.add(ag);
    }
    
    public void addBottom (AnswerGroup ag)
    {
        if (this.getGroups().contains(ag))
            bottomgList.add(ag);
    }
    
    public Double getTops()
    {
        Double top = 0.0;
        if ((topgList!=null)&&this.getValuecountermap()!=null)
        {
            for(AnswerGroup ag: this.getGroups())
            {
                if (topgList.contains(ag))
                {
                    Double v = this.getValuecountermap().get(ag);
                    if (v!=null)
                        top=top+v;
                }
            }
        }
        return top;
    }
    
    public Double getBottoms()
    {
        Double bottom = 0.0;
        if ((bottomgList!=null)&&this.getValuecountermap()!=null)
        {
            for(AnswerGroup ag: this.getGroups())
            {
                if (bottomgList.contains(ag))
                    bottom+=this.getValuecountermap().get(ag)==null?0:this.getValuecountermap().get(ag);
            }
        }
        return bottom;
    }
    
    public Double computeNPS(UniqueList<Map<Content,String>> interviews, String varName) throws VariableNotFoundException
    {
        if (((topgList!=null)&&(bottomgList!=null))&&(interviews!=null)&&(!interviews.isEmpty()))
        {
            int top=0;
            int bottom=0;
            this.populateGroups(interviews, varName);
            for(AnswerGroup ag: this.getGroups())
            {
                if (topgList.contains(ag))
                    top+=this.getValuecountermap().get(ag);
                if (bottomgList.contains(ag))
                    bottom+=this.getValuecountermap().get(ag);
                
            }
            int temp = this.getGroupedTotal()!=0?(top-bottom)*10000/this.getGroupedTotal():0;
            return (nps=temp/100.0);
            
        }
        return null;
    }
    
    public static NPSReport getAnswerGroupsWithNPSFromExcel(Path file) throws GroupsFileNotFoundException, IOException
    {
        List<AnswerGroup> groupList=new LinkedList<>();
        NPSReport report = null;
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
                            val = GroupsReport.dtolong(c.getNumericCellValue())+"";
                        }
                        AnswerGroup ag = groupList.get(i);
                        if (val!=null)
                            ag.addAnswer(val);
                    }
                    rowcount++;
                    r=sheet.getRow(rowcount);
                }
                report = new NPSReport(groupList);
                
                do
                {
                    rowcount++;
                    r=sheet.getRow(rowcount);
                }
                while (r==null||!(r.getCell(0).getCellType()==Cell.CELL_TYPE_STRING&& r.getCell(0).getStringCellValue().equals("TopGroups")));
                
                //Filling top and bottom grouplists
                List<String>topGroups=new ArrayList<>();
                List<String>bottomGroups=new ArrayList<>();
                rowcount++;
                while (r==null||!(sheet.getRow(rowcount).getCell(0).getCellType()==Cell.CELL_TYPE_STRING && sheet.getRow(rowcount).getCell(0).getStringCellValue().equals("end")))
                {
                    r = sheet.getRow(rowcount);
                    XSSFCell cell = r.getCell(0);
                    if (cell.getCellType()==Cell.CELL_TYPE_STRING)
                    {
                        String val = cell.getStringCellValue();
                        if (groups.contains(val))
                        {
                            topGroups.add(val);
                            report.addTop(report.getGroupByName(val));
                        }
                    }
                    cell = r.getCell(1);
                    if (cell.getCellType()==Cell.CELL_TYPE_STRING)
                    {
                        String val = cell.getStringCellValue();
                        if (groups.contains(val))
                        {
                            bottomGroups.add(val);
                            report.addBottom(report.getGroupByName(val));
                        }
                    }
                    rowcount++;
                }
                return report;
            }
        }
        catch (IOException ex) 
        {
            System.out.println("Something wrong with groupsfile");
            throw new GroupsFileNotFoundException(file.toAbsolutePath().toString());
        }
        return null;
        
        
    }
    public Double getPassives()
    {
        return this.getGroupedTotal()-getTops()-getBottoms();
    }
    
       
    public double getStandartDeviation()
    {
        if (nps==null)
            return 0;
        return Math.sqrt(Math.pow(100-nps,2)*getTops()/this.getGroupedTotal()+
                Math.pow(nps,2)*getPassives()/this.getGroupedTotal()+
                Math.pow(-100-nps,2)*getBottoms()/this.getGroupedTotal());
    }
}
