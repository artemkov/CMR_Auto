/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmrauto;

import automatization.exceptions.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Дина
 */
public class FXMLDocumentController {
    
    private Stage stage;
    private CMRAuto main;
    
    @FXML
    private Label dataLabel;
    @FXML
    private Label templateLabel;
    
    
    private String defaultDataPath;
    private String defaultTemplatePath;

    
    public void initialize(URL location, ResourceBundle resources) 
    {
        
    }

    public void setMain(CMRAuto main) 
    {
        this.main = main;
        this.stage = main.getStage();
        String asd = main.getProperty("DefaultData");
        defaultDataPath=main.getProperty("DefaultData");
        defaultTemplatePath=main.getProperty("DefaultTemplate");
        
        
    }
    
    private String convertToUTF8(String string)
    {
        try 
        {
            return new String(string.getBytes("ISO-8859-1"), "UTF-8");
        } 
        catch (UnsupportedEncodingException ex) 
        {
            System.out.println("Cannot convert string");
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return string;
    }
    
    
    
    
    
    @FXML
    private void handleSelectDataFile()
    {
        FileChooser fileChooser = new FileChooser();
        DirectoryChooser dirChooser = new DirectoryChooser();
        
        String defaultpath = System.getProperty("user.dir")+File.separator+"data";
        
        fileChooser.setInitialDirectory(new File(defaultpath));
        FileChooser.ExtensionFilter allfilter = new FileChooser.ExtensionFilter("Все", Arrays.asList(new String[]{"*.xlsx","*.sav"}));
        FileChooser.ExtensionFilter xlsfilter = new FileChooser.ExtensionFilter("Microsoft Excel 2007", Arrays.asList(new String[]{"*.xlsx"}));
        FileChooser.ExtensionFilter savfilter = new FileChooser.ExtensionFilter("IBM SPSS", Arrays.asList(new String[]{"*.sav"}));
        fileChooser.getExtensionFilters().add(allfilter);
        fileChooser.getExtensionFilters().add(xlsfilter);
        fileChooser.getExtensionFilters().add(savfilter);
        File file = fileChooser.showOpenDialog(stage);
        if (file!=null)
            dataLabel.setText(file.getAbsolutePath());
    }
    
    @FXML
    private void handleSelectDataDir()
    {
        
        DirectoryChooser dirChooser = new DirectoryChooser();
        
        String defaultpath = System.getProperty("user.dir")+File.separator+"data";
        
        dirChooser.setInitialDirectory(new File(defaultpath));
        
        File file = dirChooser.showDialog(stage);
        if (file!=null)
            dataLabel.setText(file.getAbsolutePath());
    }
    
    @FXML
    private void handleSelectTemplateFile()
    {
        FileChooser fileChooser = new FileChooser();
        String defaultpath = System.getProperty("user.dir")+File.separator+"template";
        fileChooser.setInitialDirectory(new File(defaultpath));
        FileChooser.ExtensionFilter xlsfilter = new FileChooser.ExtensionFilter("Excel 2007", Arrays.asList(new String[]{"*.xlsx"}));
        
        fileChooser.getExtensionFilters().add(xlsfilter);
        
        
        File file = fileChooser.showOpenDialog(stage);
        if (file!=null)
            templateLabel.setText(file.getAbsolutePath());
    }
    
    @FXML void handleProcessButton()
    {
        boolean validation = true;
        Path dataPath = Paths.get(dataLabel.getText());
        Path templatePath = Paths.get(templateLabel.getText());
        
        if (templateLabel.getText().isEmpty())
        {
            if (defaultTemplatePath==null)
                templatePath= Paths.get("template\\smartns_template.xlsx");
            else
                templatePath= Paths.get(defaultTemplatePath);
        }
        if (dataLabel.getText().isEmpty())
        {
            if (defaultDataPath==null)
                dataPath= Paths.get("data\\smartns.sav");
            else
                dataPath= Paths.get(defaultDataPath);
        }
        if (Files.isDirectory(dataPath))
        {
            File[] files = dataPath.toFile().listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) 
                {
                    if (pathname.toString().endsWith(".xlsx"))
                        return true;
                    if (pathname.toString().endsWith(".sav"))
                        return true;
                    if (pathname.toString().endsWith(".SAV"))
                        return true;
                    if (pathname.toString().endsWith(".XLSX"))
                        return true;
                    return false;
                }
            });
            if ((files!=null)&&(files.length>0))
                validation = true;
            else
                validation = false;
        }
        else if (!Files.isReadable(dataPath))
        {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(stage);
            alert.setTitle("Data file is not readable");
            alert.setHeaderText("Ошибка");
            alert.setContentText("Выберите читаемый файл данных");
            alert.showAndWait();
            validation = false;
        }
        if (!Files.isReadable(templatePath))
        {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(stage);
            alert.setTitle("Template file is not readable");
            alert.setHeaderText("Ошибка");
            alert.setContentText("Выберите читаемый файл c шаблоном");
            alert.showAndWait();
            validation = false;
        }
        if (validation)
        {
            try 
            {
                
                
                main.process(dataPath,templatePath);
                
            } 
            catch (InvalidDataFileFormatException ex) 
            {
                // Show the error message.
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Ошибка чтения файла данных");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                
                ex.printStackTrace();
            }
            catch (InvalidDataPatternType ex)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Неправильный тип данных");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                ex.printStackTrace();
            } 
            catch (InvalidTemplateFileFormatException ex) 
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Неправильный формат шаблона");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                ex.printStackTrace();
            } 
            catch (TemplateFileIOException ex) 
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Ошибка чтения файла шаблона");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                ex.printStackTrace();
            } 
            catch (DataFileIOException ex) 
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Ошибка ввода/вывода файла данных");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                ex.printStackTrace();
            } 
            catch (InvalidFilterException ex) 
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Ошибка в фильтре");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                ex.printStackTrace();
            } 
            catch (VariableNotFoundException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Не обнаружена переменная");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                ex.printStackTrace();
            }
            catch (ReportParamsNotDefinedException ex) 
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Не заданы необходимые параметры");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                ex.printStackTrace();
            }
            catch (NoSampleDataException ex) 
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Мало сэмплов для отчета по значимости");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                ex.printStackTrace();
            }
            catch (IOException ex) 
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Ошибка ввода/вывода");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                ex.printStackTrace();
            }
            catch (Exception ex) 
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Непредвиденная Ошибка");
                alert.setHeaderText("Ошибка");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                
                ex.printStackTrace();
            }
            
        }
    }
    
}
