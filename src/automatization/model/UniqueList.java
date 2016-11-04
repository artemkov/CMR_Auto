/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Артем Ковалев
 */
public class UniqueList<T> extends LinkedList<T> implements Serializable
{
    public UniqueList (Collection<? extends T> itemstoadd)
    {
        this();
        addAll(itemstoadd);
    }
    
    public UniqueList()
    {
    }
    
    
    @Override
    public boolean add(T t) 
    {
  	if (this.contains(t)) 
        {
             return false;
        }
        return super.add(t);
    }
    
    
    @Override
    public void add(int index, T t) 
    {
  	if (this.contains(t)) 
        {
             return;
        }
        super.add(index, t);
    }
    
    @Override
    public boolean addAll (Collection<? extends T> itemstoadd) 
    {
        Set<T> copyset = new LinkedHashSet(itemstoadd);
        copyset.removeAll(this);
        return super.addAll(copyset);
    }
    
    @Override
    public boolean addAll(int index, Collection <? extends T> itemstoadd) 
    {
        Set<T> copyset = new LinkedHashSet(itemstoadd);
        copyset.removeAll(this);
        return super.addAll(index, copyset);
    }
    
    @Override
    public void addFirst(T item)
    {
        if (this.contains(item)) 
        {
             return;
        }
        super.addFirst(item);
    }
    
    @Override
    public void addLast(T item)
    {
        if (this.contains(item)) 
        {
             return;
        }
        super.addLast(item);
    }
    
    
}
