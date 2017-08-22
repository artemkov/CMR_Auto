/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automatization.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Дина
 */
public class NormDAReport 
{
    private List<Color> colList;
    public NormDAReport (List<Double> values1, List<Double> values2, List<Double> ssize1, List<Double> ssize2)
    {
        colList = new ArrayList<>();
        for (int i=0; i<Math.min(values1.size(), values2.size());i++)
        {
            Double val1 = values1.get(i);
            Double val2 = values2.get(i);
            Double ss1 = ssize1.get(i);
            Double ss2 = ssize2.get(i);
            if ((val1<=100.0)&&(val2<=100.0))
            {
                
                Double diff = ReportUtils.getNormDAVal(val1, val2, ss1, ss2);
                if ((diff==null)||(diff==0.0))
                {
                    
                    colList.add(Color.BLACK);
                }
                else if (diff>0.0)
                {
                    
                    colList.add(Color.BLUE);
                }
                else 
                {
                    colList.add(Color.RED);
                }
            }
        }
    }

    public List<Color> getColList() 
    {
        return colList;
    }
    
}
