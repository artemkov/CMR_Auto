/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.GroupsFileNotFoundException;
import automatization.exceptions.VariableNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.distribution.*;

/**
 *
 * @author Артем Ковалев
 */
public class DAReport 
{
    private String varname1;
    private String varname2;
    Double confLevel = 0.95;
    Double universe = 1000000.0;
    protected double nps1;
    protected double nps2;
    protected double sd1;
    protected double sd2;
    protected double tval2s;
    protected double tval1s;
    protected double minimumSigDif2s;
    protected double minimumSigDif1s;
    protected String conclusion2s;
    protected String conclusion1s;
    protected double confInterval1;
    protected double confInterval2;
    protected double effectiveTotal1;
    protected double effectiveTotal2;
    private boolean counted=false;
    private NPSReport npsrep1=null,npsrep2=null;
    private double koeff;

    public NPSReport getNpsrep1() {
        return npsrep1;
    }

    public NPSReport getNpsrep2() {
        return npsrep2;
    }
    
    DAReport (NPSReport npsreport1, NPSReport npsreport2,String var1, String var2, Double confLevel, Double universe)
    {
        if (confLevel!=null)
            this.confLevel=confLevel;
        if (universe!=null)
            this.universe=universe;
        this.npsrep1 = npsreport1;
        this.npsrep2 = npsreport2;
        this.varname1=var1;
        this.varname2=var2;
        
        if ((npsreport1.getNps()!=null)&&(npsreport2.getNps()!=null))
        {
            double gtotal1 = npsrep1.getGroupedTotal();
            double gtotal2 = npsrep2.getGroupedTotal();
            double tot = gtotal1+gtotal2;
            double top1 = npsrep1.getTops();
            double top2 = npsrep2.getTops();
            double bottom1 = npsrep1.getBottoms();
            double bottom2 = npsrep2.getBottoms();
            double passive1 = npsrep1.getPassives();
            double passive2 = npsrep2.getPassives();
            
            double ptop1 = npsrep1.getTops()*100.0/gtotal1;
            double ptop2 = npsrep2.getTops()*100.0/gtotal2;
            double pbot1 = npsrep1.getBottoms()*100.0/gtotal1;
            double pbot2 = npsrep2.getBottoms()*100.0/gtotal2;
            double ppas1 = npsrep1.getPassives()*100.0/gtotal1;
            double ppas2 = npsrep2.getPassives()*100.0/gtotal2;
            
            computeFormulas(top1,top2,bottom1,bottom2,passive1,passive2,this.confLevel,this.universe);
            testFormulasShort(top1,top2,bottom1,bottom2,passive1,passive2,this.confLevel,this.universe);
            //testFormulas(162,162,55,81,107,81,this.confLevel,this.universe);
            
            conclusion2s = Math.abs(nps1-nps2)<=minimumSigDif2s?"No difference":"Different";
            conclusion1s  = (nps1-nps2)>minimumSigDif1s?"No difference":"Different";
            
            counted=true;
        }
    }
    
    DAReport (UniqueList<Map<Content,String>> interviews1, UniqueList<Map<Content,String>> interviews2, String var1, String var2, Path groupfilepath1,Path groupfilepath2,Double confLevel, Double universe) throws VariableNotFoundException, GroupsFileNotFoundException
    {
        this.varname1=var1;
        this.varname2=var2;
        if (confLevel!=null)
            this.confLevel=confLevel;
        if (universe!=null)
            this.universe=universe;
        Set<Content> cset = interviews1.getFirst().keySet();
        if (!cset.contains(new Content(varname1)))
            throw new VariableNotFoundException(varname1);
        cset = interviews2.getFirst().keySet();
        if (!cset.contains(new Content(varname2)))
            throw new VariableNotFoundException(varname2);
        
        try 
        {
            npsrep1 = NPSReport.getAnswerGroupsWithNPSFromExcel(groupfilepath1);
            npsrep2 = NPSReport.getAnswerGroupsWithNPSFromExcel(groupfilepath2);
            
            nps1 = npsrep1.computeNPS(interviews1, varname1);
            nps2 = npsrep2.computeNPS(interviews2, varname2);
            
            double gtotal1 = npsrep1.getGroupedTotal();
            double gtotal2 = npsrep2.getGroupedTotal();
            double tot = gtotal1+gtotal2;
            double top1 = npsrep1.getTops();
            double top2 = npsrep2.getTops();
            double bottom1 = npsrep1.getBottoms();
            double bottom2 = npsrep2.getBottoms();
            double passive1 = npsrep1.getPassives();
            double passive2 = npsrep2.getPassives();
            
            double ptop1 = npsrep1.getTops()*100.0/gtotal1;
            double ptop2 = npsrep2.getTops()*100.0/gtotal2;
            double pbot1 = npsrep1.getBottoms()*100.0/gtotal1;
            double pbot2 = npsrep2.getBottoms()*100.0/gtotal2;
            double ppas1 = npsrep1.getPassives()*100.0/gtotal1;
            double ppas2 = npsrep2.getPassives()*100.0/gtotal2;
            
            
            
            computeFormulas(top1,top2,bottom1,bottom2,passive1,passive2,this.confLevel,this.universe);
            //testFormulas(top1,top2,bottom1,bottom2,passive1,passive2,this.confLevel,this.universe);
            //testFormulas(162,162,55,81,107,81,this.confLevel,this.universe);
            
            
            
            conclusion2s = Math.abs(nps1-nps2)<=minimumSigDif2s?"No difference":"Different";
            conclusion1s  = (nps1-nps2)>minimumSigDif1s?"No difference":"Different";
            
            counted=true;
        } 
        catch (IOException ex) 
        {
            throw new GroupsFileNotFoundException(groupfilepath1.toAbsolutePath().toString()+" "+groupfilepath2.toAbsolutePath().toString());
        }
        
        
    }

