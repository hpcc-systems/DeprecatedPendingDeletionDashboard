package org.hpccsystems.dashboard.controller;

import java.util.LinkedHashMap; 
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.utils.TableRenderer;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditTableController extends SelectorComposer<Component> {
	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(EditTableController.class);

	@Wire
	Listbox sourceList, targetList;
	
	@Wire
	Div tableHolder;
	
	@WireVariable
	private DashboardService dashboardService;
	
	@WireVariable
	TableRenderer tableRenderer;
	
	@WireVariable
	HPCCService hpccService;

	private XYChartData tableData;
	private Portlet portlet;
	private Button doneButton;
	
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		portlet = (Portlet) Executions.getCurrent().getAttribute(Constants.PORTLET);
		tableData = (XYChartData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
		doneButton = (Button) Executions.getCurrent().getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
		
		sourceList.addEventListener(Events.ON_DROP, dropListener);
		targetList.addEventListener(Events.ON_DROP, dropListener);
		
		Map<String,String> columnSchemaMap = hpccService.getColumnSchema(tableData.getFileName(), tableData.getHpccConnection());
		
		
		Listitem listItem;
		if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
			for (Map.Entry<String, String> entry : columnSchemaMap.entrySet()) {
				listItem = new Listitem(entry.getKey());
				listItem.setDraggable("true");
				listItem.setDroppable("true");
				listItem.addEventListener(Events.ON_DROP, dropListener);
				
				if(tableData.getTableColumns().contains(entry.getKey())) {
					listItem.setParent(targetList);
				} else {
					listItem.setParent(sourceList);
				}
			}
			
			//TODO: Add else part
			if(portlet.getTableDataMap() != null){
				tableHolder.appendChild(
						tableRenderer.constructTableWidget(portlet.getTableDataMap(), true,portlet.getName())
					);
			}
		} else {
			for (Map.Entry<String, String> entry : columnSchemaMap.entrySet()) {
				listItem = new Listitem(entry.getKey());
				listItem.setDraggable("true");
				listItem.setDroppable("true");
				listItem.addEventListener(Events.ON_DROP, dropListener);
				listItem.setParent(sourceList);
			}
		}
	}
		
	/**
	 * Common Drop event listener for both listbox and listitem
	 */
	private EventListener<DropEvent> dropListener = new EventListener<DropEvent>() {

		public void onEvent(DropEvent event) throws Exception {
			Component dragged = event.getDragged();
			Component dropped = event.getTarget();
			
			if(dropped instanceof Listitem) {
				dropped.getParent().insertBefore(dragged, dropped);
			} else {
				//When dropped in list box
				dropped.appendChild(dragged);
			}
		}
		
	};
	
	@Listen("onClick = #drawTable")
	public void drawTable() {
		tableData.getTableColumns().clear();
		if(targetList.getChildren().size() > 1) {
			tableData.getTableColumns().clear();
			Listitem listitem;
			for (Component component : targetList.getChildren()) {
				if(component instanceof Listitem){
					listitem = (Listitem) component;
					tableData.getTableColumns().add(
								listitem.getLabel()
							);
				}
			}
			
			LinkedHashMap<String, List<String>> tableValues = null;
			try {
				tableValues = hpccService.fetchTableData(tableData);
				
				tableHolder.getChildren().clear();
				tableHolder.appendChild(
						tableRenderer.constructTableWidget(tableValues, true,portlet.getName())
						);
			} catch (Exception e) {
				Clients.showNotification("Table Creation failed. Please try again.", "error", tableHolder, "middle_center", 3000, true);
				LOG.error("Table creation failed", e);
				return;
			}
			
			doneButton.setDisabled(false);
			
			portlet.setTableDataMap(tableValues);
		} else {
			Clients.showNotification("Move some columns over here to draw a Table", "error", targetList, "middle_center", 3000, true);
		}
	}

}
