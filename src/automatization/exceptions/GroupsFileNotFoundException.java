/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.exceptions;

import java.io.IOException;

/**
 *
 * @author Дина
 */
public class GroupsFileNotFoundException extends IOException
{
    String filename;
    public GroupsFileNotFoundException(String filename)
    {
        this.filename=filename;
    }
    public String getMessage()
    {
        return "Файл(ы) "+filename+" не найден(ы)";
    }
}
