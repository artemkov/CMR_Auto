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
public class InvalidDataFileFormatException extends Exception
{
    public InvalidDataFileFormatException()
    {
        
    }
    
    public InvalidDataFileFormatException(String string) {
        System.err.println(string);
    }
    
}
