/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmrauto;


import automatization.exceptions.InvalidNodeNameInTemplateFileException;
import automatization.exceptions.InvalidTemplateFileFormatException;
import automatization.exceptions.TemplateFileIOException;
import automatization.model.Content;
import automatization.model.TemplateNode;
import automatization.model.UniqueList;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.xssf.usermodel.*;


/**
 *
 * @author Артем Ковалев
 */
public class TemplateProcessor 
{
    
    
    
    
    
    private static List<ExcelNode> parseNodes (XSSFSheet sheet,int level)
    {
        List<ExcelNode> reslist = new LinkedList<>();
        ExcelNode currentNode = null;
        int end = sheet.getLastRowNum();
        for (int i=0; i<end+1; i++)
        {
                XSSFRow row = sheet.getRow(i);
                if (row==null)
                    continue;
                
                
                XSSFCell cell = row.getCell(level);
                if (cell==null)
                    continue;
                if (cell.getCellType()==XSSFCell.CELL_TYPE_STRING)
                {
                    
                    if (currentNode==null)
                    {
                        
                        currentNode=new ExcelNode(cell.getStringCellValue(),i,sheet.getLastRowNum());
                        if (!checkNode(currentNode, level, row))
                            continue;
                        
                        XSSFCell zerocell = level==0?null:row.getCell(0);
                        if ((level>0)&&((zerocell!=null)&&(zerocell.getCellType()==XSSFCell.CELL_TYPE_STRING)))
                        {
                            currentNode.zerocelldata=zerocell.getStringCellValue();
                        }
                        XSSFCell propscell = row.getCell(ExcelNode.PROPSROW);
                        if (propscell!=null&&propscell.getCellType()==XSSFCell.CELL_TYPE_STRING&&level!=ExcelNode.PROPSROW)
                        {
                            currentNode.propsdata=propscell.getStringCellValue();
                        }
                        if (level==0)
                        {
                            reslist.add(currentNode);
                            return reslist;
                        }
                            
                    }
                    else
                    {
                        currentNode.lastRow=i-1;
                        
                        if ((level==0)&&reslist.size()>0)
                        {
                            reslist.get(0).zerocelldata+=cell.getStringCellValue()+";";
                        }
                        else
                        {
                            reslist.add(currentNode);
                        }
                        if (level==0)
                           return reslist;
                        currentNode=new ExcelNode(cell.getStringCellValue(),i,sheet.getLastRowNum());
                        XSSFCell zerocell = level==0?null:row.getCell(0);
                        if ((level>0)&&((zerocell!=null)&&(zerocell.getCellType()==XSSFCell.CELL_TYPE_STRING)))
                        {
                            currentNode.zerocelldata=zerocell.getStringCellValue();
                        }
                        XSSFCell propscell = row.getCell(ExcelNode.PROPSROW);
                        if (propscell!=null&&propscell.getCellType()==XSSFCell.CELL_TYPE_STRING&&level!=ExcelNode.PROPSROW)
                        {
                            currentNode.propsdata=propscell.getStringCellValue();
                        }
                    }
                }
        }
        if (currentNode!=null)
            reslist.add(currentNode);
        return reslist;
    }
    private static Map<Integer,List<ExcelNode>> getExcelNodesMap (Path templatePath) throws InvalidTemplateFileFormatException, TemplateFileIOException
    {
        Map<Integer,List<ExcelNode>> nodesmap = new HashMap<>();
        try (FileInputStream inputStream = new FileInputStream(templatePath.toFile()))
        {
            XSSFWorkbook wb = new XSSFWorkbook(new BufferedInputStream (inputStream));
            XSSFSheet sheet = wb.getSheetAt(0);
            for (int level = 0; level<5; level++)
            {
                nodesmap.put(level,parseNodes(sheet,level));
            }
        }
        catch (IOException ex) 
        {
            System.out.println("Something wrong with datafile");
            throw new TemplateFileIOException();
        }
        return nodesmap;
        
    }
    
    public static TemplateNode<String> getTemplateNode(Path templatePath) throws InvalidTemplateFileFormatException, TemplateFileIOException
    {
        Map<Integer,List<ExcelNode>> excelnodemap = getExcelNodesMap (templatePath);
        return buildTemplateNode(excelnodemap);
    }
    
