/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Дина
 */
public class StringIntComparator implements Comparator<String>
{
    private static final int COMP_VAL = 0;
    @Override
    public int compare(String o1, String o2) 
    {
        /*try 
        {
            Integer int1 = Integer.parseInt(o1);
            Integer int2 = Integer.parseInt(o2);
            return int1-int2;
        }
        catch (NumberFormatException ex)
        {
            return o1.compareTo(o2);
        }*/
        return compareIndexedStrings(o1,o2);
    }
    
    private int compareIndexedStrings(String s1, String s2)
    {
        
        
        int val1=COMP_VAL,val2=COMP_VAL;
        String pattern = "^\\s*(\\d+).*";
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(s1);
        if (mat.matches())
        {
            String v1 = mat.group(1);
            val1=Integer.parseInt(v1);
        }
        mat = pat.matcher(s2);
        if (mat.matches())
        {
            String v2 = mat.group(1);
            val2=Integer.parseInt(v2);
        }
        if ((val1==COMP_VAL)&(val2==COMP_VAL))
        {
            return s1.compareTo(s2);
        }
        else
        {
            return val1-val2;
        }
        
    }
    
}
