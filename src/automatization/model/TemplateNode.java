/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *
 * @author Артем Ковалев
 */
public class TemplateNode<T>
{
    private T data;
    private int level;
    private LinkedList<TemplateNode<T>> children = new LinkedList<>();
    private TemplateNode<T> parent;
    private List<String> zerolevelParams;
    private Map<String,String> zerolevelParamsMap = new HashMap<>();
    private Properties params;
    private int excelRow = 0;

    public int getExcelRow() {
        return excelRow;
    }

    public void setExcelRow(int excelRow) {
        this.excelRow = excelRow;
    }
    
    
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<String> getZerolevelParams() {
        return zerolevelParams;
    }

    public void setZerolevelParams(List<String> zerolevelParams) {
        this.zerolevelParams = zerolevelParams;
    }
    
    public void setParamsFromString(String paramstring)
    {
        if (paramstring!=null&&(!paramstring.isEmpty()))
        {
            Properties p = new Properties();
            List<String> plist = Arrays.asList(paramstring.split("@;"));
            for (String propertystring: plist)
            {
                String []keyval = propertystring.split("=", 2);
                p.setProperty(keyval[0], keyval[1]);
            }
            params=p;
        }
    }

    public Properties getParams() {
        return params;
    }

    public void setParams(Properties params) {
        this.params = params;
    }
    
    

    
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public LinkedList<TemplateNode<T>> getChildren() {
        return children;
    }

    public void setChildren(LinkedList<TemplateNode<T>> children) {
        this.children = children;
    }

    public TemplateNode<T> getParent() {
        return parent;
    }

    public void setParent(TemplateNode<T> parent) {
        this.parent = parent;
    }
    
    public TemplateNode<T> findRootNode()
    {
        if (level==0)
        {
            return this;
        }
        return getParent().findRootNode();
    }
    public void setRootParamsMap(String zeroString)
    {
        if (zeroString==null)
            return;
        List<String> params = new UniqueList<>();
        String paramsArray[] = zeroString.split(";");
        for (String param: paramsArray)
        {
            if (param.contains("SplitSampleBy"))
            {
                if (param.contains("="))
                {
                    String arg = (param.split("=",2)[1]).trim();
                    zerolevelParamsMap.put("SplitSampleBy", arg);
                }
            }
            
        }
        if (!params.isEmpty())
        {
            this.zerolevelParams=params;
        }
    }
    
    public void setZerolevelParamsFromString(String zeroString)
    {
        if (zeroString==null)
            return;
        List<String> params = new UniqueList<>();
        
        String paramsArray[] = zeroString.split(",");
        for (String param: paramsArray)
        {
            switch (param)
            {
                case "HIDDEN":
                case "Hidden":
                case "hidden":
                case "H":
                case "h":
                    params.add("HIDDEN");
                    break;
                    
            }
            
        }
        if (!params.isEmpty())
        {
            this.zerolevelParams=params;
        }
    }
}
