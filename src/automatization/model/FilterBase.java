/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.InvalidArithmeticFilterException;
import automatization.exceptions.InvalidFilterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Артем Ковалев
 */
public class FilterBase 
{

    

    
    enum Operand
    {
        AND,OR,NOT
    }
    private String arg1;
    private String arg2;
    private Operand operand;
    private boolean isSimple=false;

    public FilterBase(String filterString) throws InvalidFilterException 
    {
        getArgs(filterString);
    }
    
    
    
    private void getArgs (String filterString) throws InvalidFilterException
    {
        
        
        char[] chararray = filterString.toCharArray();
        int currentlevel = 0;
        int toplevel=Integer.MAX_VALUE;
        int topleveloperandposition=-1;
        Operand topleveloperand = null;
        
        SimpleFilter sff = checkStringIsSimpleFilter(filterString);
        if (sff!=null)
        {
            isSimple = true;
            arg2=filterString;
            operand=null;
            toplevel=-1;
            return;
        }
        
        
        for (int i=0;i<chararray.length;i++)
        {
            char ch = chararray[i];
            if (chararray[i]=='(')
            {
                currentlevel++;
            }
            if (chararray[i]==')')
            {
                currentlevel--;
            }
            if (chararray[i]=='&'|chararray[i]=='|'|chararray[i]=='^')
            {
                if (currentlevel<toplevel)
                {
                    toplevel = currentlevel;
                    topleveloperandposition=i;
                    topleveloperand = parseOperand(chararray[i]);
                }
            }
        }
        String tempstr = new String(filterString);
        if (toplevel==Integer.MAX_VALUE)
        {
            SimpleFilter sf = checkStringIsSimpleFilter(filterString);
            if (sf==null)
            {
                //ArithmeticFilter af2 =checkStringIsArithmeticFilter(filterString);
                ArithmeticFilter af =checkStringIsArithmeticFilter2(filterString);
                if (af==null)
                {
                    MissingFilter mf = checkStringIsMissingFilter(filterString);
                    if (mf==null)
                        throw new InvalidFilterException(filterString);
                    else
                    {
                        isSimple = true;
                        arg2=filterString;
                        operand=null;
                        toplevel=-1;
                    }
                
                }
                else
                {
                    isSimple = true;
                    arg2=filterString;
                    operand=null;
                    toplevel=-1;
                }
                
            }
            else
            {
                isSimple = true;
                arg2=filterString;
                operand=null;
                toplevel=-1;
                
            }
            
        }
        if (toplevel>0)
        {
            currentlevel=0;
            for (int i=0;i<chararray.length;i++)
            {
                if (chararray[i]=='(')
                {
                    
                    if (currentlevel<toplevel)
                    {
                        tempstr = removeCharFromString(tempstr,i);
                        if (topleveloperandposition>i)
                            topleveloperandposition--;
                    }
                    currentlevel++;
                }
                if (chararray[i]==')')
                {
                    currentlevel--;
                    if (currentlevel<toplevel)
                    {
                        tempstr = removeCharFromString(tempstr,i);
                        if (topleveloperandposition>i)
                            topleveloperandposition--;
                    }
                    
                    
                }
            }
        }
        //System.out.println("tempstr = '"+tempstr+"'");
        if (!isSimple)
        {
            this.arg1 = tempstr.substring(0,topleveloperandposition);
            this.arg2 = tempstr.substring(topleveloperandposition+1);
            this.operand = topleveloperand;
        }
        
        
    }
    private Operand parseOperand(char op)
    {
        if (op=='|')
            return Operand.OR;
        if (op=='&')
            return Operand.AND;
        if (op=='^')
            return Operand.NOT;
        return null;
    }
    private String removeCharFromString(String tempstr, int i) 
    {
        String temp = null;
        int len = tempstr.length();
        if ((i>0)&&(i<tempstr.length()-1))
            temp = tempstr.substring(0, i-1)+tempstr.substring(i+1);
        else if (i==0)
            temp = tempstr.substring(i+1);
        else if (i==tempstr.length())
            temp = tempstr.substring(0,i-1);
        return temp;
    }
    
