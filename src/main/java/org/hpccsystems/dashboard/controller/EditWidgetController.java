package org.hpccsystems.dashboard.controller; 

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.api.entity.ApiChartConfiguration;
import org.hpccsystems.dashboard.chart.cluster.ClusterData;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ChartData;
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
import org.hpccsystems.dashboard.controller.component.ChartPanel;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.springframework.dao.DataAccessException;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Window;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
 
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditWidgetController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(EditWidgetController.class);

    @WireVariable
    private AuthenticationService authenticationService;
    @WireVariable
    private DashboardService dashboardService;
    @WireVariable
    private WidgetService widgetService;
    
    @WireVariable
    private HPCCService hpccService;
    @WireVariable
    private ChartRenderer chartRenderer;
    @WireVariable
    private TableRenderer tableRenderer;
    @WireVariable
    private ChartService chartService;
    @WireVariable
    private HPCCQueryService hpccQueryService;
    

    @Wire
    Include holderInclude;
    @Wire
    Window editPortletWindow;
    @Wire
    Button doneButton;
    
    Portlet portlet;
    ChartData chartData;
    ChartPanel chartPanel;
    
    Dashboard dashboard;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Inside Edit portlet constructor..");
        }

        super.doAfterCompose(comp);
        
        Execution execution = Executions.getCurrent();
        
        if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)) {
            if(execution.getParameter(Constants.CIRCUIT_DASHBOARD_ID) != null){
                List<String> dashboardIdList = null;
                dashboardIdList = new ArrayList<String>();
                dashboardIdList.add(execution.getParameter(Constants.CIRCUIT_DASHBOARD_ID));
                
                dashboard = dashboardService.retrieveDashboardMenuPages(
                        Constants.CIRCUIT_APPLICATION_ID, 
                        authenticationService.getUserCredential().getUserId(), 
                        dashboardIdList,
                        null).get(0);
                
                portlet = widgetService.retriveWidgetDetails(dashboard.getDashboardId())
                        .get(0); //Assuming one Widget exists for the provided dashboard
                 
            } else {
                getApiConfigRoleDashboard(execution.getParameter(Constants.SOURCE_ID),
                        execution.getParameter(Constants.SOURCE));
            }
            
            ApiChartConfiguration configuration = new GsonBuilder().create().fromJson(
                    execution.getParameter(Constants.CIRCUIT_CONFIG),ApiChartConfiguration.class);
            portlet.setChartType(configuration.getChartType());
            portlet.setName(configuration.getChartTitle());
            
            dashboard.setName(configuration.getDashboardTitle());
            
            List<String> files = new ArrayList<String>();
            files.add(configuration.getDatasetName());
            
            initChartData();
            chartData.setFiles(files);
            chartData.setHpccConnection(configuration.getHpccConnection());
            
            holderInclude.setDynamicProperty(Constants.CIRCUIT_CONFIG, configuration);
            
        } else if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_CHART)){
            //Viewing chart through API
           if(!hasApiViewRoleChartOfSameCategory(execution.getParameter(Constants.CIRCUIT_DASHBOARD_ID),
                    execution.getParameter(Constants.SOURCE_ID),execution.getParameter(Constants.CHART_TYPE))){
        	   return;
           }
        } else {
            //General flow
            portlet = (Portlet) Executions.getCurrent().getArg().get(Constants.PORTLET);
            chartPanel = (ChartPanel) Executions.getCurrent().getArg().get(Constants.PARENT);
            holderInclude.setDynamicProperty(Constants.PARENT, editPortletWindow);

            initChartData();
            if (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
                chartData = portlet.getChartData();
            }
        }
        
        holderInclude.setDynamicProperty(Constants.CHART_DATA, chartData);
        holderInclude.setDynamicProperty(Constants.PORTLET, portlet);
        holderInclude.setDynamicProperty(Constants.EDIT_WINDOW_DONE_BUTTON, doneButton);
        
        editPortletWindow.addEventListener("onIncludeDetach", includeDetachListener);
        
        if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState()) ||
                authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)){
            // Not for configuring chart through API
            if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
                holderInclude.setDynamicProperty(Constants.CHART_DATA, chartData);
            }
            
            holderInclude.setSrc(Constants.EDIT_SCREEN_URL_BY_CATEGORY.get(chartService.getCharts().get(portlet.getChartType()).getCategory()));
            
		} else if (Constants.CATEGORY_TEXT_EDITOR == chartService.getCharts()
				.get(portlet.getChartType()).getCategory()) {
			LOG.debug("Calling layout/edit_text_editor.zul");
			holderInclude.setDynamicProperty(Constants.CHART_DATA, chartData);
            holderInclude.setSrc("layout/edit_text_editor.zul");
        }else {
        	LOG.debug("Calling layout/edit_select_data.zul");
            holderInclude.setDynamicProperty(Constants.CHART_DATA, chartData);
            holderInclude.setSrc("layout/edit_select_data.zul");
        }
        
        
        editPortletWindow.addEventListener("onExit", new EventListener<Event>() {

            @Override
            public void onEvent(Event event) {
            	LOG.debug("EditWidgetController..... editPortletWindow.... onExit event().... ");
                Clients.evalJavaScript("window.open('','_self',''); window.close();");
                editPortletWindow.detach();
            }
        
        });
    }
    
    
    /**
     * Viewing chart through API
     * @param dashboardID
     * @param sourceId
     * @param chartType
     */
    private boolean hasApiViewRoleChartOfSameCategory(String dashboardID,String sourceId,String chartType) {
        List<String> dashboardIdList = null;
        if(dashboardID != null) {
            dashboardIdList = new ArrayList<String>();
            dashboardIdList.add(dashboardID);
        }
        
        dashboard = dashboardService.retrieveDashboardMenuPages(
                        Constants.CIRCUIT_APPLICATION_ID, 
                        authenticationService.getUserCredential().getUserId(), 
                        dashboardIdList,
                        sourceId)
                            .get(0); // Assuming one Dashboard exists for a provided source_id 
        LOG.debug("API view Role - dashboard -->"+dashboard);
        portlet = widgetService.retriveWidgetDetails(dashboard.getDashboardId())
                .get(0); //Assuming one Widget exists for the provided dashboard
        LOG.debug("API view Role - portlet -->"+portlet);
        LOG.debug("chartType passed -->"+chartType);
        //Overriding chart type
        if(chartType != null) {
            
            if(chartService.getCharts().get(portlet.getChartType()).getCategory() !=
                    chartService.getCharts().get(Integer.parseInt(chartType)).getCategory() ){
            	  LOG.debug("chartType contradicts -->");
                Clients.showNotification(Labels.getLabel("incompatibleChartType"), "error", this.getSelf(), "middle_center", 5000);
                return false;
            }
            portlet.setChartType(Integer.parseInt(chartType));
        }
        
        initChartData();
        if (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
            chartData = portlet.getChartData();
                
        }
        return true;
    }


    /**
     * Fetches dashboard for the passed sourceId through Api call
     * @param sourceId
     * @param applnId
     */
    private void getApiConfigRoleDashboard(String sourceId,String applnId) {
        List<Dashboard> dashboards;
        dashboards = dashboardService.retrieveDashboardMenuPages(
                Constants.CIRCUIT_APPLICATION_ID, 
                authenticationService.getUserCredential().getUserId(), 
                null,
                sourceId);
        LOG.debug("Api dashboards -->"+dashboards);
        if(!dashboards.isEmpty()){
            // Provided source id is already saved
            dashboard = dashboards.get(0);
            
            portlet = widgetService.retriveWidgetDetails(dashboard.getDashboardId())
                    .get(0); //Assuming one Widget exists for the provided dashboard
        } else {
            dashboard = new Dashboard();
            dashboard.setSourceId(sourceId);
            dashboard.setApplicationId(applnId);
            dashboard.setColumnCount(1);
            dashboard.setSequence(0);
            portlet = new Portlet();
            portlet.setColumn(0);
        }        
    }


    /**
     * Listener to invoke when holderInclude is detached
     */
    EventListener<Event> includeDetachListener = new EventListener<Event>() {
        public void onEvent(Event event) throws Exception {
        	
            if (event.getData() != null && event.getData().equals(Constants.EDIT_WINDOW_TYPE_DATA_SELECTION)) {
                
				if (chartData.getFiles().size() > 1 ) {
					LOG.debug("chartData.getFiles().size() > 1: "+chartData.getFiles().size());
                    holderInclude.setSrc("layout/edit_join_data.zul");
                    return;
                }				
				
                holderInclude.setSrc(Constants.EDIT_SCREEN_URL_BY_CATEGORY.get(chartService.getCharts().get(portlet.getChartType()).getCategory()));
            }else if(event.getData()!= null
                    && event.getData().equals(Constants.EDIT_WINDOW_JOIN_DATA)) {
                
                holderInclude.setSrc(Constants.EDIT_SCREEN_URL_BY_CATEGORY.get(chartService.getCharts().get(portlet.getChartType()).getCategory()));
            }
        }
    }; 

    /**
     * Draws the chart from edit window to actual layout window and Adds the
     * chart to session
     * 
     * @param event
     */
    @Listen("onClick=#doneButton")
    public void closeEditWindow(final MouseEvent event) {
        portlet.setWidgetState(Constants.STATE_LIVE_CHART);
        
        if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)){    
            //Configuring chart through API
            configureApiChart();    
        } else if (authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_CHART)) {            
            //Viewing chart through API
            viewApichart();    
        } else {
            //General flow
            configureChart();            
        }
    }

    /**
     * configures chart in general flow, while clicking done button
     */
    private void configureChart() {
        portlet.setChartData(chartData);
        try {
            Div div = chartPanel.removeStaticImage();
            
            //Send event to change the chart title if the title is dynamic
            //'ModelID:<$ModelID>Actual:<$Cur_Period>'
            Events.postEvent(Constants.ON_GENERATE_DYNAMIC_TITLE, chartPanel, null);

            //For Table Widget
            if(Constants.CATEGORY_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                div.getChildren().clear();
                div.appendChild(
                        tableRenderer.constructTableWidget(
                                portlet, (TableData) portlet.getChartData(), false)
                        );
            } else if(Constants.CATEGORY_HIERARCHY == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                //For Tree Widgets
                final String divToDraw = div.getUuid(); 
                
                //isEdit Window is set to false as we are constructing the JSON to be drawn in the Widget itself
                chartRenderer.drawChart(divToDraw, portlet);
                
            } else if(Constants.CATEGORY_TEXT_EDITOR == chartService.getCharts().get(portlet.getChartType()).getCategory()){
            	Events.postEvent("onCreateDocumentWidget", chartPanel, null);
            }else if(Constants.CATEGORY_SCORED_SEARCH_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
            	costructScoredSearchTable((ScoredSearchData) portlet.getChartData(),div);                	
            } else if(Constants.RELEVANT_CONFIG == chartService.getCharts().get(portlet.getChartType()).getCategory()){
            	RelevantData objRelevantData = (RelevantData)portlet.getChartData();
            	
            	objRelevantData.setClaimImage((Combobox)holderInclude.getFellow("cmbClaim") != null ? ((Combobox)holderInclude.getFellow("cmbClaim")).getSelectedItem().getValue().toString() : null);
            	objRelevantData.setPersonImage((Combobox)holderInclude.getFellow("cmbPerson") != null ? ((Combobox)holderInclude.getFellow("cmbPerson")).getSelectedItem().getValue().toString() : null);
            	objRelevantData.setVehicleImage((Combobox)holderInclude.getFellow("cmbVehicle") != null ? ((Combobox)holderInclude.getFellow("cmbVehicle")).getSelectedItem().getValue().toString() : null);
            	objRelevantData.setPolicyImage((Combobox)holderInclude.getFellow("cmbPolicy") != null ? ((Combobox)holderInclude.getFellow("cmbPolicy")).getSelectedItem().getValue().toString() : null);
            	
            	LOG.debug("RELEVANT DATA: "+objRelevantData);
            	
        		String relJSON = new Gson().toJson(objRelevantData);
        		LOG.debug("RELEVANT JSON: "+relJSON);
            	
            	//portlet.setChartDataJSON(" { \"claimId\": \"CLM00042945-C034\", \"claimImage\": \"\\uf0d6\", \"personImage\": \"\\uf007\", \"vehicleImage\": \"\\uf1b9\", \"policyImage\": \"\\uf0f6\" }");
        		portlet.setChartDataJSON(relJSON);
        		
            	final String divToDraw = div.getId(); 
            	chartRenderer.drawChart(divToDraw, portlet);
            	LOG.debug("Drawing Relevant chart in portlet : "+ divToDraw);
            	
            } else {
                //For Chart Widgets
                final String divToDraw = div.getId(); 
                if(Constants.CATEGORY_GAUGE != chartService.getCharts().get(portlet.getChartType()).getCategory()
                    && Constants.CATEGORY_CLUSTER != chartService.getCharts().get(portlet.getChartType()).getCategory()){
                        //Changing the portlet Id from e_id to p_id
                        Gson gson = new Gson();
                        JsonElement element = gson.fromJson (portlet.getChartDataJSON(), JsonElement.class);
                        JsonObject jsonObj = element.getAsJsonObject();
                        jsonObj.remove(Constants.PORTLET_ID);               
                        jsonObj.addProperty(Constants.PORTLET_ID, "p_" + portlet.getId());               
                        LOG.debug("jsonObj -->"+jsonObj.toString());
                        portlet.setChartDataJSON(jsonObj.toString());
                   }
                    
                chartRenderer.drawChart(divToDraw, portlet);         

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Drawn chart in portlet..");
                    LOG.debug("Portlet - Div ID --> " + divToDraw);
                }
            }

            //update Live chart data into DB
            portlet.setChartData(chartData);
            
            widgetService.updateWidget(portlet);

        }catch(DataAccessException | HpccConnectionException e){
            Clients.showNotification(Labels.getLabel("unableToUpdateDBData"), "error", this.getSelf(), "middle_center", 3000, true);
            LOG.error("Exception in closeEditWindow() while updating Live chart data into DB", e);
            return;
        }catch (JAXBException e) {
            Clients.showNotification(Labels.getLabel("unableToUpdateDBData"), "error", this.getSelf(), "middle_center", 3000, true);
            LOG.error(Constants.EXCEPTION, e);
            return;    
        }catch (Exception e) {
            Clients.showNotification(Labels.getLabel("unableToFetchHpccData"), "error", this.getSelf(), "middle_center", 3000, true);
            LOG.error(Constants.EXCEPTION, e);
            return;    
        }
        
        final Include include = (Include) Selectors.iterable(this.getSelf().getPage(), "#mainInclude").iterator().next();
        Window window = (Window) include.getChildren().iterator().next();
        Events.postEvent("onDrawingLiveChart", window, portlet);
        
        //Send event to create input params
        Events.postEvent("onDrawingQueryChart", chartPanel, null);
        
        LOG.debug("Closing the editPortletWindow.detach() ..... ");
        
        editPortletWindow.detach();        
    }

    /**
     * Renders table while clicking 'doneButton'
     * @param scoredSearchData
     * @param div
     */
    private void costructScoredSearchTable(ScoredSearchData scoredSearchData,Div div) {
    	 div.getChildren().clear();
    	 Tabbox tabbox = new Tabbox();
    	 tabbox.setVflex("1");
    	 tabbox.setParent(div);
    	 Tabs tabs = new Tabs();
    	 tabs.setParent(tabbox);
    	 Tabpanels tabpanels = new Tabpanels();
    	 tabpanels.setParent(tabbox);
    	 HashMap<String, HashMap<String, List<Attribute>>> hpccResult = scoredSearchData.getHpccTableData();
    	 if(hpccResult != null){
    		 hpccResult = scoredSearchData.getHpccTableData();
    	 }else{
    		 try {
				hpccResult = hpccQueryService.fetchScoredSearchData(scoredSearchData);
			} catch (RemoteException | HpccConnectionException e) {
				LOG.error(Constants.EXCEPTION,e);
				Clients.showNotification("Unable to fetch Hpcc data",
						Clients.NOTIFICATION_TYPE_ERROR, this.getSelf(),
						"middle_center", 3000, true);
				return;
			}
    	 }
    	for(Entry<String, HashMap<String, List<Attribute>>> entry : hpccResult.entrySet()){
			Tab tab = new Tab(entry.getKey());
			tab.setParent(tabs);
			Tabpanel tabpanel = new Tabpanel();
			Listbox listBox = tableRenderer.constructScoredSearchTable(entry.getValue(),false);
			listBox.setParent(tabpanel);
			tabpanel.setVflex("1");
			tabpanel.setParent(tabpanels);
			tabpanel.setVflex("1");
		}	
	}


	/**
     * to view chart through Api call
     */
    private void viewApichart() {
        portlet.setChartData(chartData);
        
        try {
            widgetService.updateWidget(portlet);
        } catch (Exception e) {
            // TODO Show notification
        }
        
        try {
            authenticationService.logout(null);
        } catch (Exception e) {
            Clients.showNotification(Labels.getLabel("logoutError"));
            LOG.error(Constants.EXCEPTION, e);
        }
        
        Messagebox.show("Chart details are Updated Successfuly. This window will be closed", new Messagebox.Button[0], null);
        Clients.evalJavaScript("window.open('','_self',''); window.close();");
        editPortletWindow.detach();        
    }

    /**
     * Configures chart through API call
     */
    private void configureApiChart() {
        dashboard.setLastupdatedDate(new Timestamp(new Date().getTime()));
        
        try {
            List<Dashboard> dashboardList = dashboardService.retrieveDashboardMenuPages(Constants.CIRCUIT_APPLICATION_ID, 
                    authenticationService.getUserCredential().getUserId(),
                    null,dashboard.getSourceId());
            if (dashboardList.isEmpty()) {
                    dashboardService.addDashboardDetails(dashboard, Constants.CIRCUIT_APPLICATION_ID, dashboard
                                        .getSourceId(),    authenticationService.getUserCredential().getUserId());
                portlet.setChartData(chartData);
                widgetService.addWidget(dashboard.getDashboardId(),    portlet, 0);
            } else {
                dashboardService.updateDashboard(dashboard);
                portlet.setChartData(chartData);
                widgetService.updateWidget(portlet);
            }
        } catch (DataAccessException e) {
            Clients.showNotification(Labels.getLabel("errorOnSavingChanges"));
            LOG.error(Constants.EXCEPTION, e);
            return;
        } catch (JAXBException e) {
            Clients.showNotification(Labels.getLabel("errorOnSavingChanges"));
            LOG.error(Constants.EXCEPTION, e);
            return;
        } catch (EncryptDecryptException e) {
            Clients.showNotification(Labels.getLabel("errorOnSavingChanges"));
            LOG.error(Constants.EXCEPTION, e);
            return;
        } 
        
        try {
            authenticationService.logout(null);
        } catch (Exception e) {
            Clients.showNotification(Labels.getLabel("logoutError"));
            LOG.error("Logout error", e);
            return;
        }
        
        Messagebox.show("Chart details are Updated Successfuly. This window will be closed", new Messagebox.Button[0], null);
        editPortletWindow.detach();
        Clients.evalJavaScript("window.open('','_self',''); window.close();");
        // TODO Auto-generated method stub
        
    }

    /**
     * method to invalidate session while closing edit window  in the API flow
     */
    @Listen("onClose=#editPortletWindow")
    public void closeWindow(Event event){
       if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART )||
                 authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_CHART)){
           event.stopPropagation();
           Messagebox.show("Chart settings are not saved. Do you still want to close?", 
                    "Question", Messagebox.YES | Messagebox.NO,
                    Messagebox.QUESTION,
                        new EventListener<Event>(){
                            public void onEvent(Event event){
                                if(Messagebox.ON_YES.equals(event.getName())){
                                   try {
                                         authenticationService.logout(null);
                                         editPortletWindow.detach();
                                         Clients.evalJavaScript("window.open('','_self',''); window.close();");
                                     } catch (Exception ex) {
                                       LOG.error(Constants.EXCEPTION, ex);
                                     }
                                }
                            }
                        }
                    );
          
       }
    }

    private void initChartData() {
        int category = chartService.getCharts().get(portlet.getChartType()).getCategory();
        LOG.debug("EditWidgetController : initChartData() .... CHART TYPE CATEGORY: "+category);
        
        if(category == Constants.CATEGORY_XY_CHART ||
                category == Constants.CATEGORY_PIE || category == Constants.CATEGORY_USGEO) {
            chartData = new XYChartData();
        } else if(category == Constants.CATEGORY_TABLE) {
            chartData = new TableData();
        } else if(category == Constants.CATEGORY_HIERARCHY) {
            chartData = new TreeData();
        } else if(category == Constants.CATEGORY_GAUGE) {
            chartData = new GaugeChartData();
        } else if(category == Constants.CATEGORY_TEXT_EDITOR){
        	 chartData = new TextData();
        } else if(category == Constants.CATEGORY_CLUSTER){
        	 chartData = new ClusterData();
        } else if(category == Constants.CATEGORY_SCORED_SEARCH_TABLE){
       	 	chartData = new ScoredSearchData();
        } 
        // For Relevant Graph
        else if(category == Constants.RELEVANT_CONFIG){
        	chartData = new RelevantData();
        }
    }
}
