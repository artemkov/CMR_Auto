/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package automatization.model;

import automatization.exceptions.GroupsFileNotFoundException;
import automatization.exceptions.InvalidFilterException;
import automatization.exceptions.InvalidGroupsFileFormatException;
import automatization.exceptions.InvalidTemplateFileFormatException;
import automatization.exceptions.NoSampleDataException;
import automatization.exceptions.ReportFileNotFoundException;
import automatization.exceptions.ReportParamsNotDefinedException;
import automatization.exceptions.VariableNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Дина
 */
public interface ReportFactory 
{
    public Report makeReport (TemplateNode<String> level3node,Content content, Properties properties, List<UniqueList<Map<Content,String>>> sampleList, List<String> sampleNames)
            throws InvalidTemplateFileFormatException, ReportParamsNotDefinedException, VariableNotFoundException, InvalidFilterException, IOException, GroupsFileNotFoundException, ReportFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException ;
}
