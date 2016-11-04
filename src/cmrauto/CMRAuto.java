/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmrauto;
import com.ibm.statistics.util.*;
import automatization.exceptions.*;
import automatization.model.Content;
import automatization.model.StringIntComparator;
import automatization.model.TemplateNode;
import automatization.model.UniqueList;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.log4j.*;


/**
 *
 * @author Артем Ковалев
 */
public class CMRAuto extends Application
{
    private static final Logger log = Logger.getLogger(CMRAuto.class);
    
    private static final String propertiesFileName = "properties.properties";
    private Stage stage;
    private AnchorPane rootLayout;
    private PatternType ptype = PatternType.Type1;
    

    
    
    
    public Stage getStage() 
    {
        return stage;
    }
    
    private static Properties getProperties(String filename) throws FileNotFoundException, IOException
    {
        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(propertiesFileName);
        try 
        {
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            try 
            {
                properties.load(reader);
            } 
            finally 
            {
                reader.close();
            }
        } 
        finally 
        {
            inputStream.close();
        }
        return properties;
    }
    
    public String getProperty(String key)
    {
      try
      {
        Properties props = getProperties(propertiesFileName);
        try
        {
            return props.getProperty(key, null);
        }
        catch (IllegalArgumentException e)
        {
            log.warn("Cannot find property: "+key);
            return null;
        }
      }
      catch (IOException e)
      {
        log.warn("Cannot open property file: "+propertiesFileName);
        return null;
      }
              
    }
    
    
    
    @Override
    public void start(Stage stage) throws Exception 
    {
        
        
        Properties props = getProperties(propertiesFileName);
        log.info("Properties loaded from "+propertiesFileName);
        if (props!=null)
        {
            
            //DataHeaderPatternType
            try
            {
                if (props.containsKey("DefaultData"))
                    System.out.println(props.getProperty("DefaultData"));
                if (props.containsKey("ExcelHeaderType"))
                {
                    String ptyp = props.getProperty("ExcelHeaderType");
                }
                ptype = PatternType.valueOf(props.getProperty("ExcelHeaderType","Type1"));
            }
            catch (IllegalArgumentException e)
            {
                log.warn("Wrong pattern type value in "+propertiesFileName+" file changed to Type1");
                ptype = PatternType.Type1;
            }
            
        }
        Utility  spssutil = new Utility();
        String[] locations = spssutil.getStatisticsLocationAll();
        String latestlocation = spssutil.getStatisticsLocationLatest();
        log.info("SPSS location "+latestlocation);
        
        
        
        
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(CMRAuto.class.getResource("FXMLDocument.fxml"));
        log.info("JavaFXML document loaded");
        rootLayout = (AnchorPane) loader.load();
        
        Scene scene = new Scene(rootLayout);
        stage.setScene(scene);
        
        FXMLDocumentController controller = loader.getController();
        controller.setMain(this);
        
        this.stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        launch(args);
    }

    void process(Path dataPath, Path templatePath) throws InvalidDataFileFormatException, InvalidDataPatternType, InvalidTemplateFileFormatException, TemplateFileIOException, DataFileIOException, InvalidFilterException, VariableNotFoundException, IOException, ReportParamsNotDefinedException, ReportFileNotFoundException, GroupsFileNotFoundException, InvalidGroupsFileFormatException, NoSampleDataException 
    {
        
        
        TemplateNode<String> rootNode = TemplateProcessor.getTemplateNode(templatePath);
        log.info("Template loaded from "+templatePath.toAbsolutePath().toString());
        
        
        if (Files.isDirectory(dataPath))
        {
            List<String> sampleNames;
            //List<UniqueList<Map<Content,String>>> sampleList = DataProcessor.getSampleList(dataPath, ptype);
            Map<String,UniqueList<Map<Content,String>>> sampleMap = DataProcessor.getSampleMap(dataPath, ptype);
            log.info("Data loaded from "+dataPath.toAbsolutePath().toString());
            sampleNames=new LinkedList(sampleMap.keySet());
            
            String reportFileName = ReportProcessor.createMultiSampleReport(rootNode, sampleMap);
            log.info("Report created. See \""+reportFileName+"\"");
            
        }
        else
        {
            /*UniqueList<Map<Content,String>> interviewsList = dataPath.toString().endsWith(".xlsx")?DataProcessor.getInterviewsListFromExcel(dataPath, ptype):DataProcessor.getInterviewsFromSPSS(dataPath);
            log.info("Data loaded from "+dataPath.toAbsolutePath().toString());

            ReportProcessor.createReport(rootNode, interviewsList);
            log.info("Report created");*/
        }
        
        
        
        
    }
    
}
