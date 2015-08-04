package org.hpccsystems.dashboard.controller.component; 

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.cluster.ClusterData;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.InputParam;
import org.hpccsystems.dashboard.chart.entity.RelevantData;
import org.hpccsystems.dashboard.chart.entity.ScoredSearchData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.TextData;
import org.hpccsystems.dashboard.chart.entity.TitleColumn;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.gauge.GaugeChartData;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.chart.utils.TableRenderer;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.GroupService;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.springframework.dao.DataAccessException;
import org.xml.sax.SAXException;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Box;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Html;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbar;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;

import com.google.gson.Gson;


/**
 * ChartPanel class is used to create,edit and delete the Dashboard portlet's.
 *
 */
public class ChartPanel extends Panel {

    private static final  Log LOG = LogFactory.getLog(ChartPanel.class);
    private static final long serialVersionUID = 1L;    
    
    private ChartService chartService = (ChartService)SpringUtil.getBean(Constants.CHART_SERVICE);
    
    private static final String ADD_STYLE = "glyphicon glyphicon-plus btn btn-link img-btn";
    private static final String EDIT_STYLE = "glyphicon glyphicon-cog btn btn-link img-btn";
    private static final String RESET_STYLE = "glyphicon glyphicon-repeat btn btn-link img-btn";
    private static final String DELETE_STYLE = "glyphicon glyphicon-trash btn btn-link img-btn";
    private static final String INPUT_PARAM_STYLE = "glyphicon glyphicon-tasks btn btn-link img-btn";
    private static final String RESIZE_MAX_STYLE = "glyphicon glyphicon-resize-full btn btn-link img-btn";
    private static final String RESIZE_MIN_STYLE = "glyphicon glyphicon-resize-small btn btn-link img-btn";
    private static final String GREATER_THAN = ">";
    private static final String TITLE_PATTERN = "<$";
    
    final Button addBtn = new Button();
    final Button resetBtn = new Button();
    final Button deleteBtn = new Button();
    final Button resizeBtn = new Button();
    final Div holderDiv = new Div();
    final Div chartDiv = new Div();
    final Label titlelabel = new Label();
    final Textbox titleTextbox = new Textbox();
    final Box imageContainer = new Box();
    final Caption caption = new Caption();
    Button inputParamBtn = null;
    Listbox inputListbox = null;
    Portlet portlet;
    Toolbar toolbar;
    private String divId;
    private int btnState;
    private boolean showLocalFilter;
    
   
    //Delete panel listener
    EventListener<Event> deleteListener = new EventListener<Event>() {
        public void onEvent(final Event event) {
            deleteWidget();
        }
    };
    
    //Reset button listener
    EventListener<Event> resetListener = new EventListener<Event>() { 
        public void onEvent(final Event event) {
            resetWidget();
        }

    };

            
    //Event Listener for Change of title text
    EventListener<Event> titleChangeLisnr = new EventListener<Event>() {
        public void onEvent(final Event event)  {
            if(LOG.isDebugEnabled()){
                LOG.debug("Title is changed");
            }
            titleTextbox.setVisible(false);
            titlelabel.setVisible(true);
            titlelabel.setValue(titleTextbox.getValue());
            portlet.setName(titleTextbox.getValue());                             
           
            //Update Chart Title in DB
            try{
                WidgetService widgetService =(WidgetService) SpringUtil.getBean("widgetService");
                widgetService.updateWidgetTitle(portlet);
            }catch(DataAccessException ex){
                LOG.error(Constants.EXCEPTION, ex);
            }
            
            Events.postEvent("onChangeTitleValues", ChartPanel.this, null); 
        }
    };
    
