/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Дина
 */
public class Content 
{
    private String name;
    private String text;
    private UniqueList<Content> childrenList;
    private String type;
    private int firstCol;
    private int lastCol; 
    private boolean hasParent;
    private Map<String,String> answerCodeMap;
    
    public Content()
    {
        
    }
    
    public Content(String name)
    {
        this.name=name;
    }

    public boolean isHasParent() {
        return hasParent;
    }

    public void setHasParent(boolean hasParent) {
        this.hasParent = hasParent;
    }

    public Map<String, String> getAnswerCodeMap() {
        return answerCodeMap;
    }

    public void setAnswerCodeMap(Map<String, String> answerCodeMap) 
    {
        this.answerCodeMap = answerCodeMap;
    }
    
    public List<String> getCodesListFromCodeMap()
    {
        if (answerCodeMap!=null)
        {
            return new ArrayList(answerCodeMap.keySet());
        }
        return null;
    }
    
    public String getAnswerText(String code)
    {
        if (answerCodeMap!=null)
            return answerCodeMap.get(code);
        else
            return null;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getFirstCol() {
        return firstCol;
    }

    public void setFirstCol(int firstCol) {
        this.firstCol = firstCol;
    }

    public int getLastCol() {
        return lastCol;
    }

    public void setLastCol(int lastCol) {
        this.lastCol = lastCol;
    }

    public boolean hasparent() {
        return hasParent;
    }

    public void setHasparent(boolean hasParent) {
        this.hasParent = hasParent;
    }

    public UniqueList<Content> getChildrenList() {
        return childrenList;
    }

    public void setChildrenList(UniqueList<Content> childrenList) {
        this.childrenList = childrenList;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
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
        final Content other = (Content) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Content{" + "name=" + name + ", type=" + type + '}';
    }
    
    
}