    public boolean isCounted() 
    {
        return counted;
    }
    private void computeFormulas(double top1, double top2, double bottom1, double bottom2, double passive1, double passive2, double conflev, double univ)
    {
        double total1  = top1+bottom1+passive1;
        double total2  = top2+bottom2+passive2;
        
        double ptop1 = top1*100.0/total1;
        double ptop2 = top2*100.0/total2;
        double pbottom1 = bottom1*100.0/total1;
        double pbottom2 = bottom2*100.0/total2;
        double ppassive1 = passive1*100.0/total1;
        double ppassive2 = passive2*100.0/total2;
        double nps1t=nps1 = (top1-bottom1)*100.0/total1;
        double nps2t=nps2 = (top2-bottom2)*100.0/total2;
        
        
        double sd1=Math.sqrt(Math.pow(100.0-nps1t,2)*ptop1/100.0+
                Math.pow(nps1t,2)*ppassive1/100.0+
                Math.pow(-100.0-nps1t,2)*pbottom1/100.0);
        double sd2=Math.sqrt(Math.pow(100.0-nps2t,2)*ptop2/100.0+
                Math.pow(nps2t,2)*ppassive2/100.0+
                Math.pow(-100.0-nps2t,2)*pbottom2/100.0);
        
        if (total1>1 && total2>1)
        {
            TDistribution tdist = new TDistribution(null,total1+total2-1,0);
            double arg1 = (1-conflev);
            double arg2 = (conflev);
            double stInv2s = computeStudentInvCumProbab2s(tdist,arg1);
            double stInv1s = computeStudentInvCumProbab1s(tdist,arg2);
        
            double mult2 = (1.0-total1/univ)*Math.sqrt((total1*Math.pow(sd1, 2)+total2*Math.pow(sd2, 2)));
            double mult3 = Math.sqrt((1.0/total1+1.0/total2)/(total1+total2-2));
            double koeff = mult2*mult3;
        
            double val2s = stInv2s*koeff;
            double val1s = stInv1s*koeff;
            
            //=B14/(ЕСЛИ(D11>0;B11^2*$B$5/D11;0)+ЕСЛИ(D12>0;B12^2*$B$5/D12;0)+ЕСЛИ(D13>0;B13^2*$B$5/D13;0))
            /*double ecoeffTops = top1>0?ptop1*ptop1*total1/top1/10000:0;
            double ecoeffPassives = passive1>0?ppassive1*ppassive1*total1/passive1/10000:0;               
            double ecoeffBottoms = bottom1>0?pbottom1*pbottom1*total1/bottom1/10000:0;
            effectiveTotal1=total1/(ecoeffTops+ecoeffPassives+ecoeffBottoms);*/
            effectiveTotal1=total1;
            /*ecoeffTops = top2>0?ptop2*ptop2*total1/top2/10000:0;
            ecoeffPassives = passive2>0?ppassive2*ppassive2*total1/passive2/10000:0;               
            ecoeffBottoms = bottom2>0?pbottom2*pbottom2*total1/bottom2/10000:0;
            effectiveTotal2=total2/(ecoeffTops+ecoeffPassives+ecoeffBottoms);*/
            effectiveTotal2=total2;
        
        
            tdist = new TDistribution(null,total1-1,0);
            double arg3 = (1-conflev);
            confInterval1=computeStudentInvCumProbab2s(tdist,arg3)*(1.0-total1/univ)*
                Math.sqrt((Math.pow(100-nps1,2)*ptop1/100+Math.pow(nps1,2)*ppassive1/100+Math.pow(-100-nps1,2)*pbottom1/100)/effectiveTotal1);
        
            tdist = new TDistribution(null,total2-1,0);
            double arg4 = (1-conflev);
            confInterval2=computeStudentInvCumProbab2s(tdist,arg4)*(1.0-total2/univ)*
                Math.sqrt((Math.pow(100-nps2,2)*ptop2/100+Math.pow(nps2,2)*ppassive2/100+Math.pow(-100-nps2,2)*pbottom2/100)/effectiveTotal2);
        
        
            tval2s=stInv2s;
            tval2s=stInv1s;
        
        
        
            this.sd1=sd1;
            this.sd2=sd2;
            this.koeff=koeff;
        
            minimumSigDif2s=val2s;
            minimumSigDif1s=val1s;
        }
    }
    private void testFormulas(double top1, double top2, double bottom1, double bottom2, double passive1, double passive2, double conflev, double univ)
    {
        double total1  = top1+bottom1+passive1;
        double total2  = top2+bottom2+passive2;
        
        double ptop1 = top1*10000.0/100/total1;
        double ptop2 = top2*10000.0/100/total2;
        double pbottom1 = bottom1*10000.0/100/total1;
        double pbottom2 = bottom2*10000.0/100/total2;
        double ppassive1 = passive1*10000.0/100/total1;
        double ppassive2 = passive2*10000.0/100/total2;
        double nps1t = ptop1-pbottom1;
        double nps2t = ptop2-pbottom2;
        
        double sd1=Math.sqrt(Math.pow(100.0-nps1t,2)*ptop1/100.0+
                Math.pow(nps1t,2)*ppassive1/100.0+
                Math.pow(-100.0-nps1t,2)*pbottom1/100.0);
        
        
        double sd2=Math.sqrt(Math.pow(100.0-nps2t,2)*ptop2/100.0+
                Math.pow(nps2t,2)*ppassive2/100.0+
                Math.pow(-100.0-nps2t,2)*pbottom2/100.0);
        if (total1>1 && total2>1)
        {
        TDistribution tdist = new TDistribution(null,total1+total2-1,0);
        double arg1 = (1-conflev);
        double arg2 = (conflev);
        double stInv2s = computeStudentInvCumProbab2s(tdist,arg1);
        double stInv1s = computeStudentInvCumProbab1s(tdist,arg2);
        
        double mult2 = (1.0-total1/univ)*Math.sqrt((total1*Math.pow(sd1, 2)+total2*Math.pow(sd2, 2)));
        double mult3 = Math.sqrt((1.0/total1+1.0/total2)/(total1+total2-2));
        double koeff = mult2*mult3;
        
        double val2s = stInv2s*koeff;
        double val1s = stInv1s*koeff;
        
        /*double ecoeffTops = top1>0?ptop1*ptop1*total1/top1/10000:0;
        double ecoeffPassives = passive1>0?ppassive1*ppassive1*total1/passive1/10000:0;               
        double ecoeffBottoms = bottom1>0?pbottom1*pbottom1*total1/bottom1/10000:0;
        double effectivetotal1=total1/(ecoeffTops+ecoeffPassives+ecoeffBottoms);*/
        double effectivetotal1=total1;
        /*ecoeffTops = top2>0?ptop2*ptop2*total1/top2/10000:0;
        ecoeffPassives = passive2>0?ppassive2*ppassive2*total1/passive2/10000:0;               
        ecoeffBottoms = bottom2>0?pbottom2*pbottom2*total1/bottom2/10000:0;
        double effectivetotal2=total2/(ecoeffTops+ecoeffPassives+ecoeffBottoms);*/
        double effectivetotal2=total2;
        
        tdist = new TDistribution(null,total1-1,0);
        double arg3 = (1-conflev);
        double coefff1=Math.sqrt((Math.pow(100-nps1t,2)*ptop1/100+Math.pow(nps1t,2)*ppassive1/100+Math.pow(-100-nps1t,2)*pbottom1/100)/effectivetotal1);
        double confInterval1=computeStudentInvCumProbab2s(tdist,arg3)*(1.0-total1/univ)*
                Math.sqrt((Math.pow(100-nps1t,2)*ptop1/100+Math.pow(nps1t,2)*ppassive1/100+Math.pow(-100-nps1t,2)*pbottom1/100)/effectivetotal1);
        
        tdist = new TDistribution(null,total2-1,0);
        double arg4 = (1-conflev);
        double coefff2=Math.sqrt((Math.pow(100-nps2t,2)*ptop2/100+Math.pow(nps2t,2)*ppassive2/100+Math.pow(-100-nps2t,2)*pbottom2/100)/effectivetotal2);
        double confInterval2=computeStudentInvCumProbab2s(tdist,arg4)*(1.0-total2/univ)*
                Math.sqrt((Math.pow(100-nps2t,2)*ptop2/100+Math.pow(nps2t,2)*ppassive2/100+Math.pow(-100-nps2t,2)*pbottom2/100)/effectivetotal2);
        
        String conclusion2st =  Math.abs(nps1t-nps2t)<=val2s?"No difference":"Different";
        String conclusion1st = (nps1t-nps2t)>val1s?"No difference":"Different";
        
        System.out.println("\n\nvarname1 = "+this.varname1);
        System.out.println("top1 = "+top1+"; ptop1 = "+ptop1);
        System.out.println("passive1 = "+passive1+"; passive1 = "+ppassive1);
        System.out.println("bottom1 = "+bottom1+"; bottom1 = "+pbottom1);
        System.out.println("total1 = "+total1);
        System.out.println("effectivetotal1 = "+effectivetotal1);
        System.out.println("nps1 = "+nps1t);
        System.out.println("sd1 = "+sd1);
        System.out.println("confInterval1 = "+confInterval1);
        System.out.println("");
        System.out.println("varname2 = "+this.varname2);
        System.out.println("top2 = "+top2+"; ptop2 = "+ptop2);
        System.out.println("passive2 = "+passive2+"; passive2 = "+ppassive2);
        System.out.println("bottom2 = "+bottom2+"; bottom2 = "+pbottom2);
        System.out.println("total2 = "+total2);
        System.out.println("effectivetotal2 = "+effectivetotal2);
        System.out.println("nps2 = "+nps2t);
        System.out.println("sd2 = "+sd2);
        System.out.println("confInterval2 = "+confInterval2);
        System.out.println("\nУровни значимости:");
        System.out.println("Tval2s = "+stInv2s);
        System.out.println("Tval1s = "+stInv1s);
        System.out.println("mult2 = "+mult2+"; mult3 = "+mult3+"; koeff = "+koeff);
        System.out.println("val2s = "+val2s+"; val1s = "+val1s);
        System.out.println("\nВыводы:");
        System.out.println("2-стор = "+conclusion2st+" NPSdelta = "+(nps1t-nps2t)+ " val2s = "+val2s);
        System.out.println("1-стор = "+conclusion1st);
        }
    }
    
