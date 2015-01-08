package org.hpccsystems.dashboard.manage.widget;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.charts.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class TableWidgetController extends ConfigurationComposer<Component>{
	
	 private static final String ON_LOADING = "onLoading";
	 private static final long serialVersionUID = 1L;
	 private static final Logger LOGGER = LoggerFactory.getLogger(TableWidgetController.class);

	/**
	 * 
	 */
	private Table table;
	
	@Wire
	private Listbox columnListbox;
	 private ListModelList<TableColumn> columns = new ListModelList<TableColumn>();
	 
	  private ListitemRenderer<TableColumn> tableColumnRenderer = (listitem, column, index) -> {
          Listcell listcell = new Listcell(column.getColumn());
          Button button = null;
          if(!column.getAggregation().equals(null)){
        	   button = new Button();
        	   button.setLabel(column.getAggregation().toString());
               button.setZclass("btn btn-xs");
               listcell.appendChild(button);
          }
       
          
          Button closeButton=new Button();
          closeButton.setIconSclass("z-icon-times");
          closeButton.addEventListener("onClick", event -> {
           
          });
          listcell.appendChild(closeButton);
          listitem.appendChild(listcell);
      };
	 
	 @Override
	    public void doAfterCompose(Component comp) throws Exception {
	        super.doAfterCompose(comp);
	        table = (Table) widgetConfiguration.getWidget();
	        hpccConnection = widgetConfiguration.getDashboard().getHpccConnection();
	        comp.addEventListener(ON_LOADING, loadingListener);
	        
	        Clients.showBusy(comp, "Fetching fields");
	        Events.echoEvent(ON_LOADING, comp, null);
	        
	        columnListbox.setModel(columns);
	        columnListbox.setItemRenderer(tableColumnRenderer);
	        
	        if(table.isConfigured()) {
	        	//columns.add(table.);
	            drawChart();
	        }
	      
	        
	 }
	  @Listen("onDrop = #columnListbox")
	    public void onDropColumns(DropEvent event) {
		  Listitem draggedItem = (Listitem) event.getDragged();
	      TableColumn column = new TableColumn(draggedItem.getValue());
	      table.setColumn(column);;
	      columns.add(column);
	      columnListbox.setDroppable(Constants.FALSE);
	       
	       drawChart();
	    }

}
