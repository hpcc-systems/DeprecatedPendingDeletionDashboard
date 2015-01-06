package org.hpccsystems.dashboard.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.common.Constants;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Vlayout;

public class FilterController extends SelectorComposer<Panel>{

    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(FilterController.class);
    
    private ChartData chartData;
    private Button doneButton;
    private Component busyComponent;
    private Component parent;
    
    @Wire
    private Listbox filterListBox;
    @Wire
    private Vlayout dateFilterContainer;
    
    private EventListener<Event> filterAddListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Filter filter = (Filter) event.getData();
            createFilterListItem(filter);
        }
    };
    
    private EventListener<Event> onEnableApiFilter = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            String enableFilter = event.getData().toString();
            filterListBox.setDroppable(enableFilter);
        }
    };
    
    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        Execution execution = Executions.getCurrent();
        chartData =  (ChartData) execution.getAttribute(Constants.CHART_DATA);
        doneButton = (Button) execution.getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
        
        return super.doBeforeCompose(page, parent, compInfo);
    }
    
    @Override
    public void doAfterCompose(Panel comp) throws Exception {
        super.doAfterCompose(comp);
        
        busyComponent = (Component) Executions.getCurrent().getArg().get(Constants.BUSY_COMPONENT);
        parent = (Component) Executions.getCurrent().getAttribute(Constants.PARENT);
                
        // Sending attributes to Current date filter
        dateFilterContainer.setAttribute(Constants.CHART_DATA, chartData);
        dateFilterContainer.setAttribute(Constants.EDIT_WINDOW_DONE_BUTTON, doneButton);
        dateFilterContainer.setAttribute(Constants.PARENT, comp);
        dateFilterContainer.setAttribute(Constants.BUSY_COMPONENT, busyComponent);
        Events.postEvent("onInitialize", dateFilterContainer, null);
        
        //Event listener to add persisted filters
        this.getSelf().getParent().addEventListener("onFilterAdded", filterAddListener);
        this.getSelf().getParent().addEventListener(Constants.API_FILTER_EVENT, onEnableApiFilter);
    }
    
    /**
     * Method to handle filters in Edit window
     * @param dropEvent
     */
    @Listen("onDrop = #filterListBox")
    public void onDropToFilterItem(final DropEvent dropEvent) {
        final Listitem draggedListitem = (Listitem) ((DropEvent) dropEvent).getDragged();
        Filter filter = new Filter();
        filter.setColumn(draggedListitem.getLabel());
        filter.setType((Integer) draggedListitem.getAttribute(Constants.COLUMN_DATA_TYPE));
        Tabpanel tabpanel = (Tabpanel) draggedListitem.getParent().getParent();
        filter.setFileName(tabpanel.getLinkedTab().getLabel());        
        if(chartData.getFilters().contains(filter)) {
            Clients.showNotification(Labels.getLabel("columnAlreadyAdded"), Constants.ERROR_NOTIFICATION,
                    filterListBox, Constants.POSITION_END_CENTER, 3000, true);
            return;
        }
        createFilterListItem(filter);
    }
    
    private void createFilterListItem(final Filter filter) {
        Listitem filterItem = new Listitem();
        filterItem.setAttribute(Constants.FILTER, filter);
        final Listcell listcell = new Listcell(filter.getColumn());
        
        final Button playBtn = new Button();
        playBtn.setSclass("glyphicon glyphicon-play btn btn-link img-btn");
        playBtn.setStyle("float:right");
        
        Popup popup = new Popup();        
        popup.setZclass(Constants.STYLE_POPUP);
        popup.setId(filter.getColumn() + "_filterPopup");
        Include include = new Include();
        include.setDynamicProperty(Constants.PARENT, parent);
        include.setDynamicProperty(Constants.BUSY_COMPONENT, busyComponent);
        include.setDynamicProperty(Constants.FILTER, filter);
        include.setDynamicProperty(Constants.CHART_DATA, chartData);
        include.setDynamicProperty(Constants.EDIT_WINDOW_DONE_BUTTON, doneButton);

        if (Constants.DATA_TYPE_NUMERIC.equals(filter.getType())) {
            include.setSrc("layout/numeric_filter_popup.zul");
        }else{
            include.setSrc("layout/string_filter_popup.zul");
        }
        
        listcell.appendChild(popup);
        popup.appendChild(include);
        playBtn.setPopup(filter.getColumn() + "_filterPopup, position=end_center");

        Button closeBtn = new Button();
        closeBtn.setSclass(Constants.CLOSE_BUTTON_STYLE);
        closeBtn.addEventListener(Events.ON_CLICK, filterClearListener);
        listcell.appendChild(closeBtn);

        listcell.appendChild(playBtn);
        listcell.setTooltiptext(filter.getColumn());
        filterItem.appendChild(listcell);

        filterListBox.appendChild(filterItem);
        
        //Event listeners to communicate with Current date filter
        filterItem.addEventListener("onDateFilterCreated", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                listcell.setIconSclass("glyphicon glyphicon-calendar");
                playBtn.setVisible(false);                
            }
        });
        
        filterItem.addEventListener("onRevert", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                filter.setType((Integer) event.getData());
                listcell.setIconSclass(null);
                playBtn.setVisible(true);                
            }
        });
        
        
        //Sending Event to create new item for current date filter
        Events.sendEvent("onFilterAdded", dateFilterContainer, filterItem);
    }

    // Listener to close filter window
    EventListener<Event> filterClearListener = new EventListener<Event>() {
        public void onEvent(final Event event) {    
            Listitem listItem =(Listitem) event.getTarget().getParent().getParent();
            Filter filter = (Filter) listItem.getAttribute(Constants.FILTER);
            
            //Sending Event to remove item for current date filter
            Events.sendEvent("onFilterRemoved", dateFilterContainer, filter);
            
            chartData.getFilters().remove(filter);
            
            if(chartData.getFilters().isEmpty()){
                chartData.setIsFiltered(false);
            }
            try {
               Clients.showBusy(busyComponent, "Retriving data");
               Events.echoEvent(new Event(Constants.DRAW_CHART_EVENT, parent));
            } catch (Exception ex) {
                Clients.showNotification(Labels.getLabel("unableToFetchHpccData"), 
                        Constants.ERROR_NOTIFICATION,    
                        parent, 
                        Constants.POSITION_CENTER, 3000, true);
                LOG.error(Constants.EXCEPTION, ex);
            }
            
            listItem.detach();
        }
    };
}