    private void testFormulasShort(double top1, double top2, double bottom1, double bottom2, double passive1, double passive2, double conflev, double univ)
    {
        double total1  = top1+bottom1+passive1;
        double total2  = top2+bottom2+passive2;
        
        double ptop1 = top1*10000.0/100/total1;
        double ptop2 = top2*10000.0/100/total2;
        double pbottom1 = bottom1*10000.0/100/total1;
        double pbottom2 = bottom2*10000.0/100/total2;
        double ppassive1 = passive1*10000.0/100/total1;
        double ppassive2 = passive2*10000.0/100/total2;
        double nps1t = ptop1-pbottom1;
        double nps2t = ptop2-pbottom2;
        
        double sd1=Math.sqrt(Math.pow(100.0-nps1t,2)*ptop1/100.0+
                Math.pow(nps1t,2)*ppassive1/100.0+
                Math.pow(-100.0-nps1t,2)*pbottom1/100.0);
        
        
        double sd2=Math.sqrt(Math.pow(100.0-nps2t,2)*ptop2/100.0+
                Math.pow(nps2t,2)*ppassive2/100.0+
                Math.pow(-100.0-nps2t,2)*pbottom2/100.0);
        if (total1>1 && total2>1)
        {
        TDistribution tdist = new TDistribution(null,total1+total2-1,0);
        double arg1 = (1-conflev);
        double arg2 = (conflev);
        double stInv2s = computeStudentInvCumProbab2s(tdist,arg1);
        double stInv1s = computeStudentInvCumProbab1s(tdist,arg2);
        
        double mult2 = (1.0-total1/univ)*Math.sqrt((total1*Math.pow(sd1, 2)+total2*Math.pow(sd2, 2)));
        double mult3 = Math.sqrt((1.0/total1+1.0/total2)/(total1+total2-2));
        double koeff = mult2*mult3;
        
        double val2s = stInv2s*koeff;
        double val1s = stInv1s*koeff;
        
        /*double ecoeffTops = top1>0?ptop1*ptop1*total1/top1/10000:0;
        double ecoeffPassives = passive1>0?ppassive1*ppassive1*total1/passive1/10000:0;               
        double ecoeffBottoms = bottom1>0?pbottom1*pbottom1*total1/bottom1/10000:0;
        double effectivetotal1=total1/(ecoeffTops+ecoeffPassives+ecoeffBottoms);*/
        double effectivetotal1=total1;
        /*ecoeffTops = top2>0?ptop2*ptop2*total1/top2/10000:0;
        ecoeffPassives = passive2>0?ppassive2*ppassive2*total1/passive2/10000:0;               
        ecoeffBottoms = bottom2>0?pbottom2*pbottom2*total1/bottom2/10000:0;
        double effectivetotal2=total2/(ecoeffTops+ecoeffPassives+ecoeffBottoms);*/
        double effectivetotal2=total2;
        
        tdist = new TDistribution(null,total1-1,0);
        double arg3 = (1-conflev);
        double coefff1=Math.sqrt((Math.pow(100-nps1t,2)*ptop1/100+Math.pow(nps1t,2)*ppassive1/100+Math.pow(-100-nps1t,2)*pbottom1/100)/effectivetotal1);
        double confInterval1=computeStudentInvCumProbab2s(tdist,arg3)*(1.0-total1/univ)*
                Math.sqrt((Math.pow(100-nps1t,2)*ptop1/100+Math.pow(nps1t,2)*ppassive1/100+Math.pow(-100-nps1t,2)*pbottom1/100)/effectivetotal1);
        
        tdist = new TDistribution(null,total2-1,0);
        double arg4 = (1-conflev);
        double coefff2=Math.sqrt((Math.pow(100-nps2t,2)*ptop2/100+Math.pow(nps2t,2)*ppassive2/100+Math.pow(-100-nps2t,2)*pbottom2/100)/effectivetotal2);
        double confInterval2=computeStudentInvCumProbab2s(tdist,arg4)*(1.0-total2/univ)*
                Math.sqrt((Math.pow(100-nps2t,2)*ptop2/100+Math.pow(nps2t,2)*ppassive2/100+Math.pow(-100-nps2t,2)*pbottom2/100)/effectivetotal2);
        
        String conclusion2st =  Math.abs(nps1t-nps2t)<=val2s?"No difference":"Different";
        String conclusion1st = (nps1t-nps2t)>val1s?"No difference":"Different";
        
        System.out.println("VAR1: "+this.varname1 + " VAR2: "+this.varname2);
        System.out.println("2-стор = "+conclusion2st+" NPSdelta = "+(nps1t-nps2t)+ " val2s = "+val2s);
        System.out.println("1-стор = "+conclusion1st);
        }
    }
    
