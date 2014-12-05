package org.hpccsystems.dashboard.controller; 

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.utils.XMLConverter;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.ChartConfiguration;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.entity.TreeConfiguration;
import org.hpccsystems.dashboard.entity.XYConfiguration;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.ChartService;
import org.zkoss.image.Image;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Textbox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * PluginConfigurationController is responsible for handle the adding new widget plugins and
 * controller class for plugin_config.zul.
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class PluginConfigurationController extends SelectorComposer<Panel> {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(PluginConfigurationController.class);

    @Wire
    private Fileupload imageId;
    @Wire
    private Fileupload javascriptId;
    @Wire
    private Fileupload dependentjsId;
    @Wire
    private Fileupload cssFileId;
    @Wire
    private Textbox name;
    @Wire
    private Textbox description;
    @Wire
    private Textbox jsFunctionName;
    @Wire
    private Combobox category;
    @Wire
    private Checkbox groupingCheckbox;
    @Wire
    private Checkbox filterCheckbox;
    @Wire
    private Intbox minLevel;
    @Wire
    private Intbox maxLevel;
    @Wire
    private Hbox xyConfig;
    @Wire
    private Hbox treeConfig;
    @Wire
    private Hbox googleChartHbox;
    @Wire
    private Hbox googlePackages;
    @Wire
    private Combobox exsistingJsList;
    @Wire
    private Label dataModelLabel;
    @Wire
    private Button dataModelHelp;
    @Wire
    private Hbox addedJsDiv;
    
    @WireVariable
    ChartService chartService;
    @WireVariable
    private AuthenticationService authenticationService;

    private ChartConfiguration configuration;
    
    private static final String IMAGE_SAVE_PATH = Executions.getCurrent()
            .getDesktop().getWebApp().getRealPath("/demo/chart/");

    private static final String JAVASCRIPT_SAVE_PATH = Executions.getCurrent()
            .getDesktop().getWebApp().getRealPath("/demo/js/");

    private static final String DEPENDENT_JS_SAVE_PATH = Executions
            .getCurrent().getDesktop().getWebApp().getRealPath("/demo/js/lib/");

    private static final String CSS_SAVE_PATH = Executions.getCurrent()
            .getDesktop().getWebApp().getRealPath("/demo/css/");
    
    private List<String> existingDependency;
    
    @Override
    public void doAfterCompose(Panel comp) throws Exception {
        super.doAfterCompose(comp);        
        
        Comboitem comboItem = new Comboitem();        
        comboItem.setValue(Constants.CATEGORY_PIE);
        comboItem.setLabel("Pie Charts");
        category.appendChild(comboItem);
        
        comboItem = new Comboitem();
        comboItem.setValue(Constants.CATEGORY_XY_CHART);
        comboItem.setLabel("XY Charts");
        category.appendChild(comboItem);
        
        comboItem = new Comboitem();
        comboItem.setValue(Constants.CATEGORY_HIERARCHY);
        comboItem.setLabel("Hierarchy");
        category.appendChild(comboItem);
        
        //On Close Event
        this.getSelf().addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

            @Override
            public void onEvent(Event arg0) throws Exception {
                Include include = (Include) PluginConfigurationController.this.getSelf().getParent();
                include.setSrc(null);
            }
            
        });
        
        File dependentJsDirectory = new File(DEPENDENT_JS_SAVE_PATH);
        List<File> files = new ArrayList<File>(Arrays.asList(dependentJsDirectory.listFiles()));
        List<String> jsList = new ArrayList<String>();
        for (File file : files) {
            jsList.add(file.getName());
        }
        //adding google chart Api Name
        jsList.add(Constants.GOOGLE_CHART_API);
        
        ListModelList<String> modelList = new ListModelList<String>(jsList);
        exsistingJsList.setModel(modelList);
        existingDependency = new ArrayList<String>();
        
        this.getSelf().focus();
    }
    
    private Div createUploadLabel(String name) {
        final Div div = new Div();
        div.setZclass("uploaded-label");
        final Label label = new Label();
        label.setValue(name);
        div.appendChild(label);        
        return div;
    }
    
    @Listen("onSelect = #exsistingJsList")
    public void onDependencySelect(SelectEvent<Component, Object> event) {
        Comboitem comboitem = (Comboitem) event.getSelectedItems().iterator().next();
        addedJsDiv.appendChild(createUploadLabel(comboitem.getLabel()));
        
        if(Constants.GOOGLE_CHART_API.equalsIgnoreCase(comboitem.getLabel())){
            googleChartHbox.setVisible(true);
        }else{
            googleChartHbox.setVisible(false);
            StringBuilder builder = new StringBuilder("js/lib/").append(comboitem.getLabel());
            existingDependency.add(builder.toString());    
        }
        exsistingJsList.setSelectedItem(null);
        comboitem.detach();
    }
    
    @Listen("onUpload = #imageId")
    public void uploadImage(final UploadEvent uploadEvent) {
        final Object media = uploadEvent.getMedia();
        String fullFileName = uploadEvent.getMedia().getName();
        final File file = new File(IMAGE_SAVE_PATH);
        //Checks for the existing Image
        if(!Arrays.asList(file.list()).contains(fullFileName)){
            imageId.setAttribute("Media", media);
            if (media instanceof Image) {
                org.zkoss.zul.Image image = new org.zkoss.zul.Image();
                image.setContent((Image) media);
                uploadEvent.getTarget().getParent().insertBefore(image, uploadEvent.getTarget());
                
                imageId.setVisible(false);
            } else if (media != null) {
                Clients.showNotification(Labels.getLabel("notanImage"), "error",imageId, "middle_center", 3000, true);
            }
        }else{
            Clients.showNotification("Image exists.Upload different image","error", imageId, "end_center", 3000, true);
        }
        
    }

    @Listen("onUpload = #javascriptId")
    public void uploadJSFile(final UploadEvent uploadEvent) {
        final Object media = uploadEvent.getMedia();
        String fullFileName = uploadEvent.getMedia().getName();
        final File file = new File(JAVASCRIPT_SAVE_PATH);
        //Checks for the existing file
        if(!Arrays.asList(file.list()).contains(fullFileName)){
            int dot = fullFileName.lastIndexOf(".");
            final String fileName = fullFileName.substring(dot + 1);
            javascriptId.setAttribute("Media", media);
            if ("js".equals(fileName)) {
                uploadEvent.getTarget().getParent().insertBefore(createUploadLabel(fullFileName), uploadEvent.getTarget());
                javascriptId.setDisabled(true);
            } else if (media != null) {
                Clients.showNotification(Labels.getLabel("notanJavascriptFile"),"error", javascriptId, "middle_center", 3000, true);
            }
        
        }else{
            Clients.showNotification("File exists.Upload different file","error", javascriptId, "end_center", 3000, true);
        }
        
    }

    @Listen("onUpload = #dependentjsId")
    public void uploaddependentJSFile(final UploadEvent uploadEvent) {
        final Media media = uploadEvent.getMedia();
        final String fullFileName = uploadEvent.getMedia().getName();
        final File file = new File(DEPENDENT_JS_SAVE_PATH);
        //Checks for the existing file
        if(!Arrays.asList(file.list()).contains(fullFileName)){
            final int dot = fullFileName.lastIndexOf(".");
            final String fileName = fullFileName.substring(dot + 1);
            if ("js".equals(fileName)) {
                uploadEvent.getTarget().getParent().insertBefore(createUploadLabel(fullFileName), uploadEvent.getTarget());
                if (uploadEvent.getTarget().getAttribute("Media") != null) {
                    @SuppressWarnings("unchecked")
                    List<Media> medias = (List<Media>) uploadEvent.getTarget().getAttribute("Media");
                    medias.add(media);
                } else {
                    final List<Media> medias = new ArrayList<Media>();
                    medias.add(media);
                    uploadEvent.getTarget().setAttribute("Media", medias);
                }
            } else if (media != null) {
                Clients.showNotification(Labels.getLabel("notanJavascriptFile"),"error", dependentjsId, "middle_center", 3000, true);
            }
        }else{
            Clients.showNotification("File exists.Upload different file","error", dependentjsId, "end_center", 3000, true);
        }
        
    }

    @Listen("onUpload = #cssFileId")
    public void uploadCssFile(final UploadEvent uploadEvent) {
        final Media media = uploadEvent.getMedia();
        final String fullFileName = uploadEvent.getMedia().getName();
        final File file = new File(CSS_SAVE_PATH);
        //Checks for the existing file
        if(!Arrays.asList(file.list()).contains(fullFileName)){
            final int dot = fullFileName.lastIndexOf(".");
            final String fileName = fullFileName.substring(dot + 1);
            if ("css".equals(fileName)) {
                uploadEvent.getTarget().getParent().insertBefore(createUploadLabel(fileName), uploadEvent.getTarget());
                if (uploadEvent.getTarget().getAttribute("Media") != null) {
                    @SuppressWarnings("unchecked")
                    List<Media> medias = (List<Media>) uploadEvent.getTarget().getAttribute("Media");
                    medias.add(media);
                } else {
                    List<Media> medias = new ArrayList<Media>();
                    medias.add(media);
                    uploadEvent.getTarget().setAttribute("Media", medias);
                }
            } else if (media != null) {
                Clients.showNotification(Labels.getLabel("notanCSSFile"), "error",cssFileId, "middle_center", 3000, true);
            }
        }else{
            Clients.showNotification("File exists.Upload different file","error", cssFileId, "end_center", 3000, true);
        }
        
    }

    @Listen("onClick = #submitBtn")
    public void submit() throws WrongValueException, Exception {
        boolean isValidData = validateConfigData();    
        int newPluginId = 0;
        try {
            if (isValidData) {                
                int selectedCategory = this.category.getSelectedItem().getValue();
                String chartName = name.getValue();
                String chartDescription = this.description.getValue();
                configuration.setFunctionName(jsFunctionName.getText());
                String configXMLData = null;
                
                if (Constants.CATEGORY_XY_CHART == selectedCategory    || Constants.CATEGORY_PIE == selectedCategory) {
                    setXYChartData();
                    getConfigFiles();
                    configXMLData = XMLConverter.makeXYConfigurationXML((XYConfiguration) configuration);

                } else if (Constants.CATEGORY_HIERARCHY == selectedCategory) {                    
                    setTreeData();
                    getConfigFiles();
                    configXMLData = XMLConverter.makeTreeConfigurationXML((TreeConfiguration) configuration);
                }
                configuration.setEnableFilter(filterCheckbox.isChecked());
                
                if (configuration.getJsURL() != null && configuration.getImageURL() != null) {                    
                    // updating plugin chart config data into DB
                    newPluginId = chartService.addPlugin(chartName, chartDescription,configXMLData, authenticationService
                                    .getUserCredential().getUserId(), selectedCategory,true);
                    Clients.showNotification("The Plugin is added to the application sucessfully", "info",getSelf(), "middle_center", 3000, false);                        
                    
                    //Sending event to update list of plugins
                    ChartDetails chartDetails = new ChartDetails();
                    chartDetails.setCategory(selectedCategory);
                    chartDetails.setConfiguration(configuration);
                    chartDetails.setDescription(chartDescription);
                    chartDetails.setId(newPluginId);
                    chartDetails.setIsPlugin(true);
                    chartDetails.setName(chartName);
                    Events.sendEvent("onPluginAdded", this.getSelf().getParent().getParent().getParent(), chartDetails);
                    
                    //Closing the panel
                    Events.sendEvent(Events.ON_CLOSE, this.getSelf(), null);
                }
            }
        }catch(Exception ex){
            LOG.error(Constants.EXCEPTION, ex);
        }
        
    }
    
    /**
     * Validates category & chart name,description etc
     * @return boolean
     * @throws Exception 
     * @throws WrongValueException 
     */
    private boolean validateConfigData() throws WrongValueException, Exception {
        boolean isValidData = true;
        boolean packageSelected = false;
        if(name.getValue() == null || name.getValue().isEmpty() ){            
            Clients.showNotification("Define Chart name","error", name, "end_center", 3000, true);
            isValidData = false;             
        }else{
            for(Entry<Integer, ChartDetails> entry: chartService.getCharts().entrySet()){
                if(!name.getValue().equalsIgnoreCase(entry.getValue().getName())){
                    continue;
                }else{
                    Clients.showNotification("Chart name exists","error", name, "end_center", 3000, true);
                    isValidData = false;
                }
            }
        }
        if(jsFunctionName.getValue() == null || jsFunctionName.getValue().isEmpty()){
            Clients.showNotification("Provide Function name","error", description, "end_center", 3000, true);
            isValidData = false;
        }
        if(description.getValue() == null || description.getValue().isEmpty()){
            Clients.showNotification("Define Chart Description","error", description, "end_center", 3000, true);
            isValidData = false;
        }
        if(this.category.getSelectedItem() == null){
            Clients.showNotification("Select Chart Category","error", category, "end_center", 3000, true);
            isValidData = false;
        }else{                
            if(Constants.CATEGORY_HIERARCHY == (int)this.category.getSelectedItem().getValue()){
                if(minLevel == null){
                    Clients.showNotification("Define minimum hierarchy level","error", minLevel, "end_center", 3000, true);
                }
                if( maxLevel == null ){
                    Clients.showNotification("Define maximum hierarchy level","error", maxLevel, "end_center", 3000, true);            
                }
            }
            
        }
        if(googleChartHbox.isVisible()){
            for (Component componenet : googlePackages.getChildren()) {
                Checkbox checkbox = (Checkbox)componenet;
                if(checkbox.isChecked()){
                    packageSelected = true;
                    break; }            
            }
            if(!packageSelected){
                Clients.showNotification("Select Google package","error", googlePackages, "end_center", 3000, true);
                isValidData = false;
            }
        }
        
        return isValidData;
    }


    /**
     * Gets the configuration files(Js,Css,Image) and set them to 
     * Configuration object
     * 
     */
    private void getConfigFiles(){
        imageUpload();
        javascriptUpload();
        if(googleChartHbox.isVisible()){
            getGooglePackages();
            configuration.setDependentJsURL(null);
        }else{
            dependentJSUpload();
            configuration.setGooglePackages(null);
        }
        cssUpload();
    }

    /**
     * Gets the selected google packages and set to Configuration object
     */
    private void getGooglePackages() {
        List<String> packageList = new ArrayList<String>();
        for (Component componenet : googlePackages.getChildren()) {
            Checkbox checkbox = (Checkbox)componenet;
            if(checkbox.isChecked()){
                packageList.add(checkbox.getLabel());
            }            
        }
        configuration.setGooglePackages(packageList);
    }

    /**
     * Sets Tree structure config data
     * @param treeConfiguration
     */
    private void setTreeData() {
        ((TreeConfiguration)configuration).setMaxLevels(maxLevel.getValue());
        ((TreeConfiguration)configuration).setMinLevels(minLevel.getValue());    
    }
    /**
     * Sets XY chart config data
     * @param XYConfiguration
     */
    private void setXYChartData() {       
       
        ((XYConfiguration)configuration).setEnableXGrouping(groupingCheckbox.isChecked());
    }

    @Listen("onChange = #category")
    public void onCategoryChange(){
        
        int selectedCategory = this.category.getSelectedItem().getValue();
        
        dataModelHelp.setVisible(true);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = null;
        String jsonString = null;
        
        if(Constants.CATEGORY_XY_CHART == selectedCategory){    
            configuration = new XYConfiguration();
            xyConfig.setVisible(true);
            groupingCheckbox.setChecked(false);
            treeConfig.setVisible(false);
            
            je = jp.parse(Labels.getLabel("jsonXY"));
            jsonString = gson.toJson(je);
            dataModelLabel.setValue(jsonString);
            
        } else if(Constants.CATEGORY_HIERARCHY == selectedCategory){
            configuration = new TreeConfiguration();
            treeConfig.setVisible(true);
            xyConfig.setVisible(false);
            maxLevel.setValue(0);
            minLevel.setValue(2);
            
            je = jp.parse(Labels.getLabel("jsonHierarchy"));
            jsonString = gson.toJson(je);
            dataModelLabel.setValue(jsonString);
            
        }else if(Constants.CATEGORY_PIE == selectedCategory){            
            configuration = new XYConfiguration();
            xyConfig.setVisible(true);
            groupingCheckbox.setChecked(false);
            treeConfig.setVisible(false);
            
            je = jp.parse(Labels.getLabel("jsonPie"));
            jsonString = gson.toJson(je);
            dataModelLabel.setValue(jsonString);
        }
    }
    
    @Listen("onChange=#minLevel; onChange=#maxLevel;")
    public void onChangeLevel(Event event) throws Exception{
        if(minLevel.getValue() < 2){
            minLevel.setValue(0);
            Clients.showNotification("Minimum level should be greater than 2","error", minLevel, "end_center", 3000, true);
        }
        if( maxLevel.getValue() < 2){
            maxLevel.setValue(0);
            Clients.showNotification("Maximum level should be greater than 2","error", maxLevel, "end_center", 3000, true);            
        }
    }
    /**
     * Loads static image of the chart
     */
    private void imageUpload() {
        final Media media = (Media) imageId.getAttribute("Media");
            BufferedInputStream inputStream = null;
            BufferedOutputStream outputStream = null;
            InputStream fin =null;
            OutputStream fout =null;
            StringBuilder builder = null;
            try {
                if(media != null){
                fin = media.getStreamData();
                inputStream = new BufferedInputStream(fin);
                final File baseDir = new File(IMAGE_SAVE_PATH);
                if (!baseDir.exists()) {
                    baseDir.mkdirs();
                }
                builder = new StringBuilder();
                builder.append("chart/").append(media.getName());
                configuration.setImageURL(builder.toString());
                final File file = new File(IMAGE_SAVE_PATH + "/" + media.getName());
                fout = new FileOutputStream(file);
                outputStream = new BufferedOutputStream(fout);
                final byte buffer[] = new byte[1024];
                int character = inputStream.read(buffer);
                while (character != -1) {
                    outputStream.write(buffer, 0, character);
                    character = inputStream.read(buffer);
                }
                }else{
                    Clients.showNotification("Upload Image file","error", imageId, "end_center", 3000, true);
                }
            } catch (IOException exception) {
                Clients.showNotification(Labels.getLabel("unabletoUploadFile"));
                LOG.error(Labels.getLabel("exceptionWhileUploadingFile"), exception);
            } finally {
                finallyBlock(inputStream, outputStream, fin, fout);
            }
    }

    /**
     * Loads script file for the plugin chart
     */
    private void javascriptUpload() {
        final Media media = (Media) javascriptId.getAttribute("Media");
            BufferedInputStream inputStream = null;
            BufferedOutputStream outputStream = null;
            InputStream fin =null;
            OutputStream fout =null;
            StringBuilder builder = null;
            try {
                if(media != null){
                fin = media.getStreamData();
                inputStream = new BufferedInputStream(fin);
                final File baseDir = new File(JAVASCRIPT_SAVE_PATH);
                if (!baseDir.exists()) {
                    baseDir.mkdirs();
                }
                builder = new StringBuilder();
                builder.append("js/").append(media.getName());
                configuration.setJsURL(builder.toString());
                
                final File file = new File(JAVASCRIPT_SAVE_PATH    + "/" + media.getName());
                fout = new FileOutputStream(file);
                outputStream = new BufferedOutputStream(fout);
                final byte buffer[] = new byte[1024];
                int character = inputStream.read(buffer);
                while (character != -1) {
                    outputStream.write(buffer, 0, character);
                    character = inputStream.read(buffer);
                }
                }else{
                Clients.showNotification("Upload JS file","error", javascriptId, "end_center", 3000, true);
                }
            } catch (IOException exception) {
                Clients.showNotification(Labels.getLabel("unabletoUploadFile"));
                LOG.error(Constants.EXCEPTION, exception);
            } finally {
                finallyBlock(inputStream, outputStream, fin, fout);
            }
    }

    /**
     * Loads dependent Java scripts for the plugin chart
     */
    private void dependentJSUpload() {
        @SuppressWarnings("unchecked")
        final List<Media> medias = (List<Media>) dependentjsId.getAttribute("Media");
        List<String> dependentJsURL = null;
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        InputStream fin =null;
        OutputStream fout =null;
        StringBuilder builder = null;
        try {
            if(medias != null){
                dependentJsURL = new ArrayList<String>();
            for (Media media : medias) {            
                fin = media.getStreamData();
                inputStream = new BufferedInputStream(fin);
                final File baseDir = new File(DEPENDENT_JS_SAVE_PATH);
                if (!baseDir.exists()) {
                    baseDir.mkdirs();
                }
                builder = new StringBuilder();
                builder.append("js/lib/").append(media.getName());
                dependentJsURL.add(builder.toString());
                final File file = new File(DEPENDENT_JS_SAVE_PATH + "/" + media.getName());
                fout = new FileOutputStream(file);
                outputStream = new BufferedOutputStream(fout);
                final byte buffer[] = new byte[1024];
                int character = inputStream.read(buffer);
                while (character != -1) {
                    outputStream.write(buffer, 0, character);
                    character = inputStream.read(buffer);
                }
                }
            }
            configuration.setDependentJsURL(dependentJsURL);
            if(!existingDependency.isEmpty()){
                if(dependentJsURL == null) {
                    configuration.setDependentJsURL(new ArrayList<String>());
                }
                
                configuration.getDependentJsURL().addAll(existingDependency);
            }
            
            } catch (IOException exception) {
                Clients.showNotification(Labels.getLabel("unabletoUploadFile"));
                LOG.error(Constants.EXCEPTION, exception);
            } finally {
                finallyBlock(inputStream, outputStream, fin, fout);
            }
    }
    /**
     * Loads dependent Css files for the plugin chart
     */
    private void cssUpload() {
        @SuppressWarnings("unchecked")
        final List<Media> medias = (List<Media>) cssFileId.getAttribute("Media");
        List<String> dependentCssURL = null;
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        InputStream fin = null;
        OutputStream fout = null;
        StringBuilder builder = null;
        try {
            if (medias != null) {
                dependentCssURL = new ArrayList<String>();
                for (Media media : medias) {
                    fin = new ByteArrayInputStream(media.getStringData().getBytes());
                    inputStream = new BufferedInputStream(fin);
                    final File baseDir = new File(CSS_SAVE_PATH);
                    if (!baseDir.exists()) {
                        baseDir.mkdirs();
                    }
                    builder = new StringBuilder();
                    builder.append("css/").append(media.getName());
                    dependentCssURL.add(builder.toString());
                    final File file = new File(CSS_SAVE_PATH + "/" + media.getName());
                    fout = new FileOutputStream(file);
                    outputStream = new BufferedOutputStream(fout);
                    final byte buffer[] = new byte[1024];
                    int character = inputStream.read(buffer);
                    while (character != -1) {
                        outputStream.write(buffer, 0, character);
                        character = inputStream.read(buffer);
                    }
                }
                configuration.setDependentCssURL(dependentCssURL);
            }
        } catch (IOException exception) {
            Clients.showNotification(Labels.getLabel("unabletoUploadFile"));
            LOG.error(Constants.EXCEPTION, exception);
        } finally {
            finallyBlock(inputStream, outputStream, fin, fout);
        }
    }
    
    /**
     * Closes the streams
     * @param inputStream
     * @param outputStream
     * @param fin
     * @param fout
     */
    private void finallyBlock(BufferedInputStream inputStream,
            BufferedOutputStream outputStream, InputStream fin,
            OutputStream fout) {
        try {
            if (inputStream != null){
                inputStream.close();
            }    
            if (outputStream != null){
                outputStream.close();
            }
            if (fin != null){
                fin.close();
            }
            if (fout != null){
                fout.close();
            }
        } catch (IOException ex) {
            LOG.error(Constants.EXCEPTION, ex);
        }
    }
}
