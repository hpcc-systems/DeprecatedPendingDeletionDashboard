package org.hpccsystems.dashboard.manage.widget.filters;

import java.util.List;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.StringFilter;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Popup;

public class StringFilterContoller extends SelectorComposer<Component>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StringFilter filter;
	private Field field;
	private Button doneButton;
	private ListModelList<String> listOfvalues;
	 
    @WireVariable
    AuthenticationService  authenticationService;
    
    @WireVariable
    WSSQLService WSSQLService;
    
    @Wire
    Listbox filterListBox;
    
    @Wire
    Button filtersSelectedBtn;
    
	 @Override
	    public void doAfterCompose(Component comp) throws Exception {
	        super.doAfterCompose(comp);
	       WidgetConfiguration widgetconfig= (WidgetConfiguration) Executions.getCurrent().getAttribute(org.hpccsystems.dashboard.Constants.WIDGET_CONFIG);
	       filter = (StringFilter) Executions.getCurrent().getAttribute(Constants.FILTER);
	       
	        List<String> valueList = null;
	        try    {
	        	 valueList = WSSQLService.getDistinctValues(field,widgetconfig.getDashboard().getHpccConnection(), widgetconfig.getWidget().getLogicalFile().toString(), widgetconfig.getWidget().getFilters());
	        	
	        }catch(Exception e){
	        	
	        }
	        if(!valueList.isEmpty()){
	        	 listOfvalues = new ListModelList<String>(valueList);
	        	 filterListBox.setModel(listOfvalues);
	        }
	 }
	 
	 
	 @Listen("onClick = button#filtersSelectedBtn")
	    public void onfiltersSelected() { 
		   List<String> selectedValues = (List<String>) listOfvalues.getSelection();
	       
	        // Check for no values selected
	        if(selectedValues.isEmpty()) {
	            Clients.showNotification(Labels.getLabel("noFilterareSelected"), "error", 
	                    doneButton.getParent().getParent().getParent(), "middle_center", 3000, true);
	            return;
	        }
	        filter.setValues(selectedValues);

	        //Detaching the filter's popup window
	        Popup popup = (Popup) this.getSelf().getParent().getParent();
	        popup.close();
	        
	    }


}