    private double computeStudentInvCumProbab2s(TDistribution tdist,double arg)
    {
        double convarg = 1-arg/2;
        return tdist.inverseCumulativeProbability(convarg);
    }
    
    private double computeStudentInvCumProbab1s(TDistribution tdist,double arg)
    {
        return tdist.inverseCumulativeProbability(arg);
    }
    
    private double computeFormulae(double tval, int totalSize1, int totalSize2, double sd1, double sd2) 
    {
        double mult1 = tval;
        double mult2 = (1.0-totalSize1/universe)*Math.sqrt((totalSize1*Math.pow(sd1, 2)+totalSize2*Math.pow(sd2, 2)));
        double mult3 = Math.sqrt((1.0/totalSize1+1.0/totalSize2)/(totalSize1+totalSize2-2));
        
        double koeff=mult2*mult3;
        
        
        return tval*(1.0-totalSize1/universe)*Math.sqrt((totalSize1*Math.pow(sd1, 2)+totalSize2*Math.pow(sd2, 2))*(1.0/totalSize1+1.0/totalSize2)/(totalSize1+totalSize2-2));
    }
    
    public double computeSDhalf(double probab, int lfreedm, double sd)
    {
        TDistribution tdist = new TDistribution(null,lfreedm,0);
        double tval = Math.abs(tdist.inverseCumulativeProbability(probab));
        return tval*(1.0-(lfreedm+1)/universe)*sd/Math.sqrt(lfreedm+1);
    }
    
    /*@Override
    public String toString()
    {
        return this.conclusion2s+" "+this.conclusion1s;
    }*/
}
