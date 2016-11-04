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
public class InvalidArithmeticFilterException extends Exception
{
    String message;
    public InvalidArithmeticFilterException(String mes)
    {
        this.message = mes; 
    }
    public String getMessage()
    {
        return "Ошибка(и) в строке фильтра: "+message;
    }
}
