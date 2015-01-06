package org.hpccsystems.dashboard.controller;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.record.formula.functions.Rows;
import org.hpccsystems.dashboard.chart.entity.AdvancedFilter;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ScoredSearchData;
import org.hpccsystems.dashboard.chart.utils.TableRenderer;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.FilterRowRenderer;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Vbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ScoredSearchController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(ScoredSearchController.class);
	
	
	@Wire
	private Listbox inputFields;
	@Wire
	private Rows filterRows;
	@Wire
	private Grid filterGrid;
	@Wire
	private Tabs tableTabs;
	@Wire
	private Tabpanels tableTabpanels;
	@Wire
	private Panel inputPanel;
	@Wire
	private Listbox groupby;
	@Wire
	private Listbox aggregate;
	@Wire
	private Panel outputPanel;
	
	@WireVariable
	private HPCCQueryService hpccQueryService;
	@WireVariable
    private TableRenderer tableRenderer;   
	
	private Portlet portlet;
	private ScoredSearchData searchData;
	private Button doneButton;
	
	private ListModelList<AdvancedFilter> rowsModelList = new ListModelList<AdvancedFilter>();
	
	EventListener<Event> onRemoveGroupbyColumn = new EventListener<Event>() {
		
		@Override
		public void onEvent(Event event) throws Exception {
			String removedColumn = ((Button)event.getTarget()).getAttribute(Constants.COLUMN_NAME).toString();
			searchData.getGroupbyColumns().remove(removedColumn);
			if(searchData.getGroupbyColumns().isEmpty()){
				searchData.setGroupbyColumns(null);
				aggregate.setSelectedItem(null);
				searchData.setAggregateFunction(null);
			}
			event.getTarget().getParent().getParent().detach();
		}
	};
	
	EventListener<Event> removeFilter = new EventListener<Event>() {
		
		@Override
		public void onEvent(Event event) throws Exception {
			rowsModelList.remove(event.getData());
		}
	};
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		portlet = (Portlet) Executions.getCurrent().getAttribute(Constants.PORTLET);
		searchData = (ScoredSearchData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
		doneButton = (Button) Executions.getCurrent().getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
		constructInputParameters();		
		if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
			rowsModelList.addAll(searchData.getAdvancedFilters());
			showGroupbyColumn();
			showSelectedAggreagteFn();
		}
		
		
		filterGrid.setModel(rowsModelList);
		filterGrid.setRowRenderer(new FilterRowRenderer());
		filterGrid.addEventListener(Constants.ON_DELETE_FILTER, removeFilter);
	}
	
	private void showGroupbyColumn() {
		if(searchData.getGroupbyColumns() != null && !searchData.getGroupbyColumns().isEmpty()){
			for(String column : searchData.getGroupbyColumns()){
				constructGroupByColumn(column);
			}
		}
	}

	private void showSelectedAggreagteFn() {
		if(searchData.getAggregateFunction() != null){
			for(Component comp :aggregate.getChildren()){
				if(comp instanceof Listitem){
					Listitem listitem =(Listitem)comp;
					if(searchData.getAggregateFunction().equals(listitem.getValue().toString())){
						((Listitem) comp).setSelected(true);
						break;
					}
				}
			}
		}
	}

	@Listen("onDrop = #filterGrid")
	public void onDropGrid(DropEvent event){
		
		if(!rowsModelList.add(new AdvancedFilter(((Listitem)event.getDragged()).getLabel()))){
			Clients.showNotification("Filter Exists",
						Clients.NOTIFICATION_TYPE_ERROR, filterGrid,
						"middle_center", 3000, true);	        
		}
	}
	
	
	 /**Gets input parameters for Roxie query
     * @throws Exception
     */
	private void constructInputParameters() throws Exception {
		Set<String> inputParameter = null;
		List<String> inputParam = new ArrayList<String>();
		inputParameter = hpccQueryService.getInputParameters(searchData
				.getFiles().iterator().next(), searchData.getHpccConnection(),
				searchData.isGenericQuery(), searchData.getInputParamQuery());
		inputParam.addAll(inputParameter);
		searchData.setInputParamNames(inputParam);
		Iterator<String> itr = inputParameter.iterator();
		while (itr.hasNext()) {
			Listitem listitem = new Listitem();
			listitem.setDraggable("true");
			listitem.setLabel(itr.next());
			listitem.setParent(inputFields);
		}

	}    
    
    @Listen("onClick = #inputDoneButton")
    public void onConfirmInputs(){
    	
    	tableTabs.getChildren().clear();
    	tableTabpanels.getChildren().clear();

		if((rowsModelList.getInnerList() != null && !rowsModelList.getInnerList().isEmpty())
				|| 	(searchData.getGroupbyColumns() != null && !searchData.getGroupbyColumns().isEmpty())){
			searchData.setAdvancedFilters(rowsModelList.getInnerList());	
			HashMap<String, HashMap<String, List<Attribute>>> hpccResult;
			try {
				hpccResult = hpccQueryService.fetchScoredSearchData(searchData);
				inputPanel.setOpen(false);
				if(hpccResult == null || hpccResult.isEmpty()){
					Clients.showNotification("No data Available",
							Clients.NOTIFICATION_TYPE_INFO, outputPanel,
							"middle_center", 3000, true);
					return;
				}
				
				//Setting Hpcc result, to avoid hitting HPCC again to get data, while clicking 'doneButton'
				searchData.setHpccTableData(hpccResult);
				//tableTab.setSelected(true);
				tableTabpanels.getChildren().clear();
				for(Entry<String, HashMap<String, List<Attribute>>> entry : hpccResult.entrySet()){
					Tab tab = new Tab(entry.getKey());
					tab.setParent(tableTabs);
					Tabpanel tabpanel = new Tabpanel();
					Vbox vbox = tableRenderer.constructScoredSearchTable(entry.getValue(),true);
					vbox.setParent(tabpanel);
					tabpanel.setParent(tableTabpanels);
				}
				doneButton.setDisabled(false);
				
			} catch (RemoteException | HpccConnectionException e) {
				LOG.error(Constants.EXCEPTION,e);
				Clients.showNotification("Unable to fetch Hpcc data",
						Clients.NOTIFICATION_TYPE_ERROR, this.getSelf(),
						"middle_center", 3000, true);
			}
			
		}    	
    }
    
    @Listen("onDrop= #groupby")
    public void onDropGroupbyListbox(DropEvent event){
    	String droppedColumn = ((Listitem)event.getDragged()).getLabel();
    	if(searchData.getGroupbyColumns() == null){
    		List<String> groupbyColumns = new ArrayList<String>();
    		searchData.setGroupbyColumns(groupbyColumns);    		
    	}else if (searchData.getGroupbyColumns().contains(droppedColumn)) {
    		Clients.showNotification("Groupby column Exists",
					Clients.NOTIFICATION_TYPE_WARNING, groupby,"middle_center", 3000, true);
    		return;
    	}
    	constructGroupByColumn(droppedColumn);
    	   	
    	searchData.getGroupbyColumns().add(droppedColumn);
    }
    
    private void constructGroupByColumn(String droppedColumn) {
    	Listitem listitem = new Listitem();
    	Listcell listcell = new Listcell();
    	Label label = new Label(droppedColumn);
    	Button removebuButton = new Button();
    	removebuButton.setSclass(Constants.CLOSE_BUTTON_STYLE);
    	removebuButton.setAttribute(Constants.COLUMN_NAME, droppedColumn);
    	removebuButton.addEventListener(Events.ON_CLICK, onRemoveGroupbyColumn);
    	label.setParent(listcell);
    	removebuButton.setParent(listcell);
    	listcell.setParent(listitem);
    	listitem.setParent(groupby); 
	}

	@Listen("onSelect = #aggregate")
    public void onSelectAggregateFunction(){
    	if(searchData.getGroupbyColumns() == null || searchData.getGroupbyColumns().isEmpty()){
    		Clients.showNotification("Drop a groupby column",
					Clients.NOTIFICATION_TYPE_WARNING, aggregate,"middle_center", 3000, true);
    		aggregate.setSelectedItem(null);
    	}else{
    		searchData.setAggregateFunction(aggregate.getSelectedItem().getValue().toString());
    	}
    			
    }

	
}
