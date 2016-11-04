/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automatization.model;

import automatization.exceptions.InvalidArithmeticFilterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Дина
 */
public class ArithmeticFilter 
{

    
    static public enum ArithmeticLogicalOperator
    {
        more,less,notmore,notless,equal,notequal;
    }
    List<String> varNames;
    List<String> operators;
    ArithmeticLogicalOperator logicaloperator;
    double compareVal=0.0;
    //String compareVar=null;
    List<String> rightSideVarNames;
    List<String> rightSideOperators;
    
    public ArithmeticFilter(List<String> varNames, List<String> operators, List<String> rightSideVars, List<String> rightSideOperators, String lop) throws InvalidArithmeticFilterException 
    {
        this(varNames, operators , 0.0, lop);
        this.rightSideVarNames = new ArrayList<>();
        this.rightSideVarNames.addAll(rightSideVars);
        this.rightSideOperators.addAll(rightSideOperators);
    }
    
    public ArithmeticFilter(List<String> varNames, List<String> operators, String rightVar, String lop) throws InvalidArithmeticFilterException 
    {
        this(varNames, operators , 0.0, lop);
        this.rightSideVarNames = new ArrayList<>();
        this.rightSideVarNames.add(rightVar);
        this.rightSideOperators=new ArrayList<>();
        this.rightSideOperators.add("+");
    }
    
    public ArithmeticFilter(List<String> varNames, List<String> operators, double cv, String lop) throws InvalidArithmeticFilterException
    {
        this.varNames=varNames;
        this.operators=operators;
        this.compareVal=cv;
        switch (lop)
        {
            case ">":
            case "more":
                logicaloperator = ArithmeticLogicalOperator.more;
                break;
            case ">=":
            case "notless":
                logicaloperator = ArithmeticLogicalOperator.notless;
                break;
            case "<=":
            case "notmore":
                logicaloperator = ArithmeticLogicalOperator.notmore;
                break;
            case "<":
            case "less":
                logicaloperator = ArithmeticLogicalOperator.less;
                break;
            case "==":
            case "equal":
                logicaloperator = ArithmeticLogicalOperator.equal;
                break;
            case "!=":
            case "<>":
            case "notequal":
                logicaloperator = ArithmeticLogicalOperator.notequal;
                break;
            default:
                throw new InvalidArithmeticFilterException(" Invalid filter. Unknown logical operator: \""+lop+"\"");
        }
        
        
    }
    public boolean check(Map<Content, String> interview) 
    {
        
        double sum=0.0;
        int count = 0;
        if ((rightSideVarNames!=null)&&(!rightSideVarNames.isEmpty()))
        {
            Content compcontent = null;
            double rssum=0.0;
            
            for (String rscvar: rightSideVarNames)
            {
                compcontent = ContentUtils.getContentByNameFromInterview(interview, rscvar);
                
                if (compcontent!=null)
                {
                    String value = interview.get(compcontent);
                    try
                    {
                        if (rightSideOperators.get(count).equals("+"))
                            rssum+=Double.parseDouble(value);
                        if (rightSideOperators.get(count).equals("-"))
                            rssum-=Double.parseDouble(value);
                    }
                    catch (NumberFormatException nfe)
                    {
                        
                    }
                }
                else
                {
                    try
                    {
                        double pvalue = Double.parseDouble(rscvar);
                        if (rightSideOperators.get(count).equals("+"))
                            rssum+=pvalue;
                        else if (rightSideOperators.get(count).equals("-"))
                            rssum-=pvalue;
                    }
                    catch (NumberFormatException nfe)
                    {
                        
                    }
                }
                
                count++;
            }
            compareVal=rssum;
        }
        count = 0;
        for (String varName:varNames)
        {
                String operator = operators.get(count);
                Content content = null;
                content = ContentUtils.getContentByNameFromInterview(interview, varName);
                if (content!=null)
                {
                    
                    String value = interview.get(content);
                    try
                    {
                        double d = Double.parseDouble(value);
                        if (operator.equals("+"))
                            sum+=d;
                        else if (operator.equals("-"))
                            sum-=d;
                    }
                    catch (NumberFormatException e)
                    {
                    
                    }
                }
                else
                {
                    try
                    {
                        double pvalue = Double.parseDouble(varName);
                        if (operator.equals("+"))
                            sum+=pvalue;
                        else if (operator.equals("-"))
                            sum-=pvalue;
                    }
                    catch (NumberFormatException nfe)
                    {
                        
                    }
                }
            
                count++;
        }
        
        switch (logicaloperator)
        {
            case more:
                return sum>compareVal;
            case less:
                return sum<compareVal;
            case notmore:
                return sum<=compareVal;
            case notless:
                return sum>=compareVal;
            case equal:
                return sum==compareVal;
            case notequal:
                return sum!=compareVal;    
            default:
                return false;
        }
        
        
    }
    
    
}
