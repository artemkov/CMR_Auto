/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import automatization.exceptions.InvalidFilterException;
import automatization.exceptions.VariableNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Артем Ковалев
 */
public class CrossReport 
{
    private String varname1;
    private String varname2;
    
    private String[] allpossvalues1;
    private String[] allpossvalues2;
    private Integer data[][];
    

    
    public CrossReport(UniqueList<Map<Content,String>> interviews, String varname1, String varname2) throws VariableNotFoundException, InvalidFilterException 
    {
        this.varname1 = varname1;
        this.varname2 = varname2;
        Set<Content> cset = interviews.getFirst().keySet();
        if (!cset.contains(new Content(varname1)))
            throw new VariableNotFoundException(varname1);
        if (!cset.contains(new Content(varname2)))
            throw new VariableNotFoundException(varname2);
        //Поиск контента
        Iterator<Content> csetiterator = cset.iterator();
        Content content1 = null;
        Content content2 = null;
        while(csetiterator.hasNext())
        {
            Content c = csetiterator.next();
            if ((content1==null)&&(c.getName().equals(varname1)))
            {
                content1 = c;
                if (content2!=null)
                    break;
            }
            if ((content2==null)&&(c.getName().equals(varname2)))
            {
                content2 = c;
                if (content1!=null)
                    break;
            }
        }
        
        TreeSet<String> uniqvalues1 = new TreeSet<>();
        for(Map<Content,String> valmap : interviews )
        {   
            String value = valmap.get(content1);
            if (value!=null)
            {
                uniqvalues1.add(value);
            }
        }
        
        TreeSet<String> uniqvalues2 = new TreeSet<>();
        for(Map<Content,String> valmap : interviews )
        {   
            String value = valmap.get(content2);
            if (value!=null)
            {
                uniqvalues2.add(value);
            }
        }
        
        Integer d[][] = distribute(interviews, uniqvalues1, uniqvalues2);
        
        
    }
    
    private class StrPair
    {
        String val1;
        String val2;

        public StrPair(String val1, String val2) {
            this.val1 = val1;
            this.val2 = val2;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + Objects.hashCode(this.val1);
            hash = 19 * hash + Objects.hashCode(this.val2);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StrPair other = (StrPair) obj;
            if (!Objects.equals(this.val1, other.val1)) {
                return false;
            }
            if (!Objects.equals(this.val2, other.val2)) {
                return false;
            }
            return true;
        }
        
    }
    
    private Integer[][] distribute (UniqueList<Map<Content,String>> interviews, Set<String> uniqvalues1, Set<String> uniqvalues2)
    {
        Integer[][] data = new Integer[uniqvalues1.size()][uniqvalues2.size()];
        allpossvalues1 = uniqvalues1.toArray(new String[uniqvalues1.size()]);
        allpossvalues2 = uniqvalues2.toArray(new String[uniqvalues2.size()]);
        Map<StrPair,Integer> valMap  = new HashMap<>();
        for (int i =0; i<uniqvalues1.size(); i++)
            for (int j =0; j<uniqvalues2.size(); j++)
            {
                data[i][j]=0;
                valMap.put(new StrPair(allpossvalues1[i],allpossvalues2[j]),0);
            }
        
        for (Map<Content,String> iview: interviews)
        {
            String v1  = iview.get(new Content(this.varname1));
            String v2  = iview.get(new Content(this.varname2));
            StrPair pair = new StrPair(v1,v2);
            Integer counter = valMap.get(pair);
            valMap.put(pair,counter+1);
        }
        
        for (int i =0; i<uniqvalues1.size(); i++)
            for (int j =0; j<uniqvalues2.size(); j++)
                data[i][j]=valMap.get(new StrPair(allpossvalues1[i],allpossvalues2[j]));
        this.data=data;
        return data;
    }

    public String[] getAllpossvalues1() {
        return allpossvalues1;
    }

    public String[] getAllpossvalues2() {
        return allpossvalues2;
    }

    public Integer[][] getData() {
        return data;
    }

    public String getVarname1() {
        return varname1;
    }

    public String getVarname2() {
        return varname2;
    }
    
    private UniqueList<Map<Content,String>> filter (UniqueList<Map<Content,String>> interviews, String var1, String val1, String var2, String val2)
    {
        UniqueList<Map<Content,String>> reslist = new UniqueList<>(); 
        for (Map<Content,String> interview: interviews)
        {
            if (interview.get(new Content(var1)).equals(val1))
                if (interview.get(new Content(var2)).equals(val2))
                    reslist.add(interview);
        }
        return reslist;
    }
    
    
}