    public void onChangeTitleValues(Event event)
    {
        if(portlet.getName() != null && portlet.getName().contains(TITLE_PATTERN)){
            generateTitleColumns();
            //Redraw chart to get dynamic title
            if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
                HPCCService hpccService = (HPCCService) SpringUtil.getBean(Constants.HPCC_SERVICE);
            
                try {
                  //Sets the dynamic title column values with the result retrieved from Hpcc 
                    if(portlet.getChartData() instanceof XYChartData){ 
                        ChartRenderer chartRenderer = (ChartRenderer) SpringUtil.getBean("chartRenderer");
                        hpccService.getChartData((XYChartData) portlet.getChartData(),portlet.getTitleColumns());                        
                        chartRenderer.setTitleColValFromInputparam(portlet.getTitleColumns(),portlet.getChartData().getInputParams());
                    }else if(portlet.getChartData() instanceof TableData){
                        hpccService.fetchTableData((TableData) portlet.getChartData(),portlet.getTitleColumns());
                        TableRenderer tableRenderer = (TableRenderer) SpringUtil.getBean("tableRenderer");
                        tableRenderer.setTitleColValFromInputparam(portlet.getTitleColumns(),portlet.getChartData().getInputParams());
                    }
                   
                } catch (XPathExpressionException | HpccConnectionException
                        | ParserConfigurationException | SAXException
                        | IOException | ServiceException e) {
                    LOG.error(Constants.EXCEPTION, e);
                }
            }  
            if(LOG.isDebugEnabled()){
                LOG.debug("TitleColumns -->"+portlet.getTitleColumns());
            }
            generateDynamicTitle();               
        }  
    }
    
    /**
     * Changes the dynamic chart title with the latest input param values
     * @param event
     */
    public void onChangeInputParamChangeTitle(Event event){
        //Only the input param value will be change
        if(portlet.getName() != null && portlet.getName().contains(TITLE_PATTERN)){
            if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
                if(portlet.getChartData() instanceof XYChartData){
                    ChartRenderer chartRenderer = (ChartRenderer) SpringUtil.getBean("chartRenderer");
                    chartRenderer.setTitleColValFromInputparam(portlet.getTitleColumns(),portlet.getChartData().getInputParams());
                }else if(portlet.getChartData() instanceof TableData){
                    TableRenderer tableRenderer = (TableRenderer) SpringUtil.getBean("tableRenderer");
                    tableRenderer.setTitleColValFromInputparam(portlet.getTitleColumns(),portlet.getChartData().getInputParams());
                }
            }  
            if(LOG.isDebugEnabled()){
                LOG.debug("TitleColumns -->"+portlet.getTitleColumns());
            }
            generateDynamicTitle();
        }
    }
    
    EventListener<Event> enableTitleEdit = (event)->{
        titleTextbox.setVisible(true);
        titleTextbox.focus();
        titlelabel.setVisible(false);
        if(portlet.getName() != null){
            titleTextbox.setValue(portlet.getName());            
        } else {
            titleTextbox.setValue(Labels.getLabel("chartTitle"));
        }
    };

    // Defining and adding event listener to 'Add' button
    EventListener<Event> addListener = new EventListener<Event>() {

        public void onEvent(final Event event) {
            // Defining parameters to send to Modal Dialog
            final Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(Constants.PARENT, ChartPanel.this);
            parameters.put(Constants.PORTLET, portlet);

            final Window window = (Window) Executions.createComponents(
                    "/demo/add_widget.zul", holderDiv, parameters);
            window.doModal();
        }

    };
    
    //Defining event listener to 'Edit' button in the portlet
    EventListener<Event> editListener = new EventListener<Event>() {
        public void onEvent(final Event event) {
            // Defining parameters to send to Modal Dialog
            final Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(Constants.PARENT, ChartPanel.this);
            parameters.put(Constants.PORTLET, portlet);
            
            final Window window = (Window) Executions.createComponents(
                    "/demo/layout/edit_portlet.zul", holderDiv, parameters);
            window.doModal();
        }
    };    
    
    // Defining event listener to 'Input parameter apply/done' button in the portlet
    EventListener<MouseEvent> applyParamsListeners = new EventListener<MouseEvent>() {
        public void onEvent(final MouseEvent event) {
            if(Constants.RELEVANT_CONFIG == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                RelevantData relevantData = (RelevantData) portlet.getChartData();
                for ( Component comp : inputListbox.getChildren()) {
                    if(comp instanceof InputListitem) {
                        InputListitem listitem = (InputListitem) comp;
                        LOG.debug("listitem.getInputValue() -->"+listitem.getInputValue());
                        relevantData.setClaimId(listitem.getInputValue());
                    }
                }
            }else{
                Map<String,String> inputs = new HashMap<String, String>();               
                
               for ( Component comp : inputListbox.getChildren()) {
                    if(comp instanceof InputListitem) {
                        InputListitem listitem = (InputListitem) comp;
                        inputs.put(listitem.getParamName(), listitem.getInputValue());
                    }
                }
                
               InputParam inputparam = null;
               List<InputParam> paramsList = new ArrayList<InputParam>();
               for(Entry<String, String> entry : inputs.entrySet()){
                   inputparam = new InputParam(entry.getKey(),entry.getValue());
                   paramsList.add(inputparam);
               }
                portlet.getChartData().setInputParams(paramsList);
            }

            Clients.showBusy(ChartPanel.this, "Updating Chart");
            
            //passing COMMON_FILTERS_ENABLE as false, as file fields not
            // required to fetch
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(Constants.COMMON_FILTERS_ENABLED, false);
            Events.sendEvent("onCreateLiveChart", ChartPanel.this, parameters);
            
            WidgetService widgetService = (WidgetService)SpringUtil.getBean("widgetService");
            try {
                widgetService.updateWidget(portlet);
            } catch (DataAccessException |JAXBException |EncryptDecryptException e) {
                LOG.error(e);
                Clients.showNotification("Error occurred while persisting current changes","error", ChartPanel.this,"middle_center",3000, false);
            }
            //closing popup window
            Popup inputPopup = (Popup)event.getTarget().getParent().getParent().getParent().getParent();
            inputPopup.close();
        }
    };
        


    public ChartPanel(final Portlet argPortlet, final int buttonState, final boolean showLocalFilters) {
        this.btnState = buttonState;
        this.showLocalFilter = showLocalFilters;
        
        this.setZclass("panel");
        this.imageContainer.setVflex("1");
        this.imageContainer.setHflex("1");
        this.imageContainer.setAlign("center");
        this.imageContainer.setPack("center");
        
        this.portlet = argPortlet;
        this.setBorder("normal");
        this.setWidth("99%");
        this.setStyle("margin-bottom:5px");
        
        // Creating title bar for the panel
        caption.setWidth("100%");

        final Div div = new Div();
        div.setStyle("padding:0");

        final Hbox hbox = new Hbox();
        hbox.setPack("stretch");
        hbox.setWidth("100%");
        hbox.setHflex("1");

        titlelabel.setVflex("1");
        titlelabel.setSclass("title-label");
        if(portlet.getName() != null){
            if(portlet.getName().contains(TITLE_PATTERN)){
                generateTitleColumns();
            }
            titlelabel.setValue(portlet.getName()); 
                       
        } else {
            titlelabel.setValue(Labels.getLabel("chartTitle"));
        }
        titleTextbox.setVisible(false);
        titleTextbox.setHflex("1");
        titleTextbox.setVflex("1");
        titleTextbox.setSclass("title-textbox");
       
        titleTextbox.setMaxlength(200);

        toolbar = new Toolbar();
        toolbar.setAlign("end");
        toolbar.setStyle("float:right; border-style: none;");

        resetBtn.setSclass(RESET_STYLE);
        resetBtn.setTooltiptext("Reset Chart");

        deleteBtn.setSclass(DELETE_STYLE);
        deleteBtn.addEventListener(Events.ON_CLICK, deleteListener);
        deleteBtn.setTooltiptext("Delete Chart");
        
        resizeBtn.setSclass(RESIZE_MAX_STYLE);
        resizeBtn.setTooltiptext("Maximize window");
        
        if(showLocalFilters) {
            //Shows Input parameters for Quieries(Roxie/Thor)
            onDrawingQueryChart(buttonState);
        }
        
        if(Constants.SHOW_ALL_BUTTONS == buttonState) {
        	if(Constants.STATE_EMPTY.equals(portlet.getWidgetState())){
        	    //Adding title change listeners only for ADMINS
        	    titlelabel.addEventListener(Events.ON_CLICK, enableTitleEdit);
        	    titleTextbox.addEventListener(Events.ON_BLUR, titleChangeLisnr);
        	    
        	    addBtn.setTooltiptext("Add Chart");
            }else{
                addBtn.setTooltiptext("Configure Chart");
            }
            toolbar.appendChild(addBtn);
            AuthenticationService authenticationService = (AuthenticationService)SpringUtil.getBean("authenticationService");
            if(!Constants.CIRCUIT_APPLICATION_ID.equals(authenticationService.getUserCredential().getApplicationId())){
                toolbar.appendChild(resetBtn);
                toolbar.appendChild(deleteBtn);
            }
        } else if (Constants.SHOW_EDIT_ONLY == buttonState) {
            //Nothing right now.. :(
        }else if(Constants.SHOW_NO_BUTTONS == buttonState){
            titlelabel.setStyle("pointer-events:none");
            titleTextbox.setReadonly(true);
        }
        toolbar.appendChild(resizeBtn);
        
        Hlayout hlayout = new Hlayout();
        hlayout.appendChild(titlelabel);
        hlayout.appendChild(titleTextbox);
        hlayout.setHflex("1");
        
        hbox.appendChild(hlayout);
        hbox.appendChild(toolbar);

        div.appendChild(hbox);
        caption.appendChild(div);
        this.appendChild(caption);

        // Creating panel contents
        final Panelchildren panelchildren = new Panelchildren();
       	setHeight();
        panelchildren.appendChild(holderDiv);
        this.appendChild(panelchildren);
        
        resetBtn.addEventListener(Events.ON_CLICK, resetListener);
        
        if(portlet.getWidgetState().equals(Constants.STATE_EMPTY)){
            addBtn.setSclass(ADD_STYLE);
            addBtn.addEventListener(Events.ON_CLICK, addListener);
            resetBtn.setDisabled(true);
        }else if(portlet.getWidgetState().equals(Constants.STATE_LIVE_CHART)){
            addBtn.setSclass(EDIT_STYLE);
            addBtn.addEventListener(Events.ON_CLICK, editListener);
            resetBtn.setDisabled(false);
            createChartHolder();
            Clients.showBusy(this, "Retriving data");
        } else if(portlet.getWidgetState().equals(Constants.STATE_GRAYED_CHART)){
            //Only Static image is added
            setStaticImage();
            addBtn.setSclass(EDIT_STYLE);
            addBtn.addEventListener(Events.ON_CLICK, editListener);
            resetBtn.setDisabled(false);
        }
        
        chartDiv.setVflex("1");
        
        //listener to maximize and minimize window
        resizeBtn.addEventListener(Events.ON_CLICK, maximizeListener ->{
        if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
            ChartPanel.this.setMaximizable(true);
            if(!ChartPanel.this.isMaximized()){
                holderDiv.setHeight("");
                if ((Constants.CATEGORY_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory())
                        ||(Constants.CATEGORY_TEXT_EDITOR == chartService.getCharts().get(portlet.getChartType()).getCategory())
                        ||(Constants.CATEGORY_SCORED_SEARCH_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory())) {
                    
                    int screenHeight = Integer.parseInt(Sessions.getCurrent()
                            .getAttribute(Constants.SCREEN_HEIGHT).toString());
                    Sessions.getCurrent().getAttribute(Constants.SCREEN_HEIGHT);
                	holderDiv.setHeight(String.valueOf(screenHeight-240)+"px");
                } else {
                	holderDiv.setVflex("1");	
                }
                ChartPanel.this.setMaximized(true);
                resizeBtn.setSclass(RESIZE_MIN_STYLE);
                resizeBtn.setTooltiptext("Minimize window");
            }else{
                holderDiv.setVflex(null);
                setHeight();
                ChartPanel.this.setMaximized(false);
                resizeBtn.setSclass(RESIZE_MAX_STYLE);
                resizeBtn.setTooltiptext("Maximize window");
            }
            
            if (Constants.CATEGORY_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                drawTableWidget(false);
            } else if (Constants.CATEGORY_TEXT_EDITOR == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                //onCreateDocumentWidget();
            } else if(Constants.CATEGORY_SCORED_SEARCH_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                //drawScoredSearchTable();
            }else {
                if(Constants.RELEVANT_CONFIG == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    Clients.evalJavaScript("resizeGraph()");
                }else{
                    String chartScript = drawD3Graph();
                    if (chartScript != null) {
                        Clients.evalJavaScript(chartScript);
                    } else {
                        throw new WrongValueException("JSON to create chart is null");
                    }
                    
                }
            }
        }else{
        	
        	 Clients.showNotification("Add / Configure the chart before maximizing...",
                     Clients.NOTIFICATION_TYPE_WARNING, this,"middle_center", 3000, true);
        	
        }
        });
        
        this.addEventListener(Constants.ON_GENERATE_DYNAMIC_TITLE, event ->{
            if(portlet.getName() != null && portlet.getName().contains(TITLE_PATTERN)){
                generateDynamicTitle();
            }
        });
        
        this.addEventListener("onDrawingQueryChart", event ->{
            onDrawingQueryChart(buttonState);
        });
        
    }

  //Generate dynamic label 'ModelID:<$A030>'
    protected void generateDynamicTitle() {        
        String chartName = portlet.getName();
        StringBuffer columToReplace = null;
        for(TitleColumn titleColumn :portlet.getTitleColumns()){
            if(titleColumn.getValue() != null){
                columToReplace = new StringBuffer();
                columToReplace.append("<$").append(titleColumn.getName()).append(">");                
                if(chartName.contains(columToReplace)){
                    chartName = chartName.replace(columToReplace, titleColumn.getValue().toUpperCase());
                }
            }
        }
        
        if(LOG.isDebugEnabled()){
            LOG.debug("chartName -->"+chartName);                    
        }
        titlelabel.setValue(chartName);
    }

    /**
     * Parses the portlet label 'ModelID: <$ModelID> Actual:<$Cur_Period>'
     * and creates ChartNameField object.Later these field will be used to generate
     * dynamic title of the chart as'ModelID: <$A030> Actual:<$2014>'
     */
    private void generateTitleColumns() {
        try{
            String label = portlet.getName();
            List<String> columnLabels = Arrays.asList(label.split(GREATER_THAN));
            portlet.setTitleColumns(new ArrayList<TitleColumn>());
            
            columnLabels.stream().forEach(colLabel ->{
                colLabel = colLabel.trim();
                String titleColumnLabel = null;
                if(colLabel.contains(":")){
                    titleColumnLabel = colLabel.substring(0, colLabel.indexOf(":"));
                }else{
                    titleColumnLabel = "";
                }               
                String titleColumnName = null;
                if(colLabel.contains(">")){
                    titleColumnName = colLabel.substring(colLabel.indexOf("<$")+2,colLabel.indexOf(">"));
                }else{
                   titleColumnName = colLabel.substring(colLabel.indexOf("<$")+2);
                }
                TitleColumn titleField = new TitleColumn(titleColumnLabel,titleColumnName);
                portlet.getTitleColumns().add(titleField);
            });
        }catch(Exception e){
            //Didn't log as it is not required.The title can be anything,
            //if it is not having format 'ModelID: <$modelid>'
        }
        
    }

    private void setHeight() {
        int screenHeight = Integer.parseInt(Sessions.getCurrent()
                .getAttribute(Constants.SCREEN_HEIGHT).toString());
        Sessions.getCurrent().getAttribute(Constants.SCREEN_HEIGHT);
        
        StringBuilder sb = new StringBuilder();
        if(portlet.getIsSinglePortlet()){
        	sb.append(screenHeight-240);	
        } else {
        	sb.append(385);	
        }
        
        sb.append("px");
        holderDiv.setHeight(sb.toString());
    }
    
    //Adds input parameters to display in chart/portlet
    EventListener<Event> onAddInputParams = new EventListener<Event>() {

        @Override
        public void onEvent(Event event) throws Exception {
            HPCCQueryService hpccQueryService = (HPCCQueryService) SpringUtil.getBean(Constants.HPCC_QUERY_SERVICE);
            removeExistingInputparam();            
            Map<String, Set<String>> paramValues = null;
            try {
                
                //IF RELEVANT
                // portlet.getChartData().setInputParams(paramsList);
                if(Constants.RELEVANT_CONFIG == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    constructRelevantInputParam(portlet.getChartData());                    
                } else {                  
                    //Taking first query, as joining not allows for queries
                    QuerySchema querySchema = hpccQueryService
                            .getQuerySchema(portlet.getChartData().getFiles().iterator().next(), portlet.getChartData().getHpccConnection(), portlet
                                    .getChartData().isGenericQuery(), portlet.getChartData().getInputParamQuery());
                    paramValues = querySchema.getInputParams();
                    if(paramValues != null) {
                        paramValues.entrySet().forEach(entry ->{
                            InputListitem listitem = new InputListitem(
                                    entry.getKey(), entry.getValue(), String.valueOf(portlet.getId() + "_board_"));
                            inputListbox.appendChild(listitem);
                            try{
                             InputParam appliedInput = portlet.getChartData() .getInputParams() .stream()
                                            .filter(input -> input.getName().equals(
                                                    entry.getKey())).findAny().get();
                             if (appliedInput != null && !com.mysql.jdbc.StringUtils.isNullOrEmpty(appliedInput.getValue())) {
                                 listitem.setInputValue(appliedInput.getValue());
                             }
                            }catch(NoSuchElementException e){
                                //Need not to log.This occurs when an inputparam from Hpcc is not found in portlet's
                                //applied inputparam
                            }
                            
                        });
                        
                    }
                    Events.postEvent("onChangeInputParamChangeTitle", ChartPanel.this, null);
                }
            } catch (Exception e) {
                //Exception is not thrown as it is not necessary
                LOG.error(e);
            }
           
            inputListbox.setAttribute(Constants.HAS_INPUT_PARAM_VALUES, true);
            
            Clients.clearBusy((Popup)event.getData());        
        
        }
    };

        private void constructRelevantInputParam(ChartData chartData) {
            
            ChartData inputParamData = new TableData();
            inputParamData.setHpccConnection(chartData.getHpccConnection());
            List<String> files = new ArrayList<String>();
            files.add("relevant_claimslist");
            inputParamData.setFiles(files);
            
            List<Attribute> attributes = new ArrayList<Attribute>();
            Attribute attr = new Attribute();
            //hard coding 'report_no' name, as relevant chart always uses only 'claim id'
            attr.setColumn("report_no");
            attributes.add(attr);       
            ((TableData)inputParamData).setAttributes(attributes);
            
            ListModelList<String> inputParam = new ListModelList<String>();
            
            Map<String, List<Attribute>> inputs = null;
            try {
                inputs = ((HPCCQueryService)SpringUtil.getBean("hpccQueryService")).fetchTableData((TableData)inputParamData,portlet.getTitleColumns());
            } catch (RemoteException | HpccConnectionException e) {
               LOG.error(Constants.EXCEPTION,e);
            }
            LOG.debug("Input parameters -->"+ inputs);
            
            if(inputs != null && inputs.get("report_no") != null){
                inputs.get("report_no").stream().forEach(attribute ->{
                    Attribute attribut = (Attribute)attribute;
                    inputParam.add(attribut.getColumn());
                });
            }
        
            InputListitem listitem = new InputListitem("report_no", new HashSet<String>(inputParam), String.valueOf(portlet.getId() + "_board_"));
            inputListbox.appendChild(listitem);
            listitem.setInputValue(((RelevantData)chartData).getClaimId());            
      }; 
    
    /**
     * Removes the inputparameters which are already existing in the input param popup
     * and leaves the header part
     */
    protected void removeExistingInputparam() {
            List<Component> removableItem = new ArrayList<Component>();
            removableItem.addAll(inputListbox.getChildren().stream().filter(comp ->
            (comp instanceof InputListitem)).collect(Collectors.toList()));
            
            removableItem.stream().forEach(component ->{
                component.detach();
            });
        }
    

    public void onDrawingQueryChart(final int buttonState) {
        boolean showIPButton = Constants.SHOW_ALL_BUTTONS == buttonState || Constants.SHOW_EDIT_ONLY == buttonState;
        
        if(!showIPButton || !showLocalFilter) {
            return;
        }
        
        if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState()) && portlet.getChartData().getIsQuery() 
                && (Constants.CATEGORY_SCORED_SEARCH_TABLE !=  chartService.getCharts().get(portlet.getChartType())
                                .getCategory())){
            if(inputParamBtn != null) {
                inputParamBtn.detach();
                inputListbox.detach();
            }
            
            inputParamBtn = new Button();
            if(showIPButton && !toolbar.getChildren().isEmpty()) {
                toolbar.insertBefore(inputParamBtn, toolbar.getFirstChild());
            } else if(showIPButton){
                toolbar.appendChild(inputParamBtn);
            }
            inputParamBtn.setSclass(INPUT_PARAM_STYLE);
            inputParamBtn.setZclass("btn btn-sm btn-primary");
            inputParamBtn.setAttribute(Constants.INPUT_PARAM_BTN, true); 
            inputParamBtn.setTooltiptext("Configure filter");
            
            
            final Popup popup = new Popup();
            popup.setZclass("popup");
            popup.setWidth("250px");
            popup.setHeight("270px");
            popup.setStyle("overflow:auto");
            caption.appendChild(popup);
            inputParamBtn.setPopup(popup);         
            
            inputParamBtn.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {
                @Override
                public void onEvent(MouseEvent event) throws Exception {
                    Boolean hasParamValues = (Boolean)inputListbox.getAttribute(Constants.HAS_INPUT_PARAM_VALUES);
                    Boolean hasCommonFilter = (Boolean)ChartPanel.this.getAttribute(Constants.COMMON_FILTERS_ENABLED);
                     if(Constants.RELEVANT_CONFIG == chartService.getCharts().get(portlet.getChartType()).getCategory()
                             || (portlet.getChartData().getInputParams() != null 
                             && ((hasCommonFilter != null && hasCommonFilter)  || hasParamValues == null || !hasParamValues))){
                            Clients.showBusy(popup, "Fetching Input Parameters");
                            Events.echoEvent(new Event("onAddInputParams", inputListbox,popup));
                        }
                     
                }
            });
            
            inputListbox = new Listbox();
            inputListbox.addEventListener("onAddInputParams", onAddInputParams);
            Listhead head = new Listhead();
            Listheader header = new Listheader();
            header.setLabel("Input Parameters");
            Button doneButton = new Button();
            doneButton.setSclass("glyphicon glyphicon-ok btn btn-link img-btn");
            doneButton.setStyle("float:right");
            doneButton.addEventListener(Events.ON_CLICK, applyParamsListeners);
            header.appendChild(doneButton);
            header.setParent(head);
            head.setParent(inputListbox);
            inputListbox.setParent(popup);
           
        }
    }    
       

    public void onCreateLiveChart(Event event) {        
        
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>)event.getData();
        Integer dashboardId =null;
        Boolean isCommonFilterEnabled = null;
        if(parameters != null){
            dashboardId =(Integer) parameters.get(Constants.DASHBOARD_ID);
            isCommonFilterEnabled = (Boolean)parameters.get(Constants.COMMON_FILTERS_ENABLED);
        }
        

        try {
            ChartRenderer chartRenderer = (ChartRenderer) SpringUtil.getBean("chartRenderer");
            HPCCService hpccService = (HPCCService) SpringUtil.getBean(Constants.HPCC_SERVICE);

            ChartData chartData = portlet.getChartData();
            int category = chartService.getCharts().get(portlet.getChartType()).getCategory();

            if (Constants.CATEGORY_TEXT_EDITOR !=category
                    && (isCommonFilterEnabled 
                            || Constants.CATEGORY_TABLE == category
                            || Constants.CATEGORY_CLUSTER == category) ) {
                // Getting fields for each files
                Map<String, List<Field>> fieldMap = new LinkedHashMap<String, List<Field>>();
                List<Field> fields = null;
                QuerySchema querySchema = null;
                for (String file : chartData.getFiles()) {
                    fields = new ArrayList<Field>();
                    if (!chartData.getIsQuery()) {
                        fields.addAll(hpccService.getColumns(file, chartData.getHpccConnection()));
                    } else {
                        // Roxie Query - fetching fields/columns of Roxie queries
                        HPCCQueryService hpccQueryService = (HPCCQueryService) SpringUtil.getBean(Constants.HPCC_QUERY_SERVICE);
                        querySchema = hpccQueryService.getQuerySchema(file, chartData.getHpccConnection(),
                                chartData.isGenericQuery(), chartData.getInputParamQuery());
                        fields.addAll(querySchema.getFields());
                    }

                    fieldMap.put(file, fields);
                }
                chartData.setFields(fieldMap);
            }

            if (Constants.CATEGORY_XY_CHART == category || Constants.CATEGORY_PIE == category
                    || Constants.CATEGORY_USGEO == category) {
                chartRenderer.constructChartJSON((XYChartData) chartData, portlet, false);
            } else if (Constants.CATEGORY_GAUGE == category) {
                chartRenderer.constructGaugeJSON((GaugeChartData) chartData, portlet, false);
            } else if (Constants.CATEGORY_HIERARCHY == category) {

                HttpSession httpSession = (HttpSession) Executions.getCurrent().getSession().getNativeSession();
                StringBuilder builder = new StringBuilder();
                builder.append(dashboardId).append("_").append(portlet.getId());
                httpSession.setAttribute(builder.toString(), (TreeData) portlet.getChartData());

                chartRenderer.constructTreeJSON((TreeData) chartData, portlet, builder.toString());
            } else if (Constants.CATEGORY_CLUSTER == category) {
                chartRenderer.constructClusterJSON((ClusterData) chartData, portlet);
            } else if (Constants.RELEVANT_CONFIG == category) {
            	RelevantData objRelevantData = (RelevantData)portlet.getChartData();
            	LOG.debug("RELEVANT DATA: "+objRelevantData);
            	
        		String relJSON = new Gson().toJson(objRelevantData);
        		LOG.debug("RELEVANT JSON: "+relJSON);
            	
            	//portlet.setChartDataJSON(" { \"claimId\": \"CLM00042945-C034\", \"claimImage\": \"\\uf0d6\", \"personImage\": \"\\uf007\", \"vehicleImage\": \"\\uf1b9\", \"policyImage\": \"\\uf0f6\" }");
        		portlet.setChartDataJSON(relJSON);
            }          

            // To construct Table Widget
            if (Constants.CATEGORY_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                drawTableWidget(true);
            } else if (Constants.CATEGORY_TEXT_EDITOR == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                onCreateDocumentWidget();
            } else if(Constants.CATEGORY_SCORED_SEARCH_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                drawScoredSearchTable();
            }else {
                String chartScript = drawD3Graph();
                if (chartScript != null) {
                    Clients.evalJavaScript(chartScript);
                } else {
                    throw new WrongValueException("JSON to create chart is null");
                }
            }

            if(portlet.getName() != null && portlet.getName().contains(TITLE_PATTERN)){
                generateDynamicTitle();
            }
            
            // Setting button visible, that is hidden when error occurred
            addBtn.setVisible(true);
        } catch (HpccConnectionException e) {
            LOG.error(Constants.EXCEPTION, e);
            if (e.getMessage().contains("401")) {
                createPasswordResetUI();
            } else {
                createErrorUI();
            }
        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION, e);
            createErrorUI();
        } finally {
            Clients.clearBusy(this);
            Clients.clearBusy();
            // Calling listener in Dashboard - This listener decides weather all
            // the charts are drawn
            Window window = null;
            Session session = Sessions.getCurrent();
            final List<Component> list = (ArrayList<Component>) Selectors.find(
                    ((Component) session.getAttribute(Constants.NAVBAR)).getPage(), "window");
            for (final Component component : list) {
                if (component instanceof Window) {
                    window = (Window) component;
                    Events.sendEvent(new Event("onPanelDrawn", window));
                }
            }
        }
    }
    
    /**
     * Draws scored search table for DB data
     */
    private void drawScoredSearchTable() {
         TableRenderer tableRenderer = (TableRenderer) SpringUtil.getBean("tableRenderer");    
         ScoredSearchData scoredSearchData = (ScoredSearchData) portlet.getChartData();
         
         chartDiv.getChildren().clear();
         Tabbox tabbox = new Tabbox();
         tabbox.setVflex("1");
         tabbox.setParent(chartDiv);
         Tabs tabs = new Tabs();
         tabs.setParent(tabbox);
         Tabpanels tabpanels = new Tabpanels();
         tabpanels.setParent(tabbox);
         HPCCQueryService hpccQueryService = (HPCCQueryService) SpringUtil.getBean("hpccQueryService"); 
         HashMap<String, HashMap<String, List<Attribute>>> hpccResult;
        try {
            hpccResult = hpccQueryService.fetchScoredSearchData(scoredSearchData);
            scoredSearchData.setHpccTableData(hpccResult);

            for (Entry<String, HashMap<String, List<Attribute>>> entry : hpccResult.entrySet()) {
                Tab tab = new Tab(entry.getKey());
                tab.setParent(tabs);
                Tabpanel tabpanel = new Tabpanel();
                Listbox listBox = tableRenderer.constructScoredSearchTable(entry.getValue(),false);
                listBox.setParent(tabpanel);
                tabpanel.setParent(tabpanels);
            }
        } catch (RemoteException | HpccConnectionException e) {
            LOG.error(Constants.EXCEPTION,e);
            Clients.showNotification("Unable to fetch Hpcc data",
                    Clients.NOTIFICATION_TYPE_ERROR, this,"middle_center", 3000, true);
            return;
        }                    
        
     }


    private void createErrorUI() {
        chartDiv.setSclass("error-div");
        Label label = new Label("Unable to recreate this widget.");
        label.setZclass("error-label");
        chartDiv.appendChild(label);
    }
    
    private void createPasswordResetUI() {
        addBtn.setVisible(false);
        
        final Textbox textbox = new Textbox();
        textbox.setPlaceholder("Enter new password");
        textbox.setType("password");
        
        final Button button = new Button("Update");
        button.setZclass("btn btn-sm btn-info");
        button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            boolean isWarned = false;
            
            @Override
            public void onEvent(Event event) throws Exception {
                if(textbox.getValue().length() < 1 && !isWarned) {
                    Clients.showNotification("Password is empty. If you intend to submit an empty password, hit update again", "warning", textbox, "after_center", 3000, true);
                    isWarned = true;
                    return;
                }
                
                //Retrieve all dashboards to update
                AuthenticationService authenticationService = (AuthenticationService) SpringUtil.getBean(Constants.AUTHENTICATION_SERVICE);
                DashboardService dashboardService = (DashboardService) SpringUtil.getBean(Constants.DASHBOARD_SERVICE);
                WidgetService widgetService = (WidgetService) SpringUtil.getBean(Constants.WIDGET_SERVICE);
                
                GroupService groupService = (GroupService) SpringUtil.getBean("groupService");                    
                List<String> groupCodes = groupService.getGroupCodes(authenticationService.getUserCredential().getUserId());
                
                List<Dashboard> dashboards = dashboardService.retrieveDashboards(authenticationService.getUserCredential().getApplicationId(), 
                        authenticationService.getUserCredential().getUserId(), 
                        groupCodes);
                
                //Update password in all applicable widgets in DB
                widgetService.updateHpccPassword(dashboards, portlet.getChartData().getHpccConnection(), textbox.getValue());
                //Update password in current Object
                portlet.getChartData().getHpccConnection().setPassword(textbox.getValue());
                
                Clients.showNotification("Password updated sucessfully", "info", ChartPanel.this, "middle_center", 2000, false);
                
                //Clear existing form
                chartDiv.getChildren().clear();
                
                Clients.showBusy(ChartPanel.this, "Recreating Chart");
                
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(Constants.COMMON_FILTERS_ENABLED, false);
                Events.echoEvent(new Event("onCreateLiveChart", ChartPanel.this,parameters));
            }
        });
        
        Label label = new Label("Unable to connect HPCC server at " + 
                portlet.getChartData().getHpccConnection().getHostIp() + 
                " as " + portlet.getChartData().getHpccConnection().getUsername() + 
                ". Server authentication information might have changed.");

        Label label2 = new Label("* This will update password for all of your widgets created using this credentials");
        label2.setSclass("fine-print");
        
        Vbox vbox = new Vbox(new Component[]{label, textbox, button, label2});
        vbox.setPack("center");
        vbox.setAlign("center");
        vbox.setHflex("1");
        vbox.setVflex("1");
        vbox.setSclass("dark-vbox");
        vbox.setSpacing("8px");
        
        chartDiv.appendChild(vbox);
    }
    
    /**
     * Provides the java script to draw the graph
     * @return
     * Returns null if Chart is not drawn in the panel yet
     * @throws Exception 
     */
    public String drawD3Graph() {
     
        if(!portlet.getWidgetState().equals(Constants.STATE_LIVE_CHART)){
            return null;
        }
        if(Constants.CATEGORY_HIERARCHY !=  chartService.getCharts().get(portlet.getChartType()).getCategory()){
            divId =  chartDiv.getId();
        }else{
            divId = chartDiv.getUuid();
        }
        String chartJson = null;
        if(Constants.RELEVANT_CONFIG != chartService.getCharts().get(portlet.getChartType()).getCategory()){
            chartJson = StringEscapeUtils.escapeJavaScript(portlet.getChartDataJSON());
        }else{
            chartJson = portlet.getChartDataJSON();
        }
                
        
        ChartDetails chartDetails = chartService.getCharts().get(portlet.getChartType());
        
        //Forming java script
        StringBuilder jsBuilder = new StringBuilder();
        
        //Importing style sheets
        if(chartDetails.getConfiguration().getDependentCssURL() != null) {
            for (String path : chartDetails.getConfiguration().getDependentCssURL()) {
                jsBuilder.append("jq('head').append('<link rel=\"stylesheet\" type=\"text/css\" href=\"")
                        .append(path)
                        .append("\" />');");
            }
        }
        
        if(chartDetails.getConfiguration().getGooglePackages() != null) {
            jsBuilder.append("function oneMethod() {");
            
            jsBuilder.append("jq.when(");
            
            jsBuilder.append("jq.getScript('")
                .append(chartDetails.getConfiguration().getJsURL())
                .append("'),");
            
            
            jsBuilder.append("jq.Deferred(function( deferred ){")
                    .append("jq( deferred.resolve );")
                    .append("})")
                .append(").done(function(){")
                .append(chartDetails.getConfiguration().getFunctionName())
                .append("('" + divId +  "','"+ chartJson +"')")
            .append("});");
            
            jsBuilder.append("}");
            
            jsBuilder.append("google.load('visualization', '1', {'packages': [");
            Iterator<String> iterator = chartDetails.getConfiguration().getGooglePackages().iterator();
            
            while (iterator.hasNext()) {
                jsBuilder.append("'");
                jsBuilder.append(iterator.next());
                jsBuilder.append("'");
                
                if(iterator.hasNext()) {
                    jsBuilder.append(",");
                }
            }
            
            jsBuilder.append("],'callback': oneMethod});");
        } else {
            jsBuilder.append("jq.when(");
            if(chartDetails.getConfiguration().getDependentJsURL() != null 
                    && !chartDetails.getConfiguration().getDependentJsURL().isEmpty()) {
                for (String path : chartDetails.getConfiguration().getDependentJsURL()) {
                    jsBuilder.append("jq.getScript('")
                        .append(path)
                        .append("'),");
                }
            }
            
            jsBuilder.append("jq.getScript('")
                .append(chartDetails.getConfiguration().getJsURL())
                .append("'),");
            
            jsBuilder.append("jq.Deferred(function( deferred ){")
                    .append("jq( deferred.resolve );")
                    .append("})")
                .append(").done(function(){")
                .append(chartDetails.getConfiguration().getFunctionName())
                .append("('" + divId +  "','"+ chartJson +"')")
                .append("});");
        }
        
        return jsBuilder.toString();
    }
    
    //To construct Table Widget
    public void drawTableWidget(boolean refreshData){
        TableRenderer tableRenderer = (TableRenderer) SpringUtil.getBean("tableRenderer");
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("Fields -> " + portlet.getChartData().getFields());
        }
        
        Vbox vbox = tableRenderer.constructTableWidget(portlet, (TableData) portlet.getChartData(), refreshData);
                
        chartDiv.getChildren().clear();
        chartDiv.appendChild(vbox);
    }
    
    //To construct Text Editor Widget
    public void onCreateDocumentWidget(){
        Div container = new Div();
        container.setVflex("1");
        container.setHflex("1");
        container.setSclass("html-container");
        Html html = new Html();
        html.setContent(((TextData)portlet.getChartData()).getHtmlText());
        container.appendChild(html);
        
        chartDiv.getChildren().clear();
        chartDiv.appendChild(container);        
    }

    /**
     * Adds static image
     * @throws Exception 
     */
    private void setStaticImage() {
        createChartHolder();
        Image image = new Image();
        ChartDetails chartDetails = chartService.getCharts().get(portlet.getChartType());
        String imageURL = chartDetails.getConfiguration().getImageURL();
        image.setSrc(imageURL);
        image.setSclass("img-responsive grayscale-image");
        imageContainer.appendChild(image);
        chartDiv.appendChild(imageContainer);
        portlet.setWidgetState(Constants.STATE_GRAYED_CHART);
    }
    
    /**
     * Creates div for chart
     */
    private void createChartHolder() {
        String divId;
        Integer seq = 0;
        if(Sessions.getCurrent().getAttribute("divSeq") != null){
            seq = (Integer) Sessions.getCurrent().getAttribute("divSeq");
            if(LOG.isDebugEnabled()){
                LOG.debug("Seq present in Session --> " + seq);
            }
        }
        seq += 1;
        Sessions.getCurrent().setAttribute("divSeq", seq);
        
        if(portlet.getWidgetState().equals(Constants.STATE_EMPTY)) {
            divId = "chartDiv";
        } else{
            divId = "chartDivOld";
        }    
        
        divId = divId + seq;
        
        chartDiv.setId(divId);
        holderDiv.appendChild(chartDiv);
    }

    /**
     * deletes widget with confirmation message
     */
    private void deleteWidget() {
        try {
            // ask confirmation before deleting widget
           EventListener<ClickEvent> clickListener = new EventListener<Messagebox.ClickEvent>() {
                public void onEvent(ClickEvent event) {
                    if (Messagebox.Button.YES.equals(event.getButton())) {
                        WidgetService widgetService = (WidgetService) SpringUtil.getBean(Constants.WIDGET_SERVICE);
                        widgetService.deleteWidget(portlet.getId());
                        ChartPanel.this.detach();
                        
                        Window window =  null;
                        Session session = Sessions.getCurrent();
                        final List<Component> list = (List<Component>) Selectors.find(((Component)session.getAttribute(Constants.NAVBAR)).getPage(), "window");
                        for (final Component component : list) {
                            if(component instanceof Window){
                                window = (Window) component;
                                Events.sendEvent(new Event("onPortalClose", window, portlet));
                            }
                        }
                    }

                }
            };

            Messagebox.show(Labels.getLabel("deleteConfirmMsg"), Labels.getLabel("deleteChartTitle"), new Messagebox.Button[] {
                            Messagebox.Button.YES, Messagebox.Button.NO },Messagebox.QUESTION, clickListener);
        } catch (Exception ex) {
            Clients.showNotification(Labels.getLabel("unableToDeletewidget"));
            LOG.error(Constants.EXCEPTION, ex);
            return;
        }
    }
    
    //Event to create Static chart
    public void onCloseDialog(final Event event) {
        @SuppressWarnings("unchecked")
        final Map<String,Integer> paramMap = (Map<String, Integer>) event.getData();
        if(paramMap!=null){
            portlet.setChartType(paramMap.get(Constants.CHART_TYPE));
            setStaticImage();    
            addBtn.removeEventListener(Events.ON_CLICK, addListener);
            addBtn.setSclass(EDIT_STYLE);
            addBtn.setTooltiptext("Configure Chart");
            resetBtn.setDisabled(false);
            addBtn.addEventListener(Events.ON_CLICK, editListener);
        }
    }
    
    
    /**
     * @return
     * Portlet associated with the ChartPanel Object
     */
    public Portlet getPortlet() {
        return portlet;
    }
    
    /**
     * Detaches the static image attached to the Chartpanel and returns the chartDiv ID
     * @return
     *     The div id to draw chaert on
     */
    public Div removeStaticImage() {
        imageContainer.detach();
        return chartDiv;
    }
    
    private void resetWidget() {
        EventListener<ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
               resetChart();
            }

        };

        Messagebox.show(
                Labels.getLabel("resetChart"), 
                Labels.getLabel("resetChartTitle"), 
                new Messagebox.Button[] {
                    Messagebox.Button.YES, Messagebox.Button.NO 
                }, 
                Messagebox.QUESTION, 
                clickListener);
     
    }
    

    private void resetChart() {
        //Removing Input params button if available
        if(inputParamBtn != null) {
            inputParamBtn.detach();
        }
        
        Components.removeAllChildren(chartDiv);
        Components.removeAllChildren(imageContainer);
        chartDiv.getChildren().clear();
        chartDiv.detach();
        
        addBtn.setSclass(ADD_STYLE);
        addBtn.setTooltiptext("Add Chart");
        
        resetBtn.setDisabled(true);
        addBtn.removeEventListener(Events.ON_CLICK, editListener);
        addBtn.addEventListener(Events.ON_CLICK, addListener); 
        addBtn.setVisible(true);
        //Calling listener in Dashboard - This listener resets portlet object
        Window window =  null;
        Session session = Sessions.getCurrent();
        final List<Component> list = (ArrayList<Component>) Selectors.find(((Component)session.getAttribute(Constants.NAVBAR)).getPage(), "window");
        for (final Component component : list) {
            if(component instanceof Window){
                window = (Window) component;
                Events.sendEvent(new Event("onPanelReset", window, portlet));
            }
        }
    }

    public void showLocalFilter() {
        showLocalFilter = true;
        if(inputParamBtn != null){
            inputParamBtn.setVisible(true);
        } else {
            onDrawingQueryChart(this.btnState);
        }
    }

    public void hideLocalFilter() {
        showLocalFilter = false;
        if(inputParamBtn != null){
            inputParamBtn.setVisible(false);
        }
    }
} 
