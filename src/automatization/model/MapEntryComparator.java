/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author Артем Ковалев
 */
public class MapEntryComparator implements Comparator<Map.Entry<String,String>>
{

    @Override
    public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) 
    {
        String key1 = o1.getKey();
        String key2 = o2.getKey();
        
        Comparator c = new StringIntComparator();
        
        return c.compare(key1, key2);
        
    }
    
}
