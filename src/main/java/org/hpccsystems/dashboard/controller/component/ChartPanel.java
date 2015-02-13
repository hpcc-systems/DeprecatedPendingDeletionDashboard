package org.hpccsystems.dashboard.controller.component; 

import java.awt.Dimension;
import java.awt.Toolkit;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.cluster.ClusterData;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.InputParams;
import org.hpccsystems.dashboard.chart.entity.RelevantData;
import org.hpccsystems.dashboard.chart.entity.ScoredSearchData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.TextData;
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
import org.zkoss.zul.Html;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;
import org.zkoss.zul.Popup;
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
    
    final Button addBtn = new Button();
    final Button resetBtn = new Button();
    final Button deleteBtn = new Button();
    final Button resizeBtn = new Button();
    final Div holderDiv = new Div();
    final Div chartDiv = new Div();
    final Textbox titleTextbox = new Textbox();
    final Box imageContainer = new Box();
    final Caption caption = new Caption();
    Button inputParamBtn = null;
    Listbox inputListbox = null;
    Portlet portlet;
    Toolbar toolbar;
    
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
                LOG.debug("Title is being changed");
            }
            portlet.setName(titleTextbox.getValue());
            //Update Chart Title in DB
            try{
                WidgetService widgetService =(WidgetService) SpringUtil.getBean("widgetService");
                widgetService.updateWidgetTitle(portlet);
            }catch(DataAccessException ex){
                LOG.error(Constants.EXCEPTION, ex);
            }
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
            
            Map<String,String> inputs = new HashMap<String, String>();
            
            //multipleValues object was created for translating multiple value selection into List of Maps
            // This is currently not being used
            List<Object> multipleValues = new ArrayList<Object>();
            
            String multipleValueParamName = null;
            
            for ( Component comp : inputListbox.getChildren()) {
                if(comp instanceof InputListitem) {
                    InputListitem listitem = (InputListitem) comp;
                    inputs.put(listitem.getParamName(), listitem.getInputValue());
                }
            }
            
            InputParams inputParams;
            List<InputParams> paramsList = new ArrayList<InputParams>();
            if(multipleValues.isEmpty()) {
                inputParams = new InputParams(inputs);
                paramsList.add(inputParams);
            } else {
                Map<String,String> multiInputs;
                for (Object object : multipleValues) {
                    multiInputs = new HashMap<String, String>();
                    multiInputs.putAll(inputs);
                    multiInputs.put(multipleValueParamName, object.toString());
                    inputParams = new InputParams(multiInputs);
                    paramsList.add(inputParams);
                }
            }
            
            portlet.getChartData().setInputParams(paramsList);
            
            portlet.getChartData().setInputParams(paramsList);

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
        


    public ChartPanel(final Portlet argPortlet, final int buttonState) {
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

        titleTextbox.setInplace(true);
        titleTextbox.setVflex("1");
        titleTextbox.setStyle("border: none;    color: black;");
        if(portlet.getName() != null){
            titleTextbox.setValue(portlet.getName());            
        } else {
            titleTextbox.setValue(Labels.getLabel("chartTitle"));
        }
        titleTextbox.setWidth("250px");
        titleTextbox.setMaxlength(30);
        titleTextbox.addEventListener(Events.ON_CHANGE, titleChangeLisnr);

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
        
        //Shows Input parameters for Quieries(Roxie/Thor)
        onDrawingQueryChart();
        
        if(Constants.SHOW_ALL_BUTTONS == buttonState) {
        	addBtn.setTooltiptext("Add Chart");
            toolbar.appendChild(addBtn);
            AuthenticationService authenticationService = (AuthenticationService)SpringUtil.getBean("authenticationService");
            if(!Constants.CIRCUIT_APPLICATION_ID.equals(authenticationService.getUserCredential().getApplicationId())){
                toolbar.appendChild(resetBtn);
                toolbar.appendChild(deleteBtn);
            }
        } else if (Constants.SHOW_EDIT_ONLY == buttonState) {
            toolbar.appendChild(addBtn);
        }
        toolbar.appendChild(resizeBtn);
        
        hbox.appendChild(titleTextbox);
        hbox.appendChild(toolbar);

        div.appendChild(hbox);
        caption.appendChild(div);
        this.appendChild(caption);

        // Creating panel contents
        final Panelchildren panelchildren = new Panelchildren();
        if(portlet.getIsSinglePortlet()){
        	Toolkit tk = Toolkit.getDefaultToolkit();
        	Dimension d = tk.getScreenSize();
        	StringBuilder sb = new StringBuilder();
        	sb.append(d.height-254);
        	sb.append("px");
        	holderDiv.setHeight(sb.toString());
        }else{
            holderDiv.setHeight("385px");
        }
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
            ChartPanel.this.setMaximizable(true);
            if(!ChartPanel.this.isMaximized()){
                ChartPanel.this.setMaximized(true);
                resizeBtn.setSclass(RESIZE_MIN_STYLE);
                resizeBtn.setTooltiptext("Minimize window");
            }else{
                ChartPanel.this.setMaximized(false);
                resizeBtn.setSclass(RESIZE_MAX_STYLE);
                resizeBtn.setTooltiptext("Maximize window");
            }
        });
    }
    
    //Adds input parameters to display in chart/portlet
    EventListener<Event> onAddInputParams = new EventListener<Event>() {

        @Override
        public void onEvent(Event event) throws Exception {
            HPCCQueryService hpccQueryService = (HPCCQueryService) SpringUtil.getBean(Constants.HPCC_QUERY_SERVICE);
            
            Map<String, Set<String>> paramValues = null;
            try {
                
                //IF RELEVANT
                // portlet.getChartData().setInputParams(paramsList);
                if(Constants.RELEVANT_CONFIG == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    Set<String> setInuputParams = hpccQueryService.getInputParameters(portlet.getChartData().getFiles().iterator().next(), 
                            portlet.getChartData().getHpccConnection(),
                            portlet.getChartData().isGenericQuery(), 
                            portlet.getChartData().getInputParamQuery());
                    
                    for(String val : setInuputParams) {
                         if(val.equalsIgnoreCase("claim_ids") || val.equalsIgnoreCase("group_ids") || val.equalsIgnoreCase("person_ids") || val.equalsIgnoreCase("vehicle_ids")) {
                             InputListitem listitem = new InputListitem(val, null, String.valueOf(portlet.getId() + "_board_"));
                             inputListbox.appendChild(listitem);
                         }
                         
                    }
                    
                } else { 
                    paramValues = hpccQueryService.getInputParamDistinctValues(
                            portlet.getChartData().getFiles().iterator().next(),
                            portlet.getChartData().getInputParams().iterator().next().getParams().keySet(),
                            portlet.getChartData().getHpccConnection(), portlet.getChartData().isGenericQuery(),
                            portlet.getChartData().getInputParamQuery());
                }
            } catch (Exception e) {
                //Exception is not thrown as it is not necessary
                LOG.error(e);
            }

            if(paramValues != null) {
                for (InputParams inputParam : portlet.getChartData().getInputParams()) {
                    for (Entry<String,String> param : inputParam.getParams().entrySet() ) {
                        
                        LOG.debug("param.getKey(): "+param.getKey());
                        LOG.debug("paramValues.get(param.getKey()): "+paramValues.get(param.getKey()));
                        LOG.debug("String.valueOf(portlet.getId() + \"_board_\"): "+String.valueOf(portlet.getId() + "_board_"));
                        
                        InputListitem listitem = new InputListitem(param.getKey(), paramValues.get(param.getKey()), String.valueOf(portlet.getId() + "_board_"));
                        if(param.getValue() != null  && !param.getValue().isEmpty()) {
                            listitem.setInputValue(param.getValue());
                        }
                        inputListbox.appendChild(listitem);
                    }
                }
            }
            inputListbox.setAttribute(Constants.HAS_INPUT_PARAM_VALUES, true);
            
            Clients.clearBusy((Popup)event.getData());        
        
        }
    }; 
    
    public void onDrawingQueryChart() {
        if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState()) && portlet.getChartData().getIsQuery() 
                && (Constants.CATEGORY_HIERARCHY !=  chartService.getCharts().get(portlet.getChartType())
                        .getCategory() && Constants.CATEGORY_SCORED_SEARCH_TABLE !=  chartService.getCharts().get(portlet.getChartType())
                                .getCategory())){
            if(inputParamBtn != null) {
                inputParamBtn.detach();
                inputListbox.detach();
            }
            
            inputParamBtn = new Button();
            if(!toolbar.getChildren().isEmpty()) {
                toolbar.insertBefore(inputParamBtn, toolbar.getFirstChild());
            } else {
                toolbar.appendChild(inputParamBtn);
            }
            inputParamBtn.setSclass(INPUT_PARAM_STYLE);
            inputParamBtn.setZclass("btn btn-sm btn-primary");
            inputParamBtn.setAttribute(Constants.INPUT_PARAM_BTN, true); 
            
            final Popup popup = new Popup();
            popup.setZclass("popup");
            popup.setWidth("250px");
            popup.setHeight("270px");
            caption.appendChild(popup);
            inputParamBtn.setPopup(popup);         
            
            inputParamBtn.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {
                @Override
                public void onEvent(MouseEvent event) throws Exception {
                    Boolean hasParamValues = (Boolean)inputListbox.getAttribute(Constants.HAS_INPUT_PARAM_VALUES);
                     if(portlet.getChartData().getInputParams() != null 
                             && (hasParamValues == null || !hasParamValues)){
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
                drawTableWidget();
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
                Vbox vbox = tableRenderer.constructScoredSearchTable(entry.getValue(),false);
                vbox.setParent(tabpanel);
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
        String chartJson = StringEscapeUtils.escapeJavaScript(portlet.getChartDataJSON());
        
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
                .append("('" + chartDiv.getId() +  "','"+ chartJson +"')")
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
                .append("('" + chartDiv.getId() +  "','"+ chartJson +"')")
                .append("});");
        }
        
        return jsBuilder.toString();
    }
    
    //To construct Table Widget
    public void drawTableWidget(){
        TableRenderer tableRenderer = (TableRenderer) SpringUtil.getBean("tableRenderer");
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("Fields -> " + portlet.getChartData().getFields());
        }
        
        Vbox vbox = tableRenderer.constructTableWidget(portlet, (TableData) portlet.getChartData(), false);
                
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
} 
