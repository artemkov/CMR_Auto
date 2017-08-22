/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.exceptions;

/**
 *
 * @author Артем Ковалев
 */
public class VariableNotFoundException extends Exception
{
    String varName;
    String dataFileName = null;

    public VariableNotFoundException(String varName) 
    {
        this.varName = varName;
    }
    
    public VariableNotFoundException(String varName, String df) 
    {
        this.varName = varName;
        dataFileName = df;
    }
    
    @Override
    public String getMessage()
    {
        
        return "Переменная '"+varName+"' не найдена в файле данных "+(dataFileName==null?"":("'"+dataFileName+"'"));
    }
    
}
