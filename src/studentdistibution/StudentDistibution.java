/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package studentdistibution;
import org.apache.commons.math3.distribution.*;
/**
 *
 * @author Артем Ковалев
 */
public class StudentDistibution {
    public static final Double getStudentDAVal2 (double mean1, double mean2, double dispers1, double dispers2, double weight1, double weight2,
            double error_level, int numberofcomparissons)
    {
        if (weight1==0||weight2==0||(weight1+weight2<=2.0))
            return null;
        double ood = getOOD(weight1, weight2, dispers1, dispers2);
        double t = (mean1-mean2)/Math.sqrt(ood/weight1+ood/weight2);
        double student = inverseCumulativeProbability2s(weight1+weight2-2,error_level/(numberofcomparissons-1));
        if (Math.abs(t)>student)
            return t;
        return null;
    }
    
    public static final double getOOD(double weight1, double weight2, double dispers1, double dispers2)
    {
        if (!(weight1==0||weight2==0||(weight1+weight2<=2.0)))
            return ((weight1-1)*dispers1+(weight2-1)*dispers2)/(weight1+weight2-2);
        return Double.NaN;
    }

    static double inverseCumulativeProbability2s(double freedom_degrees, double alpha)
    {
        double arg =1-alpha/2;
        return (new TDistribution(null, freedom_degrees , 0.0)).inverseCumulativeProbability(arg);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        int n=200;
        double val001=inverseCumulativeProbability2s(n,0.001);
        double val01=inverseCumulativeProbability2s(n,0.01);
        double val05=inverseCumulativeProbability2s(n,0.05);
        System.out.println("St2s("+n+",0.05) = "+val05);
        System.out.println("St2s("+n+",0.01) = "+val01);
        System.out.println("St2s("+n+",0.001) = "+val001);
        
        System.out.println("");
        
        double alpha = 0.05;
        int number_of_comparissons=6;
        System.out.println("Уровень ошибки: "+alpha+" Число операторов: "+(number_of_comparissons-1));
        
        double size1 = 300.0;
        double mean1 = 7.12;
        double var1= 2.686;
        System.out.println("Выборка1:");
        System.out.println("n="+size1+"; mean="+mean1+"; variance="+var1+"; disperse="+var1*var1);
        
        
        double size2 = 100.0;
        double mean2 = 7.903;
        double var2= 2.324;
        System.out.println("Выборка2:");
        System.out.println("n="+size2+"; mean="+mean2+"; variance="+var2+"; disperse="+var2*var2);
        
        System.out.println("");
        System.out.println("Mean delta: "+Math.abs(mean1-mean2));
        double ood = getOOD(size1, size2, var1*var1, var2*var2 );
        System.out.println("OOD: "+ood);
        System.out.println("OOD/n1: "+ood/size1);
        System.out.println("OOD/n2: "+ood/size2);
        System.out.println("Znamenatel: sqrt(OOD/n1+OOD/n2)"+Math.sqrt(ood/size1+ood/size2));
        System.out.println("Student t criterium (Mean_delta/Znamenatel): t="+Math.abs((mean1-mean2)/Math.sqrt(ood/size1+ood/size2)));
        System.out.println("Student probability (with Bonferroni) " +"a="+alpha/number_of_comparissons+"; freedom="+(size1+size2-2)+": St(a,n)="+inverseCumulativeProbability2s(size1+size2-2,alpha/number_of_comparissons));
        System.out.println("Student probability (without Bonferroni) "+"a="+alpha+"; freedom="+(size1+size2-2)+": St(a,n)="+inverseCumulativeProbability2s(size1+size2-2,alpha));
        
        
        
        double res_nonbonferroni = Math.abs(getStudentDAVal2(mean1, mean2, var1*var1, var2*var2, size1, size2, alpha, 2));
        
        double res_bonferroni = Math.abs(getStudentDAVal2(mean1, mean2, var1*var1, var2*var2, size1, size2, alpha, number_of_comparissons));
        System.out.println("");
        System.out.println("Results:");
        String str_withbon = res_nonbonferroni>inverseCumulativeProbability2s(size1+size2-2,alpha/number_of_comparissons)?"Изменения значимы t>St(a,n)":"Изменения НЕ значимы t<=St(a,n)";
        System.out.println("With Bonferroni: "+res_bonferroni+" "+str_withbon);
        String str_withoutbon = res_nonbonferroni>inverseCumulativeProbability2s(size1+size2-2,alpha)?"Изменения значимы t>St(a,n)":"Изменения НЕ значимы t<=St(a,n)";
        System.out.println("Without Bonferroni: "+res_nonbonferroni+" "+str_withoutbon);
       
    }
    
}
