/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.exceptions;

/**
 *
 * @author Дина
 */
public class ReportParamsNotDefinedException extends Exception
{
    public ReportParamsNotDefinedException () 
    {
        
    }
    
    public ReportParamsNotDefinedException (String string) 
    {
        System.err.println(string);
    }
}
