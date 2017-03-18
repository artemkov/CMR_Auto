/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automatization.model;

import automatization.exceptions.GroupsFileNotFoundException;
import automatization.exceptions.InvalidFilterException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Дина
 */
public class WeightedInterviewGroupNPSReport extends WeightedInterviewGroupReport implements NPSgetter
{
    private List<InterviewGroup> topgList = new ArrayList<>(); 
    private List<InterviewGroup> bottomgList = new ArrayList<>();
    private List<InterviewGroup> passivegList = new ArrayList<>();
    private Double nps = null;
    
    public WeightedInterviewGroupNPSReport(List<InterviewGroup> intgrouplist, Content wc, Content content) 
    {
        super(intgrouplist, wc, content);
    }

    public WeightedInterviewGroupNPSReport(List<InterviewGroup> intgrouplist, Content wc) 
    {
        super(intgrouplist, wc);
    }

    public WeightedInterviewGroupNPSReport(Path excelFilePath, Content wc, Content content) throws IOException 
    {
        super(excelFilePath, wc, content);
        //System.out.println(this.groupslist);
        getInterviewGroupsWithNPSFromExcel(excelFilePath);
    }

    public WeightedInterviewGroupNPSReport(Path excelFilePath, Content wc) throws IOException 
    {
        super(excelFilePath, wc);
        System.out.println(this.groupslist);
        getInterviewGroupsWithNPSFromExcel(excelFilePath);
    }
    
    private void addTop(InterviewGroup g)
    {
        if (super.groupslist.contains(g))
            topgList.add(g);
    }
    
    private void addBottom(InterviewGroup g)
    {
        if (super.groupslist.contains(g))
            bottomgList.add(g);
    }
    
    private void addPassive(InterviewGroup g)
    {
        if (super.groupslist.contains(g))
            passivegList.add(g);
    }

    @Override
    public Double getNps() {
        return nps;
    }
    
    
    @Override
    public void populateGroups(UniqueList<Map<Content,String>> interviews) throws InvalidFilterException
    {
        super.populateGroups(interviews);
        double total = super.getTotalGroupedWeight();
        if (total==0)
            nps=0.0;
        if (((topgList!=null)&&(bottomgList!=null))&&(interviews!=null)&&(!interviews.isEmpty()))
        {
            double top = 0.0,bottom =0.0;
            for (InterviewGroup topig: topgList)
            {
                top+=this.weightedCountmap.getOrDefault(topig, 0.0);
            }
            for (InterviewGroup bottomig: bottomgList)
            {
                bottom+=this.weightedCountmap.getOrDefault(bottomig, 0.0);
            }
            nps=(top-bottom)/total*100.0;
        }
        else
        {
            nps=0.0;
        }
        
    }
    
    
    private void getInterviewGroupsWithNPSFromExcel (Path file) throws GroupsFileNotFoundException
    {
        //Filling top and bottom grouplists
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
                while (r==null||!(r.getCell(0).getCellType()==Cell.CELL_TYPE_STRING&& r.getCell(0).getStringCellValue().equals("TopGroups")));
                
                rowcount++;
                while (r==null||!(sheet.getRow(rowcount).getCell(0).getCellType()==Cell.CELL_TYPE_STRING && sheet.getRow(rowcount).getCell(0).getStringCellValue().equals("end")))
                {
                    r = sheet.getRow(rowcount);
                    XSSFCell cell = r.getCell(0);
                    
                    if (cell!=null&&cell.getCellType()==Cell.CELL_TYPE_STRING)
                    {
                        InterviewGroup val = findGroupByName(cell.getStringCellValue());
                        if (val!=null)
                        {
                            addTop(val);
                        }
                    }
                    cell = r.getCell(1);
                    if (cell!=null&&cell.getCellType()==Cell.CELL_TYPE_STRING)
                    {
                        InterviewGroup val = findGroupByName(cell.getStringCellValue());
                        if (val!=null)
                        {
                            addBottom(val);
                        }
                    }
                    cell = r.getCell(2);
                    if (cell!=null&&cell.getCellType()==Cell.CELL_TYPE_STRING)
                    {
                        InterviewGroup val = findGroupByName(cell.getStringCellValue());
                        if (val!=null)
                        {
                            addPassive(val);
                        }
                    }
                    rowcount++;
                }
            }
        }       
        catch (FileNotFoundException ex) 
        {
            System.out.println("Something wrong with groupsfile. File not found");
            throw new GroupsFileNotFoundException(file.toAbsolutePath().toString());
        } catch (IOException ex) {
            System.out.println("Something wrong with groupsfile. IO Error");
            throw new GroupsFileNotFoundException(file.toAbsolutePath().toString());
        }
    }

    @Override
    public Double getTops() 
    {
        double top = 0.0;
        for (InterviewGroup topig: topgList)
        {
            top+=this.weightedCountmap.getOrDefault(topig, 0.0);
        }
        return top;
    }

    @Override
    public Double getBottoms() {
        double bottom = 0.0;
        for (InterviewGroup topig: bottomgList)
        {
            bottom+=this.weightedCountmap.getOrDefault(topig, 0.0);
        }
        return bottom;
    }

    @Override
    public Double getPassives() {
        double passive = 0.0;
        for (InterviewGroup topig: bottomgList)
        {
            passive+=this.weightedCountmap.getOrDefault(topig, 0.0);
        }
        return passive;
    }
    
    @Override
    public Double getStandartDeviation()
    {
        if (nps==null)
            return 0.0;
        return Math.sqrt(Math.pow(100-nps,2)*getTops()/this.getGroupedTotal()+
                Math.pow(nps,2)*getPassives()/this.getGroupedTotal()+
                Math.pow(-100-nps,2)*getBottoms()/this.getGroupedTotal());
    }
    
    @Override
    public Double getConfInterval(double conflevel, double univ) 
    {
        return ReportUtils.getNPSConfInterval2s (getGroupedTotal(), conflevel, univ, nps, getTops()*100.0/getGroupedTotal(), getBottoms()*100.0/getGroupedTotal(), getPassives()*100.0/getGroupedTotal());
    }
    
    @Override
    public Double getGroupedTotal() {
        return this.getTotalGroupedWeight();
    }
}