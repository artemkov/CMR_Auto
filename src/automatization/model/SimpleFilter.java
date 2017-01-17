/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.InvalidFilterException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Дина
 */
public class SimpleFilter 
{
    private String varName;
    private String arguments;

    public SimpleFilter(String varName, String arguments) {
        this.varName = varName;
        this.arguments = arguments;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public boolean check(Map<Content, String> interview) throws InvalidFilterException 
    {
            
            Content content = ContentUtils.getContentByNameFromInterview(interview, varName);
            
            
            if (content!=null)
            {
                List<String> filterValues = getArgs(arguments);
                String value = interview.get(content);
                
                if (filterValues.contains(value))
                    return true;
                else
                    return false;
            }
            else
            {
                //System.err.println("Variable "+varName+" was not found in interview");
                return false;
            }
            
        
    }
    
    private static List<String> getArgs (String arguments) throws InvalidFilterException
    {
        String pattern1 = "^(\\d+)\\|\\|(\\d+)$";
        Pattern p = Pattern.compile(pattern1);
        Matcher m = p.matcher(arguments);
        List<String> reslist = new LinkedList<>();
        if (m.matches())
        {
            Long from = roundDouble(Double.parseDouble(m.group(1)));
            Long to = roundDouble(Double.parseDouble(m.group(2)));
            if ((to!=null)&&(from!=null))
                for (long i=from; i<=to; i++)
                {
                    reslist.add(""+i);
                }
            return reslist;
        }
        
        String pattern2 = "^((\\d)+\\|)+(\\d+)$";
        p=Pattern.compile(pattern2);
        m=p.matcher(arguments);
        if (m.matches())
        {
            String[] strarray = arguments.split("\\|");
            reslist = Arrays.asList(strarray);
            return reslist;
        }
        
        try 
        {
            reslist.add(arguments);
            return reslist;
        }
        catch (NumberFormatException e)
        {
            throw new InvalidFilterException(arguments+" - не число!!!");
        }
    }
    
    private static Long roundDouble (double d)
    {
        if (d-Math.round(d)==0)
            return Math.round(d);
        else
            return null;
    }
    
    
    
}
