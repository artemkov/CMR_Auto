/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.GroupsFileNotFoundException;
import automatization.exceptions.InvalidFilterException;
import automatization.exceptions.InvalidTemplateFileFormatException;
import automatization.exceptions.ReportFileNotFoundException;
import automatization.exceptions.ReportParamsNotDefinedException;
import automatization.exceptions.VariableNotFoundException;
import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.math3.distribution.TDistribution;

/**
 *
 * @author Артем Ковалев
 */
public class ReportUtils 
{
    static final double Zc = 1.96;
    public static Color getColorFromDiff(Double diff)
    {
        if ((diff==null)||(diff==0.0))
        {
            return Color.BLACK;
        }
        
        else if (diff<0.0)
            return Color.BLUE;
        else
            return Color.RED;
        
    }
    public static Double getNormDAVal (double part1, double part2, double ssize1, double ssize2)
    {
        if (ssize1==0||ssize2==0)
            return null;
        double x1 = part1*ssize1/100.0;
        double x2 = part2*ssize2/100.0;
        double p = (x1+x2)/(ssize1+ssize2);
        double q = 1-p;
        if ((p==0.0)||(q==0.0))
            return null;
        double diff = (part1-part2)/100.0;
        double denominator = Math.sqrt(p*q*(1/ssize1+1/ssize2));
        double z = Math.abs(diff/denominator);
        
        return z>Zc?diff:null;  
    }
    public static Double getStudentDAVal (double mean1, double mean2, double semean1, double semean2, double confinterval,double numberofseries,int numberofcomaprissons)
    {
        if ((semean1==0)||(semean2==0)||numberofseries<2)
            return null;
        double cval1 = (mean1-mean2)/(Math.sqrt(Math.pow(semean1, 2)+Math.pow(semean2, 2)));
        double student = computeStudentInvCumProbab2s(new TDistribution(null, numberofseries-2, 0.00),1-(1-confinterval)/(numberofcomaprissons-1));
        if (Math.abs(cval1)>student)
            return cval1;
        return null;
    }
    static Double[] getStudentDAValDebug (double mean1, double mean2, double semean1, double semean2, double confinterval,double numberofseries,int numberofcomaprissons)
    {
        if ((semean1==0)||(semean2==0)||numberofseries<2)
            return null;
        double cval1 = (mean1-mean2)/(Math.sqrt(Math.pow(semean1, 2)+Math.pow(semean2, 2)));
        double student = computeStudentInvCumProbab2s(new TDistribution(null, numberofseries-2, 0.00),1-(1-confinterval)/(numberofcomaprissons-1));
        Double[] res = new Double[2];
        res[0]=cval1;
        res[1]=student;
        return res;
    }
    public static Double getStudentDAVal (double mean1, double mean2, double semean1, double semean2, double confinterval,double numberofseries)
    {
        if ((semean1==0)||(semean2==0)||numberofseries<2)
            return null;
        double cval1 = (mean1-mean2)/(Math.sqrt(Math.pow(semean1, 2)+Math.pow(semean2, 2)));
        double student = computeStudentInvCumProbab2s(new TDistribution(null, numberofseries-1, 0.00),confinterval);
        if (Math.abs(cval1)>student)
            return cval1;
        return null;
    }
    private static double computeStudentInvCumProbab2s(TDistribution tdist,double arg)
    {
        double convarg = 1-(1-arg)/2;
        return tdist.inverseCumulativeProbability(convarg);
    }
    
    public Report findReportByTagFromReportCol(Collection<Report> repcol, String tag)
    {
        for(Report r: repcol)
        {
            if(r.getTag().equals(tag))
            {
                return r;
            }
        }
        return null;
    }
    
    public static TemplateNode<String> getReportNodeByTag (TemplateNode<String> root, String tag)
    {
        if (root.getParent()!=null)
            return null;
        for (TemplateNode<String> templ1: root.getChildren())
        {
            for (TemplateNode<String> templ2: templ1.getChildren())
            {
                for (TemplateNode<String> templ3: templ2.getChildren())
                {
                    for (TemplateNode<String> templ4: templ3.getChildren())
                    {
                        String arr[] = templ4.getData().split("=",2);
                        
                        String key = arr[0].trim();
                        String val = arr[1].trim();
                        
                        if (val.equals(tag))
                            if (key.equals("tag")||key.equals("Tag"))
                                return templ3;
                    }
                }
            }
        }
        return null;
    }
    public static Double round (Number value, int digitac)
    {
        double k1 = Math.pow(10, digitac);
        long temp = Math.round((Double)value*k1);
        return temp/k1;
    }
    
    public static Properties mergeProperties (Properties p1, Properties p2)
    {
        
        if (p2==null)
        {
            if (p1==null)
                return null;
            else
                return p1;
        }
        else
        {
            if (p1==null)
                return p2;
        }
        
        for (String key: p1.stringPropertyNames())
        {
            if (!p2.containsKey(key))
            {
                p2.setProperty(key, p1.getProperty(key));
            }
        }
        return p2;
    }
    
    public static double getDoubleFromProperties (String key, double default_value, Properties properties)
    {
        if (properties.containsKey(key))
        {
            try
            {
                Double un = Double.parseDouble(properties.getProperty(key));
                return un;
            }
            catch (NumberFormatException e)
            {
                        
            }
        }
        return default_value;
    }
    
    public static String getStringFromProperties (String key, String default_value, Properties properties)
    {
        return properties.getProperty(key, default_value);
    }
    public static boolean getBooleanFromProperties (String key, boolean default_value, Properties properties)
    {
        if (properties.containsKey(key))
        {
            if (properties.getProperty(key).equalsIgnoreCase("true"))
            {
                return true;
            }
            else if (properties.getProperty(key).equalsIgnoreCase("false"))
            {
                return false;
            }
                
        }
        return default_value;
    }
    
    
}


