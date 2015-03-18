package org.hpccsystems.dashboard.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CurrentDateFilterController extends SelectorComposer<Vlayout>{
    
    private static final long serialVersionUID = 1L;
    
    private static final  Log LOG = LogFactory.getLog(CurrentDateFilterController.class);
    
    @Wire
    private Textbox dateFormatTextbox;
    @Wire
    private Button dateFilterBtn;
    @Wire
    private Listbox dateFilterListBox;
    @Wire
    private Combobox columnList;
    @Wire
    private Label sampleDate;
    
    @WireVariable
    AuthenticationService  authenticationService;
    
    private ChartData chartData;
    private Button doneButton;
    private Component parent;
    private Component busyComponent;
    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        this.getSelf().addEventListener("onInitialize", initializeListener);
        
        this.getSelf().addEventListener("onFilterAdded", filterAddedListener);
        this.getSelf().addEventListener("onFilterRemoved", onFilterRemoved);
    }
    
    EventListener<Event> initializeListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            chartData = (ChartData) event.getTarget().getAttribute(Constants.CHART_DATA);
            doneButton =  (Button) event.getTarget().getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
            parent = (Component) event.getTarget().getAttribute(Constants.PARENT);
            busyComponent = (Component) event.getTarget().getAttribute(Constants.BUSY_COMPONENT);
        }
    };
    

    /**
     * Event listener to fetch data from HPCC and draw the chart
     */
    EventListener<Event> filterAddedListener = new EventListener<Event>() {
        @Override
        public void onEvent(Event event) throws Exception {
            final Listitem listitem = (Listitem) event.getData();
            Filter filter = (Filter)listitem.getAttribute(Constants.FILTER);
            
            Comboitem item = new Comboitem();
            item.setLabel(filter.getColumn());
            item.setAttribute(Constants.PARAMS, listitem);
            item.setAttribute(Constants.FILTER, filter);
            item.setParent(columnList);
            
            if(Constants.CURRENT_DATE_NUMERIC.equals(filter.getType()) ||
                    Constants.CURRENT_DATE_STRING.equals(filter.getType())) {
                columnList.setSelectedItem(item);
                dateFormatTextbox.setValue(filter.getCurrentDateFormat());
            }
        }
    };
    
    
    /**
     * Event listener to fetch data from HPCC and draw the chart
     */
    EventListener<Event> onFilterRemoved = new EventListener<Event>() {
        @Override
        public void onEvent(Event event) throws Exception {
            Filter filter = (Filter) event.getData();            
            
            Comboitem comboitem = null;
            for (Component component : columnList.getChildren()) {
                comboitem = (Comboitem) component;
                if(filter.equals(comboitem.getAttribute(Constants.FILTER))) {
                    break;
                }
            }
            
            if(columnList.getSelectedItem() != null && 
                    filter.equals(columnList.getSelectedItem().getAttribute(Constants.FILTER))) {
                columnList.setSelectedItem(null);
            }
            
            dateFormatTextbox.setValue(null);
            comboitem.detach();
        }
    };
    
    
    @Listen("onChanging = #dateFormatTextbox")
    public void onTextChange(InputEvent event) {
        SimpleDateFormat formatter = new SimpleDateFormat(event.getValue());
        String stringDate = formatter.format(new Date());
        sampleDate.setValue(stringDate);
    }
    
    @Listen("onClick = button#dateFilterBtn")
    public void onApplyDateFilter() {
        
        if(columnList.getSelectedItem()!=null){
        	
        	Listitem selectedItem =  (Listitem)columnList.getSelectedItem().getAttribute(Constants.PARAMS);
        	Filter selectedFilter = (Filter) selectedItem.getAttribute(Constants.FILTER);
        	String dateFormat = dateFormatTextbox.getValue();
	        
	        Filter filter = new Filter();
	        filter.setCurrentDateFormat(dateFormat);
	        filter.setColumn(selectedFilter.getColumn());
	        filter.setFileName(selectedFilter.getFileName());
	        
	        if(Constants.DATA_TYPE_NUMERIC.equals(selectedFilter.getType())){
	            filter.setType(Constants.CURRENT_DATE_NUMERIC);
	        }else{
	            filter.setType(Constants.CURRENT_DATE_STRING);
	        }
	        
	        if(chartData.getIsFiltered() && !chartData.getFilters().contains(filter)) {
	            chartData.getFilters().add(filter);
	        }else{
	            chartData.getFilters().add(filter);
	        }
	            
	        chartData.setIsFiltered(true);
	        try {
	            Clients.showBusy(busyComponent, "Retriving data");
	            Events.echoEvent(new Event("onDrawChart", parent));
	        } catch(Exception ex) {
	            Clients.showNotification(Labels.getLabel("unableToFetchColumnData"), "error", 
	                    doneButton.getParent().getParent().getParent(), "middle_center", 3000, true);
	            LOG.error(Constants.EXCEPTION, ex);
	            return;
	        }
	        if(!authenticationService.getUserCredential().getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID) || 
	                authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_EDIT_DASHBOARD)){
	            doneButton.setDisabled(false);
	        }
	        
	        Events.postEvent("onDateFilterCreated", selectedItem,null);
        }
        //Detaching the filter's popup window
        Popup popup = (Popup) this.getSelf().getParent();
        popup.close();    
                
        
    }
    

}
