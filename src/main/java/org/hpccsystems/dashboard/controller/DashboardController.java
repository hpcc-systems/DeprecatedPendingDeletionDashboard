package org.hpccsystems.dashboard.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.cluster.ClusterData;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.entity.InputParam;
import org.hpccsystems.dashboard.chart.entity.Interactivity;
import org.hpccsystems.dashboard.chart.entity.RelevantData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.TitleColumn;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.gauge.GaugeChartData;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.ChartPanel;
import org.hpccsystems.dashboard.controller.component.GroupListitem;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.entity.RequestParams;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.ConditionalGroupService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.GroupService;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.hpccsystems.dashboard.services.WidgetService;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.springframework.dao.DataAccessException;
import org.xml.sax.SAXException;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkmax.ui.event.PortalMoveEvent;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zkmax.zul.Portalchildren;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Anchorchildren;
import org.zkoss.zul.Anchorlayout;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Html;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Toolbar;
import org.zkoss.zul.Window;

import com.google.gson.Gson;

/**
 * DashboardController class is used to add new dashboard into sidebar and 
 *  controller class for dashboard.zul.
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DashboardController extends SelectorComposer<Window>{

    private static final long serialVersionUID = 1L;
    private static final  Log LOG = LogFactory.getLog(DashboardController.class); 
    
    private static String MINIMUM_VALUE = "minVal";
    private static String RANGE_FACTOR = "rangeFactor";
    
    private Dashboard dashboard; 
    private Integer oldColumnCount = null;
    
    Integer dashboardId = null;
    String dashboardRole = null;

    @Wire
    private Label nameLabel;
    
    
    @Wire
    private Toolbar dashboardToolbar;
            
    @Wire("portalchildren")
    private List<Portalchildren> portalChildren;
    
    @Wire
    private Panel commonFiltersPanel;
    @Wire
    private Rows filterRows;
    
    @Wire
    private Tabbox commonFilterTabbox;
    
    @Wire
    private Button manageGroups;
    
    @Wire
    private Popup addGroup;    
    
    @Wire
    private Listbox availableGroups, addedGroups;
    
    @Wire
    private Button addWidget;
    
    @Wire
    private Button configureDashboard;
    
    Integer panelCount = 0;
    Set<Filter> appliedCommonFilters;
    Set<InputParam> appliedCommonInputParam;
    
    Map<String, Set<Field>> commonFields;
    private int drawnLiveChartCount;
    private Map<String, Map<String,Set<String>>>  commonInputParams;
    private static final String PERCENTAGE_SIGN = "%";
    
    @WireVariable
    private AuthenticationService authenticationService;
    @WireVariable
    private DashboardService dashboardService;
    @WireVariable
    private WidgetService widgetService;
    @WireVariable
    private HPCCService hpccService;
    @WireVariable
    private ChartService chartService;
    @WireVariable
    private ConditionalGroupService  conditionalGroupService;    
    @WireVariable
    private GroupService groupService;
    @WireVariable
    private HPCCQueryService hpccQueryService;
    
    UserCredential userCredential;
    
    EventListener<Event> redrawInteractivityTable = (event) -> {
        @SuppressWarnings("unchecked")
        List<Portlet> selectedtables = (List<Portlet>) event.getData();
        selectedtables.stream().forEach(portlet -> {
            List<Component> chartPanels = portalChildren.get(portlet.getColumn()).getChildren();
                    ChartPanel selectedTablePanel = (ChartPanel) chartPanels
                            .stream()
                            .filter(panel -> portlet.getId().equals(
                                    ((ChartPanel) panel).getPortlet().getId()))
                            .findFirst().get();
                   selectedTablePanel.drawTableWidget(true);
        });
    };
    
    EventListener<Event> applyInteractivityFilter = (event) -> {
        Interactivity interactivity = (Interactivity) event.getData();
        Portlet releventPortlet = dashboard.getPortletList().stream().filter(portlet -> portlet.getId().equals(interactivity.getTargetId())).findFirst().get();
        List<Component> chartPanels = portalChildren.get(releventPortlet.getColumn()).getChildren();
        ChartPanel selectedRelevantPanel = (ChartPanel) chartPanels
                .stream()
                .filter(panel -> releventPortlet.getId().equals(
                        ((ChartPanel) panel).getPortlet().getId()))
                .findFirst().get();
        
        RelevantData relevantData = (RelevantData)releventPortlet.getChartData();
        relevantData.setClaimId(interactivity.getFilterValue());
       
        String relJSON = new Gson().toJson(relevantData);
        LOG.debug("relJSON --> "+relJSON);
        
        releventPortlet.setChartDataJSON(relJSON);
        //To update Relevant data with the selected claim id
        widgetService.updateWidget(releventPortlet);
        
        String chartScript = selectedRelevantPanel.drawD3Graph();
        if (chartScript != null) {
            Clients.evalJavaScript(chartScript);
        } 
        
    };
    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        
        dashboardId =(Integer) Executions.getCurrent().getAttribute(Constants.ACTIVE_DASHBOARD_ID);
        dashboardRole = (String)Executions.getCurrent().getAttribute(Constants.ACTIVE_DASHBOARD_ROLE);
        
        userCredential = authenticationService.getUserCredential();
       
        //For the first Dashboard, getting Id from Session
        if(dashboardId == null ){
            dashboardId = (Integer) Sessions.getCurrent().getAttribute(Constants.ACTIVE_DASHBOARD_ID);
            dashboardRole = (String)Sessions.getCurrent().getAttribute(Constants.ACTIVE_DASHBOARD_ROLE);
        }
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("Dashboard ID - " + dashboardId);
            LOG.debug("Dashboard Role - " +dashboardRole);
        }
        //removing the previous/existing commonfilterset from session 
        Sessions.getCurrent().removeAttribute(Constants.COMMON_FILTERS);
        
        if(dashboardId != null ){
            
            List<String> dashboardIdList = new ArrayList<String>(); 
            dashboardIdList.add(String.valueOf(dashboardId));
            List<Dashboard> dashboardList =null;
            try {
                dashboard = dashboardService.getDashboard(dashboardId);    
                //resetting the dashboard role from default 'admin' role
                dashboard.setRole(dashboardRole);
            } catch(Exception ex) {
                Clients.showNotification(
                        Labels.getLabel("unabletoRetrieveDB"),
                        Constants.ERROR_NOTIFICATION, comp, Constants.POSITION_CENTER, 3000, true);
                LOG.error("Exception while fetching widget details from DB", ex);
            }            
            if(dashboard != null){
                dashboard.setPersisted(true);
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Visiblity - > " + dashboard.getVisibility());
                }
                if(Constants.VISIBLITY_PUBLIC == dashboard.getVisibility()){
                    manageGroups.setVisible(true);
                    addAvailableGroups();
                    addAddedGroups();
                }else{
                    manageGroups.setVisible(false);
                }
                
            }
            if(LOG.isDebugEnabled()){
                LOG.debug("dashboardList in DashboardController.doAfterCompose()-->"+dashboardList);
                LOG.debug("Creating dashboard - Dashboard Id " + dashboardId);
            }
            nameLabel.setValue(dashboard.getName());
            
            //Preparing the layout
            Integer count = 0;
            for (Portalchildren portalchildren : portalChildren) {
                if( count < dashboard.getColumnCount()) {
                    portalchildren.setVisible(true);
                    portalchildren.setWidth(100/dashboard.getColumnCount() + PERCENTAGE_SIGN);
                }
                count ++;
            }        

            try {
                dashboard.setPortletList((ArrayList<Portlet>) widgetService.retriveWidgetDetails(dashboardId));
            } catch(DataAccessException ex) {
                Clients.showNotification(
                        Labels.getLabel("unableToRetrieveWidget"),
                        Constants.ERROR_NOTIFICATION, comp, Constants.POSITION_CENTER, 3000, true);
                LOG.error(Constants.EXCEPTION, ex);
            }
            
            if(LOG.isDebugEnabled()){
                LOG.debug("PortletList of selected Dashboard -->"+dashboard.getPortletList());
            }
            
            ChartPanel panel = null;
            //Common Query filters
            //Adding Query params from session
            if(userCredential.hasRole(Constants.ROLE_API_VIEW_DASHBOARD)) {
                if(Constants.ROLE_CONSUMER.equals(dashboard.getRole()) ) {
                    commonFiltersPanel.setVisible(false);
                  } else {
                       commonFiltersPanel.setVisible(true);                    
                  }
                
                RequestParams requestParams = (RequestParams) Sessions.getCurrent().getAttribute(Constants.REQUEST_PRAMS);
                if(requestParams.hasInputParams()) {
                    dashboard.setHasCommonFilter(true);
                    dashboard.addCommonQueryFilters(requestParams.getInputParams());
                    for (InputParam inputparam : requestParams.getInputParams()) {
                        if (Constants.ROLE_CONTRIBUTOR.equals(dashboard.getRole())
                                || Constants.ROLE_ADMIN.equals(dashboard.getRole())) {
                            inputparam.setIsCommonInput(true);
                          }
                        applyInputParamToPortlets(inputparam, false); 
                    }
                }
            }
            
            
            //Removing AddWidget , Configure Dashboard buttons for Single layout
            if(dashboard.getPortletList().size() == 1 && dashboard.getPortletList().get(0).getIsSinglePortlet()){
                addWidget.detach();
            }
            for (Portlet portlet : dashboard.getPortletList()) {  
              if(userCredential.hasRole(Constants.CIRCUIT_ROLE_VIEW_EDIT_DASHBOARD) ||
                        Constants.ROLE_ADMIN.equals(dashboard.getRole()) ) {
                    panel = new ChartPanel(portlet, Constants.SHOW_ALL_BUTTONS);
                } else if(Constants.ROLE_CONTRIBUTOR.equals(dashboard.getRole())) {
                     panel = new ChartPanel(portlet, Constants.SHOW_EDIT_ONLY);
                } else {
                    panel = new ChartPanel(portlet, Constants.SHOW_NO_BUTTONS);                    
                }
                                
                portalChildren.get(portlet.getColumn()).appendChild(panel);
                
                //Constructing chart data only when live chart is drawn
                if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
                    Clients.showBusy("Loading Dashboard");
                    drawnLiveChartCount++;
                    Map<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put(Constants.DASHBOARD_ID, dashboardId);
                    parameters.put(Constants.COMMON_FILTERS_ENABLED, dashboard.getHasCommonFilter());
                    
                    Events.echoEvent(new Event("onCreateLiveChart", panel,parameters));
                }                
                
            }
            
            if(!userCredential.hasRole(Constants.ROLE_API_VIEW_DASHBOARD)
                    && ! userCredential.getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID)
                    && dashboard.getRole().equals(Constants.ROLE_ADMIN)) {
                dashboardToolbar.setVisible(true);
            }
            
        } else {
            this.getSelf().setBorder("none");            
            return;
        }
         
        this.getSelf().addEventListener("onPortalClose", onPanelClose);
        this.getSelf().addEventListener("onLayoutChange", onLayoutChange);
        
        if(LOG.isDebugEnabled()){
            LOG.debug("Created Dashboard");
            LOG.debug("Panel Count - " + dashboard.getColumnCount());
            LOG.debug("dashboard.isShowFiltersPanel() --> " + dashboard.getHasCommonFilter());
        }
        
        
        this.getSelf().addEventListener("onDrawingLiveChart", onDrawingLiveChart);
        this.getSelf().addEventListener("onPanelReset", onPanelReset);
        this.getSelf().addEventListener("onPanelDrawn", onPanelDrawn);
        this.getSelf().addEventListener(Constants.ON_SAVE_INTERACTIVITY, redrawInteractivityTable);
        this.getSelf().addEventListener(Constants.ON_SELECT_INTERACTIVITY_FILTER, applyInteractivityFilter);
        
        //Setting common HpccObject to Session
        if(dashboard.getHasCommonFilter()) {
            setCommonHpccConnection();
            setPanelCommonfilterIndicator();
        } else {
            Sessions.getCurrent().setAttribute(Constants.HPCC_CONNECTION, null);
        }
        
        //Showing common filters panel true for new Dashboards
        if(!userCredential.hasRole(Constants.ROLE_API_VIEW_DASHBOARD) && dashboard.getHasCommonFilter()) {
            commonFiltersPanel.setVisible(true);
        }
    }
   

    /**
     * Shows common filter panel
     */
    private void constructCommonFilterPanel() throws HpccConnectionException,RemoteException {

        if (dashboard.getHasCommonFilter()) {
            if(!userCredential.hasRole(Constants.ROLE_API_VIEW_DASHBOARD)){
                commonFiltersPanel.setVisible(true);
            }
            //All charts used logical files
            if(Constants.LOGICAL_FILE.equals(dashboard.getFileType())) {
                constructDBCommonFilters();
                
            }else if(Constants.QUERY.equals(dashboard.getFileType())){
                //All charts used queries
                constructDBCommonInputparams();
            }
          
        }
    }
        
    private void constructDBCommonInputparams() {
        
        final List<InputParam> persistedGlobalInputParams  = new ArrayList<InputParam>();
        
        if(dashboard.getCommonQueryFilters() != null) {
            persistedGlobalInputParams.addAll(dashboard.getCommonQueryFilters());
        } else {
            dashboard.getPortletList().forEach( portlet -> {
                
                if (portlet.getWidgetState().equals( Constants.STATE_LIVE_CHART)
                        && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                        && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                    
                    List<InputParam>  portletInputs = new ArrayList<InputParam>();
                    if(portlet.getChartData().getInputParams() != null && !portlet.getChartData().getInputParams().isEmpty()){
                        portlet.getChartData().getInputParams().stream().forEach(inputparam -> {
                            if(inputparam.getIsCommonInput()
                                    && persistedGlobalInputParams.contains(inputparam)){
                                portletInputs.add(persistedGlobalInputParams
                                        .get(persistedGlobalInputParams.indexOf(inputparam)));
                            }else{
                                portletInputs.add(inputparam);
                                if (inputparam.getIsCommonInput()) {
                                    persistedGlobalInputParams.add(inputparam);
                                }
                            }
                        });
                    }                    
                    portlet.getChartData().setInputParams(portletInputs);
                }
                
            });
               
            dashboard.addCommonQueryFilters(persistedGlobalInputParams);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Persisted Common input params -> "  + persistedGlobalInputParams);
        }
       
        // Generating applied filter rows, with values
      
        // Getting all columns.This holds the fields for newly added charts, to avoid reconstructing listbox 
        //with filter columns for existing queries as well
        Map<String, Map<String,Set<String>>>  newInputParams = new LinkedHashMap<String, Map<String,Set<String>>>();
        boolean isInputparamCollected = false;

        if(commonInputParams == null){
            commonInputParams =  new LinkedHashMap<String, Map<String,Set<String>>>();
        }
        for(InputParam globalInput : persistedGlobalInputParams){
                Set<String> distinctValues = new HashSet<>();
                for(Portlet portlet : dashboard.getPortletList()){
                    if (portlet.getWidgetState().equals( Constants.STATE_LIVE_CHART)
                            && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                            && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                        QuerySchema querySchema =null;
                      //As joining not allows in Query taking first file
                        if(portlet.getChartData().getInputParams().contains(globalInput)){
                            try {
                                querySchema = hpccQueryService.getQuerySchema(portlet.getChartData().getFiles().get(0), portlet
                                        .getChartData().getHpccConnection(), portlet
                                        .getChartData().isGenericQuery(), portlet
                                        .getChartData().getInputParamQuery());
                           
                            distinctValues.addAll(querySchema.getInputParams().get(globalInput.getName())) ;
                            } catch (Exception e) {
                               LOG.error(Constants.EXCEPTION,e);
                            }
                        }
                        //collects the input param for all the portlets 
                        if(!isInputparamCollected){
                            getInputParams(portlet,newInputParams,querySchema);
                        }
                         
                    }
                }
                isInputparamCollected = true;
                try {
                    filterRows.appendChild(createQueryInputFilterRow(globalInput,distinctValues));
                } catch (RemoteException | HpccConnectionException e) {
                    LOG.error(Constants.EXCEPTION,e);
                }
            }
           

           
        if(!newInputParams.isEmpty()) {
            constructFilterItemForQuery(newInputParams);
        }else{
           dashboard.getPortletList().stream().forEach(portlet ->{
               if (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                       && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                       && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()){
                   getInputParams(portlet,newInputParams,null);
               }
           });
           
           constructFilterItemForQuery(newInputParams);
        }
        
    }


    private void constructDBCommonFilters() throws RemoteException, HpccConnectionException {
        List<Filter> persistedGlobalFilters = new ArrayList<Filter>();
        Set<Filter> filters;
        // Unifying Filter Objects - Making Duplicates filters a single
        // instance
        for (Portlet portlet : dashboard.getPortletList()) {
            if (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                    && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()  
                    && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()
                    && portlet.getChartData().getIsFiltered()) {
                filters = new LinkedHashSet<Filter>();
                for (Filter filter : portlet.getChartData().getFilters()) {
                    if (filter.getIsCommonFilter()
                            && persistedGlobalFilters.contains(filter)) {
                        filters.add(persistedGlobalFilters
                                .get(persistedGlobalFilters.indexOf(filter)));
                    } else {
                        filters.add(filter);
                        if (filter.getIsCommonFilter()) {
                            persistedGlobalFilters.add(filter);
                        }
                    }
                }
                portlet.getChartData().setFilters(filters);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Persisted Common filters -> "
                    + persistedGlobalFilters);
        }

        // Generating applied filter rows, with values
        
        for (Filter filter : persistedGlobalFilters) {
            Field field = null;
            field = new Field();
            field.setColumnName(filter.getColumn());
            if (filter.getType().equals(Constants.DATA_TYPE_STRING)) {
                // String filters now
                filterRows.appendChild(createStringFilterRow(filter));
            } else if (filter.getType().equals(Constants.DATA_TYPE_NUMERIC)) {
                // Numeric filters
                filterRows.appendChild(createNumericFilterRow(filter));
            }
        }            

            // Getting all columns
            commonFields = new HashMap<String, Set<Field>>();
            Set<Field> fieldSet;
            for (Portlet portlet : dashboard.getPortletList()) {
                if (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                        && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                        && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                    for (Map.Entry<String, List<Field>> entry : portlet
                            .getChartData().getFields().entrySet()) {
                        fieldSet = new LinkedHashSet<Field>();
                        for (Field field : entry.getValue()) {
                                fieldSet.add(field);
                        }
                        commonFields.put(entry.getKey(), fieldSet);
                    }
                }
            }
            constructFilterItem(commonFields);   
        
    }


    /**
     * Sets common HpccConnection Object to session
     */
    private void setCommonHpccConnection() {
        if(Sessions.getCurrent().getAttribute(Constants.HPCC_CONNECTION) == null) {
            Sessions.getCurrent().setAttribute(Constants.HPCC_CONNECTION, dashboard.getCommonHpccConnection());
        }
    }     


    /**
     * Listener for adding groups from available groups.
     */
    @Listen("onDrop = #addedGroups")
    public void onGroupDropped(final DropEvent event)  {
        final Listitem listItem = (Listitem) event.getDragged();
        final Group group = (Group) listItem.getAttribute(Constants.GROUP);
        boolean isDuplicateGroup = checkDuplicate(group);
        if(!isDuplicateGroup){
            //Setting default Consumer role
            group.setRole(Constants.ROLE_CONSUMER);
            
            addedGroups.appendChild(new GroupListitem(group, dashboardId));
            
            try {
                groupService.addGroup(dashboardId, group);
            } catch (DataAccessException ex) {
                Clients.showNotification("Unable to add Groups", Constants.ERROR_NOTIFICATION,
                        addGroup, Constants.POSITION_CENTER, 3000, true);
                LOG.error("Exception while adding groups", ex);
            }
        }else{
            Clients.showNotification("Group Exists", Constants.ERROR_NOTIFICATION,
                    addGroup, Constants.POSITION_CENTER, 3000, true);
        }
    }


    /**
     * Checks for group duplication
     * @param group
     * @return
     */
    private boolean checkDuplicate(Group group) {
        Group addedGroup = null;
        boolean isDuplicateGroup = false;
        for (Component element : addedGroups.getChildren()) {
            if(element instanceof Listitem) {
                addedGroup = (Group)element.getAttribute(Constants.GROUP);
            }
            if(addedGroup != null && addedGroup.getCode().equals(group.getCode())){
                isDuplicateGroup = true;
                break;
            }
            
        }    
    return isDuplicateGroup;
    }

    // To close the group popup window after groups added.
    @Listen("onClick=#doneBtn")
    public void click() {
        addGroup.close();
    }

    private void constructFilterItem(Map<String, Set<Field>> commonFields) {        
        Listbox listbox;
        Listitem listitem;
        Tab tab;
        Tabpanel tabpanel;
        for (Map.Entry<String, Set<Field>> entry : commonFields.entrySet()) {
            tab = new Tab(entry.getKey());
            tabpanel = new Tabpanel();
            tabpanel.setSclass("collapsiblePanel");
            listbox = new Listbox();
            listbox.setMultiple(false);
            listbox.setVflex(true);
            listbox.addEventListener(Events.ON_SELECT, fieldSelectListener);
            for (Field field : entry.getValue()) {
                listitem = new Listitem(field.getColumnName());
                listitem.setAttribute(Constants.FIELD, field);
                listbox.appendChild(listitem);
            }
            tabpanel.appendChild(listbox);
            commonFilterTabbox.getFirstChild().appendChild(tab);
            commonFilterTabbox.getLastChild().appendChild(tabpanel);
        }
    }    
    
    private void constructFilterItemForQuery(Map<String, Map<String,Set<String>>>  newInputParams){        
        Listbox listbox;
        Listitem listitem;
        Tab tab;
        Tabpanel tabpanel;
        for (Entry<String, Map<String, Set<String>>> entry : newInputParams.entrySet()) {
            tab = new Tab(entry.getKey());
            tab.setAttribute(Constants.INPUT_PARAM_NAMES,entry.getValue().keySet());
            tab.setAttribute(Constants.INPUT_PARAM_QUERY,entry.getKey());
            tabpanel = new Tabpanel();
            tabpanel.setSclass("collapsiblePanel");
            listbox = new Listbox();
            listbox.setMultiple(false);
            listbox.setVflex(true);
            listbox.addEventListener(Events.ON_SELECT, paramsSelectListener);
            
            for (Entry<String, Set<String>> inputParamsEntry : entry.getValue().entrySet()) {
                if(!inputParamsEntry.getValue().isEmpty()){
                    listitem = new Listitem(inputParamsEntry.getKey());
                    listitem.setAttribute(Constants.INPUT_PARAM_NAME, inputParamsEntry.getKey());
                     listitem.setAttribute(Constants.INPUT_PARAM_VALUE, inputParamsEntry.getValue());
                    listbox.appendChild(listitem);
                }
            }
            tabpanel.appendChild(listbox);
            commonFilterTabbox.getFirstChild().appendChild(tab);
            commonFilterTabbox.getLastChild().appendChild(tabpanel);
        }
    }
    
    EventListener<SelectEvent<Component, Object>> fieldSelectListener = new EventListener<SelectEvent<Component,Object>>() {
        
        @Override
        public void onEvent(SelectEvent<Component, Object> event) throws Exception {
            Listitem selectedItem = (Listitem) event.getSelectedItems().iterator().next();
            Tabpanel associatedTabpanel = (Tabpanel) selectedItem.getParent().getParent();
            
            String fileName = associatedTabpanel.getLinkedTab().getLabel();
            Field field = (Field) selectedItem.getAttribute(Constants.FIELD);
            
            Popup popup = (Popup) selectedItem.getParent().getParent().getParent().getParent().getParent();
            popup.close();
            
            //Removing selection
            selectedItem.setSelected(false);    
            
            Filter newFilter = new Filter();
            newFilter.setIsCommonFilter(true);
            newFilter.setColumn(field.getColumnName());
            newFilter.setFileName(fileName);
            
            //Checking whether filter is applied already
            if(appliedCommonFilters!= null && appliedCommonFilters.contains(newFilter)) {
                Clients.showNotification("This field is already filtered", Constants.ERROR_NOTIFICATION, commonFiltersPanel,Constants.POSITION_CENTER, 3000, true);
                return;
            }
            
            if(DashboardUtil.checkNumeric(field.getDataType())){
                newFilter.setType(Constants.DATA_TYPE_NUMERIC);
                filterRows.appendChild(createNumericFilterRow(newFilter));
            } else {
                newFilter.setType(Constants.DATA_TYPE_STRING);
                filterRows.appendChild(createStringFilterRow(newFilter));
            }
            
            List<Filter> filtersToReomove = null;
            for (Portlet portlet : dashboard.getPortletList()) {
                if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                        && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                        && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()
                        && portlet.getChartData().getIsFiltered()) {
                    
                    filtersToReomove = new ArrayList<Filter>();
                    for (Filter filter : portlet.getChartData().getFilters()) {
                        // overriding the portlet specific filter by selected global/dashboard filter
                        if (!filter.getIsCommonFilter()
                                && filter.equals(newFilter)) {
                            filtersToReomove.add(filter);                        
                        }
                    }
                    
                    //Removing portlet specific filters, when selecting the same global filter
                    if ( portlet.getChartData().getFilters().removeAll(filtersToReomove) ) {
                        if(portlet.getChartData().getFilters().isEmpty()){
                            portlet.getChartData().setIsFiltered(false);
                        }            
                        updateWidgets(portlet);
                    }
                }
                
            }
        }
    }; 
    
    
    
    EventListener<SelectEvent<Component, Object>> paramsSelectListener = new EventListener<SelectEvent<Component,Object>>() {
        
        @Override
        public void onEvent(SelectEvent<Component, Object> event) throws Exception {
            
            Listitem selectedItem = (Listitem) event.getSelectedItems().iterator().next();
            
            String inputName = (String) selectedItem.getAttribute(Constants.INPUT_PARAM_NAME);
            @SuppressWarnings("unchecked")
            Set<String> distinctValues = (Set<String>) selectedItem.getAttribute(Constants.INPUT_PARAM_VALUE);
            
            Popup popup = (Popup) selectedItem.getParent().getParent().getParent().getParent().getParent();
            popup.close();
            
            //Removing selection
            selectedItem.setSelected(false);    
            
            InputParam selectedParam=new InputParam(inputName);
            selectedParam.setIsCommonInput(true);
            
            //Checking whether filter is applied already
            if(appliedCommonInputParam!= null && appliedCommonInputParam.contains(selectedParam)) {
                Clients.showNotification("This input is already filtered", Constants.ERROR_NOTIFICATION, commonFiltersPanel,Constants.POSITION_CENTER, 3000, true);
                return;
            }
           
            filterRows.appendChild(createQueryInputFilterRow(selectedParam,distinctValues));
            
            for (Portlet portlet : dashboard.getPortletList()) {
                if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                        && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                        && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()
                        && portlet.getChartData().getIsFiltered()) {
                    
                    List<InputParam> filtersToReomove  = new ArrayList<InputParam>();
                    portlet.getChartData().getInputParams().forEach(widgetInputParam ->{
                        // overriding the portlet specific filter by selected global/dashboard filter
                        if (!widgetInputParam.getIsCommonInput()
                                && widgetInputParam.equals(selectedParam)) {
                            filtersToReomove.add(selectedParam);                        
                        }
                    });
                    
                    //Removing portlet specific filters, when selecting the same global filter
                    if ( portlet.getChartData().getInputParams().removeAll(filtersToReomove)) {
                        updateWidgets(portlet);
                    }
                }
                
            }
        }
    }; 
    
    /**
     * Updates charts in the portlet passed
     * 
     * @param portlet
     * @param portalChildren
     * @throws HpccConnectionException  
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws ServiceException 
     * @throws EncryptDecryptException 
     * @throws XPathExpressionException 
     * @throws Exception
     */
    public void updateWidgets(Portlet portlet) throws JAXBException
            ,DataAccessException, ServiceException,
             ParserConfigurationException, SAXException, IOException,
             HpccConnectionException, EncryptDecryptException, XPathExpressionException {

        if(LOG.isDebugEnabled()){
            LOG.debug("Updating charts in portlet - " + portlet);
        }
        Portalchildren children = portalChildren.get(portlet.getColumn());
        
        ChartPanel panel =null;
        for (Component comp : children.getChildren()) {
            panel = (ChartPanel) comp;
            if (panel.getPortlet().getId() == portlet.getId()) {
                break;
            }
        }
        
        //Updating widget with latest filter details into DB
       widgetService.updateWidget(portlet);
        
       Events.postEvent("onChangeInputParamChangeTitle",panel, null);
       
        if(Constants.CATEGORY_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){    
            //Refreshing table with updated filter values
            panel.drawTableWidget(true);
        }else{
            ChartRenderer chartRenderer = (ChartRenderer) SpringUtil.getBean("chartRenderer");
            //Refreshing chart with updated filter values
            
             if (Constants.CATEGORY_XY_CHART == chartService.getCharts().get(portlet.getChartType())
                    .getCategory()    || Constants.CATEGORY_PIE == chartService.getCharts().get(portlet.getChartType())
                            .getCategory()) {
                chartRenderer.constructChartJSON((XYChartData) portlet.getChartData(), portlet, false);
        
            } else if (Constants.CATEGORY_HIERARCHY ==  chartService.getCharts().get(portlet.getChartType())
                    .getCategory()) {  
                //setting tree data into session with id as 'dashboardId_portletId'
                HttpSession httpSession = (HttpSession) Executions.getCurrent().getSession().getNativeSession();
                StringBuilder builder = new StringBuilder();
                builder.append(dashboardId).append("_").append(portlet.getId());
                httpSession.setAttribute(builder.toString(), (TreeData) portlet.getChartData());
                
                chartRenderer.constructTreeJSON((TreeData) portlet.getChartData(), portlet,builder.toString());
            }
                
            if (panel.drawD3Graph() != null) {
                    Clients.evalJavaScript(panel.drawD3Graph());
            }
        }
    }
    
    /**
     * Event listener to be invoked when a new live chart is drawn.
     * Adds new files' columns to the displayed Filter Column list 
     */
    EventListener<Event> onDrawingLiveChart = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Portlet portlet = (Portlet) event.getData();
            Map<String, Set<Field>> newFiles;
            Map<String, Map<String,Set<String>>> newInputParams;
            if(dashboard.getHasCommonFilter() && 
                    Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState()) 
                    && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                    && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                
                if(commonInputParams == null){
                    commonInputParams =  new LinkedHashMap<String, Map<String,Set<String>>>();
                }                
                //As dashboard has commonfilter enabled,setting ''hasCommonFileter' true for the  chart panel
                //It will tell whether to fetch the fresh hpcc data while selecting the inputparam button.
                List<Component> chartPanels = portalChildren.get(portlet.getColumn()).getChildren();
                ChartPanel presentChartpanel = (ChartPanel) chartPanels.stream()
                        .filter(panel -> portlet.getId().equals(
                                ((ChartPanel) panel).getPortlet().getId()))
                        .findFirst().get();
                presentChartpanel.setAttribute(Constants.COMMON_FILTERS_ENABLED, true);
                
                newFiles = new LinkedHashMap<String, Set<Field>>();
                Set<Field> fieldSet;
                if(!portlet.getChartData().getIsQuery()){
                    for (String fileName : portlet.getChartData().getFiles()) {
                        //Checking if the file is already present in filters
                        if(!commonFields.containsKey(fileName)) {
                            fieldSet = new LinkedHashSet<Field>();
                            fieldSet.addAll(portlet.getChartData().getFields().get(fileName));
                            newFiles.put(fileName, fieldSet);
                        }
                    }
                    
                    constructFilterItem(newFiles);
                }else{
                    newInputParams = new HashMap<String, Map<String,Set<String>>>();
                    for (String fileName : portlet.getChartData().getFiles()) {
                        //Checking if the query is already present in filters
                        if(!commonInputParams.containsKey(fileName)) {
                           QuerySchema querySchema = hpccQueryService.getQuerySchema(fileName, portlet
                                    .getChartData().getHpccConnection(), portlet
                                    .getChartData().isGenericQuery(), portlet
                                    .getChartData().getInputParamQuery());
                      
                            newInputParams.put(fileName, querySchema.getInputParams());
                        }
                    }
                    constructFilterItemForQuery(newInputParams);
                }
            }
        }
    };
    
    private Row createQueryInputFilterRow(InputParam inputparam, Set<String> inputDistinctValues)
            throws HpccConnectionException,RemoteException {
        Row row = new Row();
       
        //if selected any filter values,set Row selected as true
        if(inputparam.getValue() != null){
            row.setAttribute(Constants.ROW_CHECKED, true);
        }else{
            row.setAttribute(Constants.ROW_CHECKED, false);
        }
        if(appliedCommonInputParam== null ){
            appliedCommonInputParam = new HashSet<InputParam>();
        }
        Sessions.getCurrent().setAttribute(Constants.COMMON_FILTERS, appliedCommonInputParam);
        appliedCommonInputParam.add(inputparam);
        dashboard.addCommonQueryFilter(inputparam);
        
        row.setAttribute(Constants.INPUT_PARAM_NAME, inputparam.getName());
        row.setAttribute(Constants.INPUT_PARAM_VALUE, inputDistinctValues);
        
        Div div = new Div();
        Label label = new Label(inputparam.getName());
        label.setSclass("h5");
        div.appendChild(label);
        Button button = new Button();
        button.setSclass("glyphicon glyphicon-remove-circle btn btn-link img-btn");
        button.setStyle("float: right;");
        button.addEventListener(Events.ON_CLICK, removeGlobalFilter);
        div.appendChild(button);
        
        row.appendChild(div);
        
        Anchorlayout anchorlayout = new Anchorlayout();
        anchorlayout.setHflex("1");
        
        Hbox hbox = new Hbox();
        hbox.appendChild(anchorlayout);
        
        // Current implementation assumes, in a dashboard, 
        // there are no two widgets drawn such that their file names are same but belong to different HPCC Systems
        
        //Generating Checkboxes
        Anchorchildren anchorchildren;
        Radio radio;
        for (String value : inputDistinctValues) {
            anchorchildren = new Anchorchildren();
            radio = new Radio(value);
            radio.setZclass("checkbox");
            radio.setStyle("margin: 0px; padding-right: 5px;");
            
            anchorchildren.appendChild(radio);
            anchorlayout.appendChild(anchorchildren);
            //To display previously selected filter values
            if(value.equals(inputparam.getValue())){
                radio.setChecked(true);
                row.setAttribute(Constants.ROW_CHECKED, true);
                row.setAttribute(Constants.SELECTED_RADIO_BTN, radio);
            }
            radio.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

                @Override
                public void onEvent(Event event) throws Exception {
                    Radio radio = (Radio) event.getTarget();
                    Row row = (Row) radio.getParent().getParent().getParent().getParent();

                    row.setAttribute(Constants.ROW_CHECKED, true);
                    inputparam.setValue(radio.getLabel());
                    inputparam.setIsCommonInput(true);
                    applyInputParamToPortlets(inputparam, true);
                    
                   if(row.getAttribute(Constants.SELECTED_RADIO_BTN) != null){
                      ( (Radio)row.getAttribute(Constants.SELECTED_RADIO_BTN)).setSelected(false);
                   }
                   row.setAttribute(Constants.SELECTED_RADIO_BTN, radio);
                   
                // Set Common HpccConnection to session, if this is first common filter applied
                   setCommonHpccConnection();
                }
            });
        }
        
        row.appendChild(hbox);
        
        return row;
    }    
    
    
    @SuppressWarnings("unchecked")
    protected void applyInputParamToPortlets(InputParam inputparam, boolean redrawCharts) {

        for (Portlet portlet : dashboard.getPortletList()) {

            if (!Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                    || Constants.CATEGORY_TEXT_EDITOR == chartService.getCharts().get(portlet.getChartType()).getCategory()
                    || Constants.CATEGORY_SCORED_SEARCH_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                continue;
            }
            ChartData chartData = DashboardUtil.getChartData(portlet);
           
            boolean hasSelectedInputparam =false;
            if(userCredential.hasRole(Constants.ROLE_API_VIEW_DASHBOARD)){
                hasSelectedInputparam = true;
            }
            //Checks this portlet query has inputparam with selected inputparam name
            try{
                for(String file : chartData.getFiles()){
                    Optional<Component> option = commonFilterTabbox.getFirstChild().getChildren().stream().filter(component ->
                    (component.getAttribute(Constants.INPUT_PARAM_QUERY).equals(file))).findFirst();
                    Component tab = null;
                    if(option.isPresent()){
                        tab = option.get();
                    }
                   if(tab != null && ((Set<String>)tab.getAttribute(Constants.INPUT_PARAM_NAMES)).contains(inputparam.getName())){
                       hasSelectedInputparam = true;
                   }
               }
            }catch(NoSuchElementException e){
                //Need not to log this exception
            }
             
             if(hasSelectedInputparam){
                 if(chartData.getInputParams() == null){
                     List<InputParam>  inputs = new ArrayList<InputParam>();
                     chartData.setInputParams(inputs);
                 }
                 if(chartData.getInputParams().contains(inputparam)) {
                     chartData.getInputParams().remove(inputparam);
                 }
                 chartData.getInputParams().add(inputparam);
                 if(redrawCharts) {
                     try {
                         updateWidgets(portlet);
                     } catch (Exception e) {
                         LOG.error("Error Updating Charts", e);
                        Clients.showNotification("Unable to update charts", Clients.NOTIFICATION_TYPE_ERROR, this.getSelf(), Constants.POSITION_CENTER, 5000, true);
                     }
                 }
             }
            
           
        }
    
        
    }


    /**
     * Creates a row of Common filters with a list of Distinct Values 
     * from the specified filter column from all datasets present in the Dashboard
     * 
     * @param field
     * @param filter
     * @return
     *     Constructed row
     * @throws Exception
     */
    private Row createStringFilterRow(Filter filter) throws HpccConnectionException,RemoteException {
        Row row = new Row();
        //if selected any filter values,set Row selected as true
        if(filter.getValues() != null && !filter.getValues().isEmpty()){
            row.setAttribute(Constants.ROW_CHECKED, true);
        }else{
            row.setAttribute(Constants.ROW_CHECKED, false);
        }
        if(appliedCommonFilters== null ){
            appliedCommonFilters = new HashSet<Filter>();
        }
        Sessions.getCurrent().setAttribute(Constants.COMMON_FILTERS, appliedCommonFilters);
        appliedCommonFilters.add(filter);
        
        row.setAttribute(Constants.FILTER, filter);
        
        Div div = new Div();
        Label label = new Label(filter.getColumn());
        label.setSclass("h5");
        div.appendChild(label);
        Button button = new Button();
        button.setSclass("glyphicon glyphicon-remove-circle btn btn-link img-btn");
        button.setStyle("float: right;");
        button.addEventListener(Events.ON_CLICK, removeGlobalFilter);
        div.appendChild(button);
        
        Anchorlayout anchorlayout = new Anchorlayout();
        anchorlayout.setHflex("1");
        
        // Current implementation assumes, in a dashboard, 
        // there are no two widgets drawn such that their file names are same but belong to different HPCC Systems
        //TODO: Handle same filenames in multiple HPCC Systems
        Set<String> values = new LinkedHashSet<String>();
        for (Portlet portlet : dashboard.getPortletList()) {
            if(portlet.getWidgetState().equals(Constants.STATE_LIVE_CHART)
                    && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                    && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()
                    && portlet.getChartData().getFiles().contains(filter.getFileName())) {
                values.addAll(
                        hpccService.getDistinctValues(filter.getColumn(), filter.getFileName(),portlet.getChartData(), false));
            }
        }
        Boolean showApplyButton = false;
        if(values.size() > 5){
            showApplyButton = true;
        }
        
        //Generating Checkboxes
        Anchorchildren anchorchildren;
        Checkbox checkbox;
        for (String value : values) {
            anchorchildren = new Anchorchildren();
            checkbox = new Checkbox(value);
            checkbox.setZclass("checkbox");
            checkbox.setStyle("margin: 0px; padding-right: 5px;");
            if(showApplyButton){
                checkbox.addEventListener(Events.ON_CHECK, stringFilterMultiCheckListener);
            } else {
                checkbox.addEventListener(Events.ON_CHECK, stringFilterCheckListener);
            }
            anchorchildren.appendChild(checkbox);
            anchorlayout.appendChild(anchorchildren);
            //To display previously selected filter values
            if(filter != null && filter.getValues() != null && filter.getValues().contains(value)){
                checkbox.setChecked(true);
                row.setAttribute(Constants.ROW_CHECKED, true);
            }
        }
        
        row.appendChild(div);
        
        Hbox hbox = new Hbox();
        hbox.appendChild(anchorlayout);
        
        if(showApplyButton) {
            Button applyButton = new Button("Apply");
            applyButton.setZclass("btn btn-xs");
            applyButton.setSclass("btn-default");
            applyButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

                @Override
                public void onEvent(Event event) throws Exception {
                    Button button = (Button) event.getTarget();
                    Row row = (Row) button.getParent().getParent();

                    row.setAttribute(Constants.ROW_CHECKED, true);
                    
                    Filter filter = (Filter) row.getAttribute(Constants.FILTER);
                    
                    if(filter.getValues() != null) {
                        updateStringFilterToPortlets(filter);
                        button.setSclass("btn-default");
                    } else {
                        Clients.showNotification(Labels.getLabel("selectValueToApply"), "warning", row, "after_center", 2000);
                    }
                }
                
            });
            hbox.appendChild(applyButton);
        }
                
        row.appendChild(hbox);
        return row;
    }    
    
    /**
     * Event listener for string filters when there's a separate apply button.
     */
    EventListener<Event> stringFilterMultiCheckListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Anchorlayout anchorlayout = (Anchorlayout) event.getTarget().getParent().getParent();
            Hbox hbox = (Hbox) anchorlayout.getParent();
            Row row = (Row) hbox.getParent();
            Filter filter = (Filter) row.getAttribute(Constants.FILTER);
            row.setAttribute(Constants.ROW_CHECKED, true);
            //Instantiating Value list if empty
            if(filter.getValues() == null){
                List<String> values = new ArrayList<String>();
                filter.setValues(values);
            }
            
            //Updating change to filter object
            for (Component component : anchorlayout.getChildren()) {
                Checkbox checkbox = (Checkbox) component.getFirstChild();
                String value = checkbox.getLabel();
                if(checkbox.isChecked()){
                    if(!filter.getValues().contains(value)) {
                        filter.getValues().add(value);
                    }
                } else {
                    filter.getValues().remove(value);
                }
            }
            
            Button button = (Button) hbox.getLastChild();
            button.setSclass("btn-warning");
            
            // Set Common HpccConnection to session, if this is first common filter applied
            setCommonHpccConnection();
        }
    };
    
    /**
     * Creates a row with Slider for Numeric Filters
     * 
     * @param field
     * @param filter
     * @return
     * @throws Exception
     */
    private Row createNumericFilterRow(Filter filter)
            throws HpccConnectionException,RemoteException {
        Row row = new Row();
        //if selected any filter values,set Row selected as true
        if(filter.getStartValue() != null && filter.getEndValue() != null){
            row.setAttribute(Constants.ROW_CHECKED, true);
        }else{
            row.setAttribute(Constants.ROW_CHECKED, false);
        }
        if(appliedCommonFilters == null ){
            appliedCommonFilters = new HashSet<Filter>();
        }
        Sessions.getCurrent().setAttribute(Constants.COMMON_FILTERS, appliedCommonFilters);
        appliedCommonFilters.add(filter);
        
        row.setAttribute(Constants.FILTER, filter);
        
        Div div = new Div();
        Label label = new Label(filter.getColumn());
        label.setSclass("h5");
        div.appendChild(label);
        Button button = new Button();
        button.setSclass("glyphicon glyphicon-remove-circle btn btn-link img-btn");
        button.setStyle("float: right;");
        button.addEventListener(Events.ON_CLICK, removeGlobalFilter);
        div.appendChild(button);
        
        
        // Current implementation assumes, in a dashboard, 
        // there are no two widgets drawn such that their file names are same but belong to different HPCC Systems
        //TODO: Handle same filenames in multiple HPCC Systems
        BigDecimal min = null;
        BigDecimal max = null;
        for (Portlet portlet : dashboard.getPortletList()) {
            if(portlet.getWidgetState().equals(Constants.STATE_LIVE_CHART) 
                    && portlet.getChartData().getFiles().contains(filter.getFileName())) {
                Map<Integer, BigDecimal> map = hpccService.getMinMax(filter.getColumn(),filter.getFileName(), portlet.getChartData(), false);
                min = map.get(Constants.FILTER_MINIMUM);
                max = map.get(Constants.FILTER_MAXIMUM);
            }
        }
        
        //Intitialising Slider positions
        Integer sliderStart = 0;
        Integer sliderEnd = 100;
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("Min & Max - " + min + max);
        }
        
        //Translating min & max to a scale of 0 to 100 using Linear equation 
        //((actualVal - actualMin)/(actualMax- actualMin)) = ((sliderVal - sliderMin)/(sliderMax- sliderMin))
        // Range Factor = (actualMax- actualMin)/(sliderMax- sliderMin)
        BigDecimal rangeFactor = max.subtract(min).divide(new BigDecimal(100));
        
        if(filter.getStartValue() != null && filter.getEndValue() != null) {
            //Updating slider positions for already applied filters
            sliderStart = filter.getStartValue().subtract(min).divide(rangeFactor, RoundingMode.DOWN).intValue();
            sliderEnd = filter.getEndValue().subtract(min).divide(rangeFactor, RoundingMode.CEILING).intValue();
        } else {
            filter.setStartValue(min);
            filter.setEndValue(max);
        }
        
        //Adding minimum and Range factor to Row to resume calculations on listener
        row.setAttribute(MINIMUM_VALUE, min);
        row.setAttribute(RANGE_FACTOR, rangeFactor);
        
        Hbox hbox = new Hbox();
        //Setting Id to be passed from Java script
        hbox.setId(filter.getFileName().replaceAll(":", "") + filter.getColumn() + "_hbox");
        
        Label minLabel = new Label(String.valueOf(filter.getStartValue().intValue()));
        minLabel.setWidth("75px");
        Label maxLabel = new Label(String.valueOf(filter.getEndValue().intValue()));
        maxLabel.setWidth("75px");
        
        Div sliderDiv = new Div();
        sliderDiv.setWidth("300px");
        
        String id = filter.getFileName().replaceAll(":", "") + filter.getColumn();
        
        StringBuilder html = new StringBuilder();
        html.append("<div id=\"");
            html.append(id);
            html.append("_sdiv\" style=\"margin: 8px;\" class=\"slider-grey\">");
            html.append("</div>");
        
        html.append("<script type=\"text/javascript\">");
            html.append("$('#").append(id).append("_sdiv').slider({")
                .append("range: true,")
                .append("values: [").append(sliderStart).append(", ").append(sliderEnd).append("]")
                .append("});");
    
            html.append("$('#").append(id).append("_sdiv').on( \"slide\", function( event, ui ) {")
                .append("payload = \"").append(id).append("_hbox,\" + ui.values;")
                .append("zAu.send(new zk.Event(zk.Widget.$('$")
                    .append("dashboardWin").append("'), 'onSlide', payload, {toServer:true}));")
                .append("});");
            
            html.append("$('#").append(id).append("_sdiv').on( \"slidestop\", function( event, ui ) {")
                .append("payload = \"").append(id).append("_hbox,\" + ui.values;")
                .append("zAu.send(new zk.Event(zk.Widget.$('$")
                    .append("dashboardWin").append("'), 'onSlideStop', payload, {toServer:true}));")
                .append("});");
        html.append("</script>");
        
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("Generated HTML " + html.toString());
        }
        
        sliderDiv.appendChild(new Html(html.toString()));
        
        Div holder = new Div();
        holder.setStyle("min-width: 70px; text-align: end;");
        holder.appendChild(minLabel);
        hbox.appendChild(holder);
        
        hbox.appendChild(sliderDiv);
        
        holder = new Div();
        holder.setStyle("min-width: 70px");
        holder.appendChild(maxLabel);
        hbox.appendChild(holder);
        
        row.appendChild(div);
        row.appendChild(hbox);
        return row;
    }
    
    /**
     * Listener event, to be triggered by Slider from Client
     * Event is triggered onSlide
     * 
     * @param event
     */
    @Listen("onSlide = #dashboardWin")
    public void onSlide(Event event) {
        String[] data = ((String) event.getData()).split(",");
        
        Hbox hbox = (Hbox) this.getSelf().getFellow(data[0]);
        Row row = (Row) hbox.getParent();
        
        BigDecimal min = (BigDecimal) row.getAttribute(MINIMUM_VALUE);
        BigDecimal rangeFactor = (BigDecimal) row.getAttribute(RANGE_FACTOR);
        
        Integer startPosition = Integer.valueOf(data[1]);
        Integer endPosition = Integer.valueOf(data[2]);
        
        Label minLabel = (Label) hbox.getFirstChild().getFirstChild();
        Label maxLabel = (Label) hbox.getLastChild().getFirstChild();
        
        //Converting position into value
        // value = pos . rangeFactor + min  
        minLabel.setValue(String.valueOf(rangeFactor.multiply(new BigDecimal(startPosition)).add(min).intValue()));
        maxLabel.setValue(String.valueOf(rangeFactor.multiply(new BigDecimal(endPosition)).add(min).intValue()));
    }

    /**
     * Listener event, to be triggered by Slider from Client
     * Event is triggered when the slider id stopped after sliding
     * @param event
     * @throws Exception 
     */
    @Listen("onSlideStop = #dashboardWin")
    public void onSlideStop(Event event) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("On Slide Stop Event - Data -- " + event.getData());
        }
        
        String[] data = ((String) event.getData()).split(",");
        
        Hbox hbox = (Hbox) this.getSelf().getFellow(data[0]);
        Row row = (Row) hbox.getParent();
        row.setAttribute(Constants.ROW_CHECKED, true);
        Filter filter = (Filter) row.getAttribute(Constants.FILTER);
        
        BigDecimal min = (BigDecimal) row.getAttribute(MINIMUM_VALUE);
        BigDecimal rangeFactor = (BigDecimal) row.getAttribute(RANGE_FACTOR);
        
        Integer startPosition = Integer.valueOf(data[1]);
        Integer endPosition = Integer.valueOf(data[2]);
        
        //Updating Change to filter object
        // value = pos . rangeFactor + min  
        filter.setStartValue(rangeFactor.multiply(new BigDecimal(startPosition)).add(min));
        filter.setEndValue(rangeFactor.multiply(new BigDecimal(endPosition)).add(min));
        
        for (Portlet portlet : dashboard.getPortletList()) {
            if(!Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                    || Constants.CATEGORY_TEXT_EDITOR ==  chartService.getCharts().get(portlet.getChartType()).getCategory()
                    || Constants.CATEGORY_SCORED_SEARCH_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                continue;
            }
            
            ChartData chartData = null;
            if (Constants.CATEGORY_XY_CHART == chartService.getCharts().get(portlet.getChartType())
                    .getCategory()    || Constants.CATEGORY_PIE == chartService.getCharts().get(portlet.getChartType())
                            .getCategory() || Constants.CATEGORY_USGEO == chartService.getCharts().get(portlet.getChartType())
                                    .getCategory()) {
                chartData = (XYChartData) portlet.getChartData();
        
            } else if (Constants.CATEGORY_HIERARCHY ==  chartService.getCharts().get(portlet.getChartType())
                    .getCategory()) {    
                chartData = (TreeData) portlet.getChartData();
            }else if(Constants.CATEGORY_TABLE ==  chartService.getCharts().get(portlet.getChartType())
                    .getCategory()){
                chartData = (TableData) portlet.getChartData();                
            }else if(Constants.CATEGORY_CLUSTER ==  chartService.getCharts().get(portlet.getChartType())
                    .getCategory()){
                 chartData = (ClusterData) portlet.getChartData();
            }
            else if(Constants.CATEGORY_GAUGE ==  chartService.getCharts().get(portlet.getChartType())
                    .getCategory()){
                 chartData = (GaugeChartData) portlet.getChartData();
                
            } 
            
            if(chartData.getFiles().contains(filter.getFileName())) {
                //Overriding filter if applied already
                if(chartData.getIsFiltered() && chartData.getFilters().contains(filter)){
                    chartData.getFilters().remove(filter);
                    chartData.getFilters().add(filter);
                } else {
                    chartData.setIsFiltered(true);
                    chartData.getFilters().add(filter);
                }
                
                try {
                    updateWidgets(portlet);
                } catch (Exception e) {
                    LOG.error("Error Updating Charts", e);
                    //TODO: Show Notification
                }
            }
        }
        
        // Set Common HpccConnection to session, if this is first common filter applied
        setCommonHpccConnection();
    }

    
    /**
     * Listener to remove global filters
     */
    EventListener<MouseEvent> removeGlobalFilter = new EventListener<MouseEvent>() {

        @Override
        public void onEvent(MouseEvent event) throws Exception {
            
            Row removedRow = (Row) event.getTarget().getParent().getParent();
            Boolean rowChecked = (Boolean)removedRow.getAttribute(Constants.ROW_CHECKED);
            
            //refresh the portlets, if the removed row/filter has any checked values
            if(rowChecked){
                Iterator<Portlet> iterator =null;
                if(Constants.LOGICAL_FILE.equals(dashboard.getFileType())){
                    iterator = removeFilter(removedRow).iterator();
                    
                    Filter removedFilter = (Filter)removedRow.getAttribute(Constants.FILTER);
                    //Need To remove the filter from applied filter set
                    appliedCommonFilters.remove(removedFilter);
                }else if(Constants.QUERY.equals(dashboard.getFileType())){
                    iterator = removeInputparam(removedRow).iterator();
                    
                    String removedInput =  removedRow.getAttribute(Constants.INPUT_PARAM_NAME).toString();
                    InputParam removedInputparam = new InputParam(removedInput);
                    //Need To remove the filter from applied inputparam set
                    appliedCommonInputParam.remove(removedInputparam);
                    if(dashboard.getCommonQueryFilters() != null){
                        dashboard.getCommonQueryFilters().remove(removedInputparam);
                    }
                }
            
            // refreshing the chart && updating DB
            while (iterator.hasNext()) {
                Portlet portlet = iterator.next();
                updateWidgets(portlet);
                }
            }
            
            //Removing the Filter row in UI
            removedRow.detach();
        }
    };

    /**
     * Event to be triggered when any filter value is checked or Unchecked
     * @param event
     */
    EventListener<CheckEvent> stringFilterCheckListener = new EventListener<CheckEvent>() {
        
        @Override
        public void onEvent(CheckEvent event) throws Exception {
            Anchorlayout anchorlayout = (Anchorlayout) event.getTarget().getParent().getParent();
            Row row = (Row) anchorlayout.getParent().getParent();
            Filter filter = (Filter) row.getAttribute(Constants.FILTER);
            row.setAttribute(Constants.ROW_CHECKED, true);
            //Instantiating Value list if empty
            if(filter.getValues() == null) {
                List<String> values = new ArrayList<String>();
                filter.setValues(values);
            }
            
            //Updating change to filter object
            for (Component component : anchorlayout.getChildren()) {
                Checkbox checkbox = (Checkbox) component.getFirstChild();
                String value = checkbox.getLabel();
                if(checkbox.isChecked()){
                    if(!filter.getValues().contains(value)) {
                        filter.getValues().add(value);
                    }
                } else {
                    filter.getValues().remove(value);
                }
            }
            
            if(LOG.isDebugEnabled()){
                LOG.debug("Selected Filter Values -> " + filter.getValues());
            }
            
            updateStringFilterToPortlets(filter);
            
            // Set Common HpccConnection to session, if this is first common filter applied
            setCommonHpccConnection();
        }
    };
    
    /**
     * Updates portlet objects on the dashboard according to the filter object passed.
     * 
     * @param filter
     * @throws Exception 
     */
    private void updateStringFilterToPortlets(Filter filter)  {
        for (Portlet portlet : dashboard.getPortletList()) {
            
            if(!Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                    || Constants.CATEGORY_TEXT_EDITOR ==  chartService.getCharts().get(portlet.getChartType()) .getCategory() 
                    || Constants.CATEGORY_SCORED_SEARCH_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                continue;
            }
            ChartData chartData = DashboardUtil.getChartData(portlet);
            
            if(filter.getValues().isEmpty()) {
                //Removing Filter if no values selected
                if(chartData.getIsFiltered()){
                    chartData.getFilters().remove(filter);
                    if(chartData.getFilters().isEmpty()) {
                        chartData.setIsFiltered(false);
                    }
                }
            } else {
                // Adding Filter to Portlets
                if(chartData.getFiles().contains(filter.getFileName())) {
                    //Overriding filter if applied already
                    if(chartData.getIsFiltered() && chartData.getFilters().contains(filter)){
                        chartData.getFilters().remove(filter);
                        chartData.getFilters().add(filter);
                    } else {
                        chartData.setIsFiltered(true);
                        chartData.getFilters().add(filter);
                    }
                    
                }
            }
            if(chartData.getFiles().contains(filter.getFileName())){
                try {
                    updateWidgets(portlet);
                } catch (Exception e) {
                    LOG.error("Error Updating Charts", e);
                    Clients.showNotification(Labels.getLabel("unableToUpdateWidget"),
                            Clients.NOTIFICATION_TYPE_ERROR, this.getSelf(), Constants.POSITION_CENTER, 3000, true);
                }
            }
        }
    }
    
    /**
     * Returns the list of portlets which has the removed inputparam
     * @param removedRow
     * @return  Set<Portlet>
     */
    private  List<Portlet>  removeInputparam(Row removedRow) {
        
        String removedInput =  removedRow.getAttribute(Constants.INPUT_PARAM_NAME).toString();
        InputParam removedInputparam = new InputParam(removedInput);
        dashboard.getCommonQueryFilters().remove(removedInputparam);
        List<Portlet> portletsToRefresh = dashboard.getPortletList().stream().filter(portlet ->
        (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()
                && portlet.getChartData().getInputParams().contains(removedInputparam))).collect(Collectors.toList());
        
        portletsToRefresh.forEach(portlet ->{
            portlet.getChartData().getInputParams().remove(removedInputparam);
            //resetting the title column value to null while removing a inputparam
            if(portlet.getTitleColumns() != null){
                TitleColumn titleCol = new TitleColumn("",  removedInputparam.getName());
                if(portlet.getTitleColumns().contains(titleCol)){
                    portlet.getTitleColumns().get(portlet.getTitleColumns().indexOf(titleCol)).setValue(null);
                }
            }
        });
        return portletsToRefresh;
    
    }


    /**
     * Event to be triggered onClick of 'Add Widget' Button
     */
    @Listen("onClick = #addWidget")
    public void addWidget() {
        ChartPanel chartPanel=null;
        try {
            final Portlet portlet = new Portlet();
            
            portlet.setWidgetState(Constants.STATE_EMPTY);
            dashboard.getPortletList().add(portlet);
            
            // Adding new Widget to the column with lowest number of widgets
            Integer count = 0, childCount = 0, column = 0;
            for (Portalchildren portalchildren : portalChildren) {
                if(! (count < dashboard.getColumnCount())) {
                    break;
                }
                if(portalchildren.getChildren().size() < childCount) {
                    column = count;
                }
                childCount = portalchildren.getChildren().size();
                count ++;
            }
            portlet.setColumn(column);
            
            //Assuming ChartPanel is configurable, as 'Add Widget' is enabled
            chartPanel = new ChartPanel(portlet,Constants.SHOW_ALL_BUTTONS);
            portalChildren.get(portlet.getColumn()).appendChild(chartPanel);
            chartPanel.focus();
            
            reorderPortletPanels();
            
            widgetService.addWidget(dashboardId, portlet, dashboard.getPortletList().indexOf(portlet));
            
            //Updating new widget sequence to DB
            widgetService.updateWidgetSequence(dashboard);
        } catch (DataAccessException e) {
            LOG.error(Constants.EXCEPTION, e);
            Clients.showNotification(Labels.getLabel("widgetHaventSaved"), Constants.ERROR_NOTIFICATION, chartPanel, Constants.POSITION_CENTER, 5000, true);
        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION, e);
            Clients.showNotification(Labels.getLabel("widgetHaventSaved"), Constants.ERROR_NOTIFICATION, chartPanel, Constants.POSITION_CENTER, 5000, true);
        }
        
    }
    
    /**
     * Event to be triggered onClick of 'Configure Dashboard' Button
     * @param event
     */
    @Listen("onClick = #configureDashboard")
    public void configureDashboard(Event event) {
        oldColumnCount = dashboard.getColumnCount();
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Constants.PARENT, this.getSelf());
        parameters.put(Constants.DASHBOARD, dashboard);
        
        Window window  = (Window) Executions.createComponents("/demo/layout/dashboard_config.zul", this.getSelf(), parameters);
        window.doModal();
    }
    
    private void reorderPortletPanels() {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Reordering portlets.");
            LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
        }
        List<Portlet> newPortletList = new ArrayList<Portlet>();
        short portletChild=0;
        int colCount=0;
        Iterator<Component> iterator=null;
        
        Component component=null;
        Portlet portlet=null;
        do {
            if(!portalChildren.get(portletChild).getChildren().isEmpty()) {
                iterator = (Iterator<Component>) portalChildren.get(portletChild).getChildren().iterator();
                while(iterator.hasNext()) {
                     component = iterator.next();
                     ChartPanel panel = (ChartPanel)component;
                     portlet = panel.getPortlet();
                     portlet.setColumn(colCount);
                     newPortletList.add(portlet);
                 }
                colCount++;
            }
            portletChild++;
            
        } while(portletChild<3);
        
        dashboard.setPortletList(newPortletList);
    }
    
    private void resizePortletPanels() {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Resizing portlet children");
            LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
        }
        Integer counter = 0;
        for(final Portalchildren portalChildren : this.portalChildren) {
            if(counter < dashboard.getColumnCount()){
                portalChildren.setVisible(true);
                portalChildren.setWidth((100/dashboard.getColumnCount()) + PERCENTAGE_SIGN);
                final List<Component> list = portalChildren.getChildren();
                for (final Component component1 : list) {
                        final ChartPanel panel = (ChartPanel) component1;
                        if(panel.drawD3Graph() != null) {
                            Clients.evalJavaScript(panel.drawD3Graph());
                        }
                }
            } else {
                portalChildren.setVisible(false);
            }
            counter ++;
        }
    }
    
    /**
     * Event listener to listen to 'Dashboard Configuration'
     */
    final EventListener<Event> onLayoutChange = new EventListener<Event>() {

        @Override
        public void onEvent(Event event) throws Exception {         
        
            // Check if any visible panels are hidden when layout is changed
            if(dashboard.getColumnCount() < oldColumnCount) {
                //List to capture hidden panels
                List<Component> hiddenPanels = new ArrayList<Component>();
                
                Integer counter = 0;
                for (Portalchildren component : portalChildren) {
                    if( !(counter < dashboard.getColumnCount()) ) {
                        hiddenPanels.addAll(component.getChildren());
                        component.getChildren().clear();
                    }
                    counter ++;
                }
                
                //Adding hidden panels to last visible column 
                for (Component component : hiddenPanels) {
                    if(component instanceof ChartPanel) {
                        portalChildren.get(dashboard.getColumnCount() -1).appendChild(component);
                    }
                }
            }
            
            //To update Dashboard Name
            onNameChange();
            
            //Showing Common filters panel
            if(dashboard.getHasCommonFilter()){
                
                setPanelCommonfilterIndicator();
                
                if(Constants.LOGICAL_FILE.equals(dashboard.getFileType())) {
                    if(commonFields == null){
                        commonFields  = new LinkedHashMap<String, Set<Field>>();
                    }
                 // Retrieving missing columns to be displayed in list of common filter columns
                    Map<String,Set<Field>> newCommonFields = new LinkedHashMap<String, Set<Field>>();
                    Set<Field> fields;
                    for (Portlet portlet : dashboard.getPortletList()) {
                        // Add a condition to validate all portlets having same
                        // hpcc connection for applied filters for dashboard.
                        if (portlet.getWidgetState().equals( Constants.STATE_LIVE_CHART)
                                && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                                && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                            if (portlet.getChartData().getFields() == null) {
                                getfileFields(portlet);
                            }
                            for (Map.Entry<String, List<Field>> entry : portlet.getChartData().getFields().entrySet()) {
                                if (!commonFields.containsKey(entry.getKey())) {
                                    fields = new LinkedHashSet<Field>();
                                    fields.addAll(entry.getValue());

                                    commonFields.put(entry.getKey(), fields);
                                    newCommonFields.put(entry.getKey(), fields);
                                }
                            }
                        }
                    }
                    if(!newCommonFields.isEmpty()) {
                        constructFilterItem(newCommonFields);
                    }
                }else if(Constants.QUERY.equals(dashboard.getFileType())){
                    if(commonInputParams == null){
                        commonInputParams =  new LinkedHashMap<String, Map<String,Set<String>>>();
                    }
                    //this holds the fields for newly added charts, to avoid reconstructing listbox with filter columns for existing queries as well
                    Map<String, Map<String,Set<String>>>  newInputParams = new LinkedHashMap<String, Map<String,Set<String>>>();
                    for (Portlet portlet : dashboard.getPortletList()) {
                        if (portlet.getWidgetState().equals( Constants.STATE_LIVE_CHART)
                                && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                                && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                            getInputParams(portlet,newInputParams,null);
                        }
                    }
                    
                    if(!newInputParams.isEmpty()) {
                        constructFilterItemForQuery(newInputParams);
                    }
                }
                if(!userCredential.hasRole(Constants.ROLE_API_VIEW_DASHBOARD)){
                    commonFiltersPanel.setVisible(true);
                }
            } else {  
                resetPanelCommonfilterIndicator();
                if (filterRows.getChildren() != null && !filterRows.getChildren().isEmpty() ) {
                    removeGlobalFilters();
                }
                // making common filters panel invisible
                commonFiltersPanel.setVisible(false);
            }
            
            //Show/Hide Manage groups for dashboard
            if(dashboard.getVisibility().equals(Constants.VISIBLITY_PUBLIC)) {
                manageGroups.setVisible(true);
                addAvailableGroups();
                addAddedGroups();
            } else {
                manageGroups.setVisible(false);
            }
            
            reorderPortletPanels();
            resizePortletPanels();
            try {
                //updating Dashboard details
                dashboard.setLastupdatedDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
                dashboardService.updateDashboard(dashboard);
                
                //updating Widget sequence
                widgetService.updateWidgetSequence(dashboard);
            }catch(DataAccessException ex){
                LOG.error("Exception while configuring Dashboard in onLayoutChange()", ex);
                Clients.showNotification(Labels.getLabel("unableToUpdateWidget"),
                        Clients.NOTIFICATION_TYPE_ERROR, DashboardController.this.getSelf(), Constants.POSITION_CENTER, 3000, true);
            }
        }        
    };

    
    /**
     * Method to remove all global filters, while unchecking common filter
     * in dashboard configuration page
     */
    private void removeGlobalFilters() {
        try {
            if(Constants.LOGICAL_FILE.equals(dashboard.getFileType())){
                removeFilterUpdateWidget();
               
            }else if(Constants.QUERY.equals(dashboard.getFileType())){
                removeInputparamUpdateWidget();
            }
            Sessions.getCurrent().removeAttribute(Constants.COMMON_FILTERS);
            // Removing common filters Row from UI
            filterRows.getChildren().clear();
            dashboard.setHasCommonFilter(false);
            
        } catch (Exception e) {
            LOG.debug(" Exception while removing global filters", e);
        }
    }
    
  //As dashboard has commonfilter enabled,setting 'hasCommonFileter' false for the  chartpanel
    //It will tell whether to fetch the fresh hpcc data while selecting the inputparam button.
    protected void resetPanelCommonfilterIndicator() {
        portalChildren.stream().forEach(portalChild ->{
            portalChild.getChildren().stream().forEach(childComp ->{
               ChartPanel panel = ((ChartPanel)childComp);
                if(panel instanceof ChartPanel 
                        &&  Constants.STATE_LIVE_CHART.equals(panel.getPortlet().getWidgetState())
                        && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(panel.getPortlet().getChartType()).getCategory()
                        && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(panel.getPortlet().getChartType()).getCategory()){
                    panel.setAttribute(Constants.COMMON_FILTERS_ENABLED, false);
                }
                
            });
        });
    }


    //As dashboard has commonfilter enabled,setting 'hasCommonFileter' true for the  chartpanel
    //It will decide not to fetch the fresh hpcc data while selecting the inputparam button.
    protected void setPanelCommonfilterIndicator() {        
        portalChildren.stream().forEach(portalChild ->{
            portalChild.getChildren().stream().forEach(childComp ->{
               ChartPanel panel = ((ChartPanel)childComp);
                if(panel instanceof ChartPanel 
                        &&  Constants.STATE_LIVE_CHART.equals(panel.getPortlet().getWidgetState())
                        && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(panel.getPortlet().getChartType()).getCategory()
                        && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(panel.getPortlet().getChartType()).getCategory()){
                    panel.setAttribute(Constants.COMMON_FILTERS_ENABLED, true);
                }
                
            });
        });
        
    }


    private void removeInputparamUpdateWidget() {
        dashboard.getPortletList().stream().forEach(portlet ->{
            if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                    && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                    && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()){
                appliedCommonInputParam.stream().forEach(inputparam ->{
                    dashboard.getCommonQueryFilters().remove(inputparam);
                    if(portlet.getChartData().getInputParams().contains(inputparam)){
                        portlet.getChartData().getInputParams().remove(inputparam);
                        //resetting the title colum value while removing a inputparam
                        if(portlet.getTitleColumns() != null){
                            TitleColumn titleCol = new TitleColumn("",  inputparam.getName());
                            if(portlet.getTitleColumns().contains(titleCol)){
                                portlet.getTitleColumns().get(portlet.getTitleColumns().indexOf(titleCol)).setValue(null);
                            }
                        }
                    }
                });
                try {
                    // refreshing the chart && updating DB
                    updateWidgets(portlet);
                } catch (Exception e) {
                    LOG.error(Constants.EXCEPTION,e);
                    Clients.showNotification(Labels.getLabel("unableToUpdateWidget"),
                            Clients.NOTIFICATION_TYPE_ERROR, this.getSelf(), Constants.POSITION_CENTER, 3000, true);
                }
            }
        });
        
    }


    private void removeFilterUpdateWidget() {
        dashboard.getPortletList().stream().forEach(portlet ->{
            if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                    && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                    && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()){
                appliedCommonFilters.stream().forEach(filter ->{
                    if(portlet.getChartData().getIsFiltered()
                            && portlet.getChartData().getFilters().contains(filter)){
                        portlet.getChartData().getInputParams().remove(filter);
                        if (portlet.getChartData().getFilters().isEmpty()) {
                            portlet.getChartData().setIsFiltered(false);
                        }
                        
                    }
                });
              
                try {
                    // refreshing the chart && updating DB
                    updateWidgets(portlet);
                } catch (Exception e) {
                    LOG.error(Constants.EXCEPTION,e);
                    Clients.showNotification(Labels.getLabel("unableToUpdateWidget"),
                            Clients.NOTIFICATION_TYPE_ERROR, this.getSelf(), Constants.POSITION_CENTER, 3000, true);
                }
            }
            
        });
        
    }


    protected void getfileFields(Portlet portlet) {
        // Getting fields for each files
          try {
                Map<String, List<Field>> fieldMap = new LinkedHashMap<String, List<Field>>();
                List<Field> fields = null;
                for (String file : portlet.getChartData().getFiles()) {
                    fields = new ArrayList<Field>();
                    fields.addAll(hpccService.getColumns(file, portlet.getChartData().getHpccConnection()));                
                    fieldMap.put(file, fields);
                }
                portlet.getChartData().setFields(fieldMap);
        } catch ( Exception e) {
            LOG.error(Constants.EXCEPTION,e);
        }
    }
    
    protected void getInputParams(Portlet portlet, Map<String, Map<String, Set<String>>> newInputParams,
            QuerySchema querySchema) {
        // Getting input params for each query
        try {
            for (String file : portlet.getChartData().getFiles()) {
                // Roxie Query - fetching fields/columns of Roxie queries
               if(querySchema == null){
                querySchema = hpccQueryService.getQuerySchema(file, portlet
                        .getChartData().getHpccConnection(), portlet
                        .getChartData().isGenericQuery(), portlet
                        .getChartData().getInputParamQuery());
               }

                if (!commonInputParams.containsKey(file)) {
                    commonInputParams.put(file, querySchema.getInputParams());
                    newInputParams.put(file, querySchema.getInputParams());
                }
            }

        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION, e);
        }

    }

    /**
     *  When a widget is deleted
     */
    final EventListener<Event> onPanelClose = new EventListener<Event>() {

        public void onEvent(final Event event)  {
            
            Portlet deletedPortlet = (Portlet) event.getData();
            dashboard.getPortletList().remove(deletedPortlet);
            
            if(LOG.isDebugEnabled()) {
                LOG.debug("Deleted portlet -> " + deletedPortlet);
            }
            
            Events.sendEvent("onPanelReset", DashboardController.this.getSelf(), deletedPortlet);            
            
            if(LOG.isDebugEnabled()) {
                LOG.debug("hide portlet event");
            }
            
            reorderPortletPanels();
            resizePortletPanels();
            
            if(LOG.isDebugEnabled()) {
                LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
            }
            try{
                if(!dashboard.getPortletList().isEmpty()){
                    //Updating new widget sequence to DB
                    widgetService.updateWidgetSequence(dashboard);
                }
            }catch(DataAccessException e){
                LOG.error("Exception in onPanelClose()", e);
            }
        }
    };    
    
    final EventListener<Event> onPanelDrawn = new EventListener<Event>() {

        @Override
        public void onEvent(Event event) throws Exception {
            drawnLiveChartCount--;    
            if(drawnLiveChartCount == 0){
                constructCommonFilterPanel();
            }
        }
        
    };
    /**
     * Event listener to be invoked when a Panel is reset
     */
    final EventListener<Event> onPanelReset = new EventListener<Event>() {
        
        @SuppressWarnings("unchecked")
        @Override
        public void onEvent(Event event) throws Exception {
            Portlet deletedPortlet = (Portlet) event.getData();
            dashboard.getPortletList().remove(deletedPortlet);

            //Logic assumes no same filenames in different HPCC Systems in a single dashboard
            //TODO: Fix
            if (Constants.STATE_LIVE_CHART.equals(deletedPortlet.getWidgetState())
                    && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(deletedPortlet.getChartType()).getCategory()
                    && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(deletedPortlet.getChartType()).getCategory()) {
                //Files being detached from Dashboard
                List<String> files = new ArrayList<String>();
                files.addAll(deletedPortlet.getChartData().getFiles());
                //Removing files to be retained
              
                for (String deletedFile : deletedPortlet.getChartData().getFiles()) {
                    for (Portlet portlet : dashboard.getPortletList()) {
                        if(portlet.getWidgetState().equals(Constants.STATE_LIVE_CHART) 
                                && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                                && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()
                                && portlet.getChartData().getFiles().contains(deletedFile)) {
                            files.remove(deletedFile);
                        }
                    }
                }
                LOG.debug("files after removing-->"+files);
                //Removing Displayed columns
                List<Component> componentsToDetach = null;
               
                if(!files.isEmpty() && !deletedPortlet.getChartData().getIsQuery()){
                    componentsToDetach = getFileTabsToDetach(files);
                    for(Component component: componentsToDetach) {
                        component.detach();
                    }
                    removeAppliedFileFilterRow(files);
                }else if(!files.isEmpty()){
                    componentsToDetach = getQueryTabsToDetach(files);
                   Set<String> deletedQueriesInputParams = new HashSet<String>();
                    for(Component component: componentsToDetach) {
                        if(component instanceof Tab){
                            deletedQueriesInputParams.addAll((Collection<? extends String>) component.getAttribute(Constants.INPUT_PARAM_NAMES));
                        }
                        component.detach();
                    }
                    removeAppliedQueryFilterRow(deletedQueriesInputParams);
                }
               
            }
            
            deletedPortlet.setChartData(null);
            deletedPortlet.setChartDataJSON(null);
            deletedPortlet.setChartDataXML(null);
            deletedPortlet.setChartType(null);
            deletedPortlet.setName(null);
            deletedPortlet.setWidgetState(Constants.STATE_EMPTY);
            
            //Clears all chart data from DB
            widgetService.updateWidget(deletedPortlet);
            
        }
    };
    
    /**
     * Removes the filter from specified row. Updates portlet objects
     *  
     * @param rowToRemove
     *     Row to be detached
     * @return
     *     A set of portlets for those, charts has to be refreshed and changes to be saved in DB
     */
    Set<Portlet> removeFilter(Row rowToRemove) {
        Set<Portlet> portletsToRefresh = new HashSet<Portlet>();
        Filter filter = (Filter) rowToRemove.getAttribute(Constants.FILTER);
        for (Portlet portlet : dashboard.getPortletList()) {
            if (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                    && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                    && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()
                    && portlet.getChartData().getIsFiltered()) {
                // removing global filter object from filter list
                if (portlet.getChartData().getFilters().contains(filter)) {
                    portlet.getChartData().getFilters().remove(filter);
                    if (portlet.getChartData().getFilters().isEmpty()) {
                        portlet.getChartData().setIsFiltered(false);
                    }
                }
                
                portletsToRefresh.add(portlet);
            }
        }    
        
        return portletsToRefresh;
    }
    
    
    /**
     * Removing applied common input param
     * @param deletedQueriesInputParams
     * @param files
     */
    @SuppressWarnings("unchecked")
    protected void removeAppliedQueryFilterRow( Set<String> deletedQueriesInputParams) {
        Set<InputParam> inputparamToRemove = new HashSet<InputParam>();
        if(appliedCommonInputParam != null){
           
            Component otherCompWithSameInputparam = null;
            for (InputParam inputparam : appliedCommonInputParam) {
                try{
                    Optional<String> optionInput = deletedQueriesInputParams.stream().filter(input ->inputparam.getName().equals(input)).findFirst();
                    final String appliedInput = optionInput.isPresent() ? optionInput.get():null;
                    if(appliedInput != null){
                        //Check any other query has the same input param
                        Optional<Component> option =  commonFilterTabbox.getFirstChild().getChildren().stream().filter(component ->
                        (((Set<String>)component.getAttribute(Constants.INPUT_PARAM_NAMES)).contains(appliedInput))).findFirst();
                        if(option.isPresent()){
                            otherCompWithSameInputparam = option.get();
                        }
                       
                        if(otherCompWithSameInputparam == null){
                            inputparamToRemove.add(inputparam);
                        }else{
                            //Don't remove the inputparam as other query has same column
                        }
                    }else{
                        //Do nothing as this inputaparam not present in other queries
                    }
                }catch(NoSuchElementException e){
                    LOG.error(Constants.EXCEPTION,e);
                }
            }
         }
        //Collects the filter rows which has the inputparam of deleted widget query's inputparam
        //and deletes those rows
        if(!inputparamToRemove.isEmpty()){
            List<Component> rowsToDelete = new ArrayList<Component>();
            Component row = null;
            for (InputParam inputparam  : inputparamToRemove) {
                Optional<Component> option = filterRows.getChildren().stream().filter(component ->
                (component.getAttribute(Constants.INPUT_PARAM_NAME).equals(inputparam.getName()))).findFirst();
                if(option.isPresent()){
                    row = option.get();
                }                
                if(row != null){
                    rowsToDelete.add(row);
                }
            }
            for (Component row2 : rowsToDelete) {
                row2.detach();
            }
        }
        
    }


    /**Removes the filter row from common filter panel, while deleting a widget.
     * The removing filter used the file used by the deleted widget
     * @param files
     */
    protected void removeAppliedFileFilterRow( List<String> files) {
        //Remove applied filters
        Set<Filter> filtersToRemove = new HashSet<Filter>();
        if(appliedCommonFilters!= null){
        for (Filter filter : appliedCommonFilters) {
            if(files.contains(filter.getFileName())) {
                filtersToRemove.add(filter);
            }
        }}
        LOG.debug("filtersToRemove -->"+filtersToRemove);
        Row row;
        Filter rowFilter;
        List<Row> rowsToDelete = new ArrayList<Row>();
        for (Filter filter : filtersToRemove) {
            for (Component component : filterRows.getChildren()) {
                row = (Row) component;
                rowFilter = (Filter) row.getAttribute(Constants.FILTER);
                if(filter.equals(rowFilter)) {
                    rowsToDelete.add(row);
                }
            }
        }
        LOG.debug("rowsToDelete -->"+rowsToDelete);
        for (Row row2 : rowsToDelete) {
            row2.detach();
        }
        
    }


    /**
     * Iterates the common filter tabs for Queries.And returns the tabs
     * which used the deleted portlet's query
     * @param files
     * @return List<Component>
     */
    protected List<Component> getQueryTabsToDetach(List<String> queries) {

        List<Component> componentsToDetach = new ArrayList<Component>();
        Tab tab;
        for(Component component : commonFilterTabbox.getFirstChild().getChildren()) {
            tab = (Tab) component;
            if(queries.contains(tab.getLabel())) {
                componentsToDetach.add(tab);
                componentsToDetach.add(tab.getLinkedPanel());
                commonInputParams.remove(tab.getLabel());
            }
        }
        return componentsToDetach;
    }


    /**
     * Iterates the common filter tabs for logical files.And returns the tabs
     * which used the deleted portlet's logical file
     * @param files
     * @return List<Component>
     */
    protected List<Component> getFileTabsToDetach(List<String> files) {
        List<Component> componentsToDetach = new ArrayList<Component>();
        Tab tab;
        for(Component component : commonFilterTabbox.getFirstChild().getChildren()) {
            tab = (Tab) component;
            if(files.contains(tab.getLabel())) {
                componentsToDetach.add(tab);
                componentsToDetach.add(tab.getLinkedPanel());
                commonFields.remove(tab.getLabel());
            }
        }
        return componentsToDetach;
    }


    @Listen("onPortalMove = portallayout")
    public void onPanelMove(final PortalMoveEvent event) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("onPanelMove");
        }
        final ChartPanel panel = (ChartPanel) event.getDragged();
        
        if(panel.drawD3Graph() != null) {
            Clients.evalJavaScript(panel.drawD3Graph());
        }
        
        reorderPortletPanels();
        resizePortletPanels();
        
        //Updating new widget sequence to DB
        try {
            widgetService.updateWidgetSequence(dashboard);
        } catch (Exception e) {
            Clients.showNotification(Labels.getLabel("errorOnUpdatingWidgetDetails"), Constants.ERROR_NOTIFICATION, this.getSelf(), Constants.POSITION_CENTER, 3000, true);
            LOG.error(Constants.EXCEPTION, e);
        }
    }
    
    
    public void onNameChange() {
        nameLabel.setValue(dashboard.getName());
        
        final Navbar navBar=(Navbar)Sessions.getCurrent().getAttribute(Constants.NAVBAR);
        final List<Component> childNavBars = navBar.getChildren(); 
        Integer navDashId=0;
        Navitem dashBoardObj=null;
        for (final Component childNavBar : childNavBars) {
            if(childNavBar instanceof Navitem){
                dashBoardObj = (Navitem) childNavBar;
                navDashId =  (Integer) dashBoardObj.getAttribute(Constants.DASHBOARD_ID);
                if(dashboard.getDashboardId().equals(navDashId)) {
                    dashBoardObj.setLabel(dashboard.getName());
                    break;
                }
            }
        }
    }
    
    /**
     * deleteDashboard() is used to delete the selected Dashboard in the sidebar page.
     */
    @Listen("onClick = #deleteDashboard")
    public void deleteDashboard() {
        try{
         // ask confirmation before deleting dashboard
         EventListener<ClickEvent> clickListener = new EventListener<Messagebox.ClickEvent>() {
             public void onEvent(ClickEvent event) {
                 
                 if(Messagebox.Button.YES.equals(event.getButton())) {
                    final Navbar navBar  = (Navbar) Selectors.iterable(DashboardController.this.getSelf().getPage(), "navbar").iterator().next();
                       
                    navBar.getSelectedItem().setVisible(false);
                       
                       final Include include = (Include) Selectors.iterable(DashboardController.this.getSelf().getPage(), "#mainInclude")
                               .iterator().next();
                       List<Integer> dashboardIdList = new ArrayList<Integer>();
                       
                       if(LOG.isDebugEnabled()){
                           LOG.debug("Setting first visible Nav item as active");
                       }
                       
                       Navitem navitem;
                       Boolean isSelected = false;
                       for (Component component : navBar.getChildren()) {
                           navitem = (Navitem) component;
                           if(navitem.isVisible()){
                               //Adding visible items to list
                               dashboardIdList.add((Integer) navitem.getAttribute(Constants.DASHBOARD_ID));
                               
                               //Selecting first visible Item
                               if(!isSelected){
                                   navitem.setSelected(true);
                                   Events.sendEvent(Events.ON_CLICK, navitem, null);
                                   isSelected = !isSelected;
                               }
                           }
                       }
                       
                       if( !isSelected ) {
                           Sessions.getCurrent().setAttribute(Constants.ACTIVE_DASHBOARD_ID, null);
                           //Detaching the include and Including the page again to trigger reload
                           final Component component2 = include.getParent();
                           include.detach();
                           final Include newInclude = new Include("/demo/layout/dashboard.zul");
                           newInclude.setId("mainInclude");
                           component2.appendChild(newInclude);
                           Clients.evalJavaScript("showPopUp()");
                       }                       
                       dashboardService.deleteDashboard(dashboard.getDashboardId(),authenticationService.getUserCredential().getUserId());
                       dashboardService.updateSidebarDetails(dashboardIdList);
                 }

               } 
           };
           
       Messagebox.show(Labels.getLabel("deleteDashboardMsg"), Labels.getLabel("deleteDashboardTitle"), new Messagebox.Button[]{
               Messagebox.Button.YES, Messagebox.Button.NO }, Messagebox.QUESTION, clickListener);
        }catch(DataAccessException ex){
            Clients.showNotification(Labels.getLabel("unableToDeleteDashboard"), Clients.NOTIFICATION_TYPE_ERROR, this.getSelf(), Constants.POSITION_CENTER, 3000, true);
            LOG.error("Exception while deleting Dashboard in DashboardController", ex);
            return;
        }catch(Exception ex){
            Clients.showNotification(Labels.getLabel("unableToDeleteDashboard"), Clients.NOTIFICATION_TYPE_ERROR, this.getSelf(), Constants.POSITION_CENTER, 3000, true);
            LOG.error("Exception while deleting Dashboard in DashboardController", ex);
            return;            
        }
  }    
    
    public void addAvailableGroups(){
        List<Group> groups;
        try {
            groups = conditionalGroupService.getGroups();
            Listitem listItem = null;
            Listcell listCell = null;
            for (Group group : groups) {
                listCell = new Listcell();
                listItem = new Listitem();    
                if(Constants.ROLE_ADMIN.equals(group.getRole())
                        || Constants.ROLE_CONTRIBUTOR.equals(group.getRole())){
                    listCell.setIconSclass("glyphicon glyphicon-pencil");
                }else if(Constants.ROLE_CONSUMER.equals(group.getRole())){
                    listCell.setIconSclass("glyphicon glyphicon-eye-open");
                }
                listCell.setLabel(group.getName());
                listCell.setParent(listItem);
                listItem.setDraggable("true");
                listItem.setAttribute(Constants.GROUP, group);
                listItem.setParent(availableGroups);
            }
        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION, e);
            Clients.showNotification("Error in retrieving groups", Constants.ERROR_NOTIFICATION, manageGroups, "after_center", 3000);
        }
    }
    
    private void addAddedGroups() throws DataAccessException,SQLException{
        try {
            List<Group> groups = groupService.getGroups(dashboard.getDashboardId());
            for (Group group : groups) {
                addedGroups.appendChild(new GroupListitem(group, dashboardId));
            }
        } catch (DataAccessException | SQLException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        }    
    }
    
    @Listen("onClick = #interactivityBtn")
    public void onClickInteractivity() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Constants.DASHBOARD, dashboard);
        
        Window window  = (Window) Executions.createComponents("/demo/layout/interactivity/config.zul", this.getSelf(), parameters);
        window.doModal();
    }
    
    @Listen("onClick = #hyperlinlBtn")
    public void generateDashboardLink() {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Constants.DASHBOARD, dashboard);

        final Window window = (Window) Executions.createComponents(
                "/demo/layout/hyperlink/link.zul", this.getSelf(), parameters);
        window.doModal();
    }
}