    public boolean compute(Map<Content, String> interview) throws InvalidFilterException
    {
        boolean isSimpleArg1 = true;
        boolean isSimpleArg2 = true;
        boolean res1=false,res2=false;
        //Arg1
        if ((this.operand!=null)&&(this.operand!=Operand.NOT))
        {
            if (arg1==null)
                throw new InvalidFilterException("Операнд 1 не может быть null. arg1="+arg1+" arg2="+arg2+" operand="+operand);
            
            SimpleFilter sf1 = checkStringIsSimpleFilter(arg1);
            //не простой фильтр
            if (sf1==null)
            {
                //ArithmeticFilter af21 = checkStringIsArithmeticFilter(arg1);
                ArithmeticFilter af1 = checkStringIsArithmeticFilter2(arg1);
                
                //не арифметико-логический фильтр
                if (af1==null)
                {
                    //фильтр MISSING
                    MissingFilter mf1 = checkStringIsMissingFilter(arg1);
                    if (mf1==null)
                    {
                        if (operand==null)
                            throw new InvalidFilterException("Не простой фильтр!!!  arg1="+arg1+" arg2="+arg2+" operand="+operand);
                        isSimpleArg2 = false;
                        FilterBase arg = new FilterBase(arg1);
                        res1=arg.compute(interview);
                    }
                    else
                    {
                        isSimpleArg2 = true;
                        res1=mf1.check(interview);
                    }
                }
                else
                {
                    isSimpleArg1 = true;
                    res1=af1.check(interview);
                }
            }
            //простой фильтр
            else
            {
                isSimpleArg1 = true;
                res1=sf1.check(interview);
            }
        }
        //Arg2
        if (arg2==null)
            throw new InvalidFilterException("Операнд 2 не может быть null. arg1="+arg1+" arg2="+arg2+" operand="+operand);
        SimpleFilter sf2 = checkStringIsSimpleFilter(arg2);
        //не простой фильтр
        if (sf2==null)
        {
            //ArithmeticFilter af21 = checkStringIsArithmeticFilter(arg2);
            ArithmeticFilter af2 = checkStringIsArithmeticFilter2(arg2);
            
            //не арифметико-логический фильтр
            if (af2==null)
            {
                //фильтр MISSING
                MissingFilter mf2 = checkStringIsMissingFilter(arg2);
                if (mf2==null)
                {
                    if (operand==null)
                        throw new InvalidFilterException("Не простой фильтр!!!  arg1="+arg1+" arg2="+arg2+" operand="+operand);
                    isSimpleArg2 = false;
                    FilterBase arg = new FilterBase(arg2);
                    res2=arg.compute(interview);
                }
                else
                {
                    isSimpleArg2 = true;
                    res2=mf2.check(interview);
                }
            }
            //арифметико-логический фильтр
            else
            {
                isSimpleArg2 = true;
                res2=af2.check(interview);
            }
        }
        //простой фильтр
        else
        {
            isSimpleArg2 = true;
            res2=sf2.check(interview);
        }
        
        
        if (operand==Operand.AND)
            return res1&res2;
        if (operand==Operand.OR)
            return res1|res2;
        if (operand==Operand.NOT)
            return !res2;
        if (operand==null)
            return res2;
        throw new InvalidFilterException("Ошибка в фильтре!!!");
    }
    
