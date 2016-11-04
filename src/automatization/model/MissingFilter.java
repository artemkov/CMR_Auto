/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Артем Ковалев
 */
public class MissingFilter 
{
    List<String> vars = null;
    List<String> operators = null;

    
    public MissingFilter(String argsstr)
    {
        if (argsstr!=null&& !argsstr.isEmpty())
        {
            vars=Arrays.asList(argsstr.trim().split("\\|"));
            operators=new ArrayList<>();
            for (int i=0;i<vars.size()-1;i++)
                operators.add("|");
            
        }
    }
    
    public boolean check(Map<Content, String> interview) 
    {
        boolean res=false;
        int a=0;
        for (String var: vars)
        {
            Content c = ContentUtils.getContentByNameFromInterview(interview, var);
            if (c!=null)
            {
                String value = interview.getOrDefault(c, "");
                boolean val;
                if (value.equals(""))
                    val=true;
                else
                    val=false;
                if (a==0)
                    res=val;
                else
                    if (operators.get(a-1).equals("|"))
                    {
                        res = res|val;
                    }
                    else if (operators.get(a-1).equals("&"))
                    {
                        res = res&val;
                    }
                
            }
            else
            {
                if (a==0)
                    res = true;
                else
                    if (operators.get(a-1).equals("|"))
                    {
                        res = res|true;
                    }
                    else if (operators.get(a-1).equals("&"))
                    {
                        res = res&true;
                    }
            }
            a++;
        }
        return res;
    }
    
    
}