    private static TemplateNode<String> buildTemplateNode(Map<Integer,List<ExcelNode>> excelnodesmap)
    {
        ExcelNode rootenode = excelnodesmap.get(0).get(0);
        
        Set<Integer> propsrowset = new HashSet<>();
        
        TemplateNode<String> rootNode = new TemplateNode<>();
        if (rootenode.propsdata!=null)
        {
            rootNode.setParamsFromString(rootenode.propsdata);
            propsrowset.add(rootenode.firstRow);
        }
        rootNode.setData(rootenode.data);
        
        
        for (int i1=0; i1<excelnodesmap.get(1).size();i1++)
        {
            ExcelNode enode1 = excelnodesmap.get(1).get(i1);
            TemplateNode<String> tnode1 = new TemplateNode<>();
            tnode1.setData(enode1.data);
            tnode1.setLevel(1);
            tnode1.setParent(rootNode);
            tnode1.setZerolevelParamsFromString(enode1.zerocelldata);
            if (enode1.propsdata!=null)
            {
                tnode1.setParamsFromString(enode1.propsdata);
                propsrowset.add(enode1.firstRow);
            }
            if (rootNode.getChildren()==null)
                rootNode.setChildren(new LinkedList<>());
            rootNode.getChildren().add(tnode1);
            
            if (excelnodesmap.get(2)!=null)
            for (int i2=0; i2<excelnodesmap.get(2).size();i2++)
            {
                ExcelNode enode2 = excelnodesmap.get(2).get(i2);
                
                if (enode2.lastRow<enode1.firstRow)
                    continue;
                if (enode2.firstRow>enode1.lastRow)
                    break;
                if (enode1.lastRow<enode2.lastRow)
                    enode2.lastRow=enode1.lastRow;
                
                TemplateNode<String> tnode2 = new TemplateNode<>();
                tnode2.setExcelRow(enode2.firstRow);
                tnode2.setData(enode2.data);
                tnode2.setLevel(2);
                tnode2.setParent(tnode1);
                tnode2.setZerolevelParamsFromString(enode2.zerocelldata);
                if (enode2.propsdata!=null)
                {
                    tnode2.setParamsFromString(enode2.propsdata);
                    propsrowset.add(enode2.firstRow);
                }
                if (tnode1.getChildren()==null)
                    tnode1.setChildren(new LinkedList<>());
                tnode1.getChildren().add(tnode2);
                if (excelnodesmap.get(3)!=null)
                for (int i3=0; i3<excelnodesmap.get(3).size();i3++)
                {
                    ExcelNode enode3 = excelnodesmap.get(3).get(i3);
                    if (enode3.lastRow<enode2.firstRow)
                        continue;
                    if (enode3.firstRow>enode2.lastRow)
                        break;
                    if (enode2.lastRow<enode3.lastRow)
                        enode3.lastRow=enode2.lastRow;
                    TemplateNode<String> tnode3 = new TemplateNode<>();
                    tnode3.setExcelRow(enode3.firstRow);
                    tnode3.setData(enode3.data);
                    tnode3.setLevel(3);
                    tnode3.setParent(tnode2);
                    tnode3.setZerolevelParamsFromString(enode3.zerocelldata);
                    if (tnode2.getChildren()==null)
                        tnode2.setChildren(new LinkedList<>());
                    tnode2.getChildren().add(tnode3);
                    if (excelnodesmap.get(4)!=null)
                    for (int i4=0; i4<excelnodesmap.get(4).size();i4++)
                    {
                        ExcelNode enode4 = excelnodesmap.get(4).get(i4);
                        if (propsrowset.contains( enode4.firstRow))
                            continue;
                        if (enode4.lastRow<enode3.firstRow)
                            continue;
                        if (enode4.firstRow>enode3.lastRow)
                            break;
                        if (enode3.lastRow<enode4.lastRow)
                            enode4.lastRow=enode3.lastRow;
                        TemplateNode<String> tnode4 = new TemplateNode<>();
                        tnode4.setData(enode4.data);
                        tnode4.setExcelRow(enode4.firstRow);
                        tnode4.setLevel(4);
                        tnode4.setParent(tnode3);
                        tnode4.setZerolevelParamsFromString(enode4.zerocelldata);
                        if (tnode3.getChildren()==null)
                            tnode3.setChildren(new LinkedList<>());
                        tnode3.getChildren().add(tnode4);
                        
                    }
                }
            }
        }
        return rootNode;
    }
    
    
    
    public static TemplateNode<String> getTemplateFromExcel(Path templatePath) throws InvalidTemplateFileFormatException
    {
        try (FileInputStream inputStream = new FileInputStream(templatePath.toFile()))
        {
            XSSFWorkbook wb = new XSSFWorkbook(new BufferedInputStream (inputStream));
            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFRow row = sheet.getRow(0);
            XSSFCell cell0 = row.getCell(0);
            if ((cell0==null)||(cell0.getCellType()!=XSSFCell.CELL_TYPE_STRING))
                throw new InvalidTemplateFileFormatException();
            String templateName = cell0.getStringCellValue();
            if(templateName.isEmpty())
                throw new InvalidTemplateFileFormatException();
            TemplateNode<String> rootNode = new TemplateNode();
            rootNode.setData(templateName);
            
            row = sheet.getRow(1);
            /*while ((row.getCell(1)==null)||((row.getCell(1).getCellType()!=XSSFCell.CELL_TYPE_STRING)))
            {
                
            }*/
        }
        catch (IOException ex) 
        {
            System.out.println("Something wrong with datafile");
        }
        return null;
    }

    private static boolean checkNode(ExcelNode node, int level, XSSFRow row ) 
    {
        if (level!=ExcelNode.PROPSROW)
        {
            return true;
        }
        
        for (int i=0; i<ExcelNode.PROPSROW-1; i++)
        {
            XSSFCell cell = row.getCell(i);
            if ((cell!=null)&&(cell.getCellType()==XSSFCell.CELL_TYPE_STRING))
            {
                return false;
            }
        }
        return true;
        
        
    }

    static class ExcelNode 
    {
        static final int PROPSROW = 4;
        String propsdata;
        String zerocelldata;
        String data;
        int firstRow;
        int lastRow;

        public ExcelNode(String data, int firstRow, int lastRow) {
            this.data = data;
            this.firstRow = firstRow;
            this.lastRow = lastRow;
        }

        
    }
    
}