    private SimpleFilter checkStringIsSimpleFilter(String filterString)
    {
        String simplePattern = "\\s*(\\w+)\\s*\\(\\s*([\\w\\.,-\\?!\\+]+\\s*|(\\d+\\s*\\|)*\\s*\\d+\\s*|\\d+\\s*\\|\\|\\s*\\d+)\\s*\\)\\s*";
        Pattern p = Pattern.compile("(?U)^\\s*\\(?"+simplePattern+"\\)?\\s*$");
        Matcher m = p.matcher(filterString);
        if (m.matches())
        {
            String varName = m.group(1);
            String arguments = m.group(2).replaceAll("\\s", "");
            return new SimpleFilter(varName,arguments);
        }
        return null;
    }
    private MissingFilter checkStringIsMissingFilter(String filterString) 
    {
        String pattern1 = "\\s*(\\w+)\\s*\\(\\s*\\)\\s*";
        Pattern p1 = Pattern.compile("(?U)^\\s*\\(?"+pattern1+"\\)?\\s*$");
        Matcher m1 = p1.matcher(filterString);
        if (m1.matches())
        {
            String arg = m1.group(1).replaceAll("\\s", "");
            return new MissingFilter(arg);
            
        }
        return null;
    }
    
    
    private ArithmeticFilter checkStringIsArithmeticFilter(String filterString)
    {
        
        String apattern = "\\s*([_a-zA-Zа-яА-Я]\\w*\\s*([\\+\\-]\\s*[_a-zA-Zа-яА-Я]\\w*\\s*)*)"+
                "([<>=!]{1,2})"+
                "\\s*((\\d+)\\s*|((\\d+|[_a-zA-Zа-яА-Я]\\w*)\\s*([\\+\\-]\\s*(\\d+|[_a-zA-Zа-яА-Я]\\w*)\\s*)*))";
        Pattern p = Pattern.compile("(?U)^"+apattern+"$");
        Matcher m = p.matcher(filterString);
        
        if (m.matches())
        {   
            for (int i = 0;i<m.groupCount();i++)
                System.out.println(i+": "+m.group(i));
            //Левая часть
            List<String> varList = new ArrayList<>();
            List<String> operList = new ArrayList<>();
            String vars = m.group(1);
            String[] operArray = vars.split("\\w+");
            varList = Arrays.asList(vars.split("[\\+\\-]"));
            if (operArray.length==0)
            {
                operList.add("+");
            }
            for(String operstr: operArray)
            {
                if (operstr.isEmpty()||operstr.equals("+"))
                    operList.add("+");
                else if (operstr.equals("-"))
                    operList.add("-");
            }
            
            
            
            
            String lop = m.group(3);
            String rightisnumber = m.group(4);
            String rightisvar = null;
            Double compV = null;
            if (rightisnumber!=null&&(!rightisnumber.isEmpty()))
            {
                try
                {
                    compV = Double.parseDouble(rightisnumber);
                }
                catch (NumberFormatException nfe)
                {
                    rightisvar = rightisnumber;
                    rightisnumber = null;
                }
            }
            
            
            
            try 
            {
                if (rightisnumber!=null) 
                {
                    return new ArithmeticFilter(varList,operList,compV,lop);
                }
                return new ArithmeticFilter(varList,operList,rightisvar,lop);
                
                
            } 
            catch (InvalidArithmeticFilterException ex) 
            {
                return null;
            }
        }
        return null;
    }
    
    private ArithmeticFilter checkStringIsArithmeticFilter2(String filterString)
    {
        
        String apattern = "\\s*\\(?([_a-zA-Zа-яА-Я]\\w*\\s*([\\+\\-]\\s*[_a-zA-Zа-яА-Я]\\w*\\s*)*)"+
                "([<>=!]{1,2})"+
                "(\\s*(\\d+)\\s*|\\s*([_a-zA-Zа-яА-Я]\\w*)\\s*)\\)?";
        
        
        Pattern p = Pattern.compile("^"+apattern+"$");
        String s = filterString;
        Matcher m = p.matcher(s);
        if (m.matches())
        {
            List<String> varList = new ArrayList<>();
            List<String> operList = new ArrayList<>();
            String vars = m.group(1);
            String[] operArray = vars.split("\\w+");
            varList = Arrays.asList(vars.split("[\\+\\-]"));
            if (operArray.length==0)
            {
                operList.add("+");
            }
            for(String operstr: operArray)
            {
                if (operstr.isEmpty()||operstr.equals("+"))
                    operList.add("+");
                else if (operstr.equals("-"))
                    operList.add("-");
                        
                    
            }
            
            
            
            
            String lop = m.group(3);
            String rightisnumber = m.group(4);
            String rightisvar = null;
            Double compV = null;
            if (rightisnumber!=null&&(!rightisnumber.isEmpty()))
            {
                try
                {
                    compV = Double.parseDouble(rightisnumber);
                }
                catch (NumberFormatException nfe)
                {
                    rightisvar = rightisnumber;
                    rightisnumber = null;
                }
            }
            
            
            
            try 
            {
                if (rightisnumber!=null) 
                {
                    return new ArithmeticFilter(varList,operList,compV,lop);
                }
                return new ArithmeticFilter(varList,operList,rightisvar,lop);
                
                
            } 
            catch (InvalidArithmeticFilterException ex) 
            {
                return null;
            }
        }
        return null;
    }
    
    
}
