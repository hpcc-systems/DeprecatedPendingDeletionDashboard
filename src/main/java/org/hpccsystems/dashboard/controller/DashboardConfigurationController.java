package org.hpccsystems.dashboard.controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
public class DashboardConfigurationController extends SelectorComposer<Component>{

	private static final long serialVersionUID = 1L;
	private static final  Log LOG = LogFactory.getLog(DashboardConfigurationController.class); 
	
	@Wire("radio")
	ArrayList<Radio> radioList;

	@Wire
	Radiogroup layoutRadiogroup;
	@Wire
	Textbox nameTextbox;
	
	private Component parent;
	private Dashboard dashboard; 
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		   
		parent = (Component) Executions.getCurrent().getArg().get(Constants.PARENT);
		
		if(parent instanceof Window) {
			//Dashboard already created
			
			dashboard = (Dashboard) Executions.getCurrent().getArg().get(Constants.DASHBOARD);
			
			nameTextbox.setValue(dashboard.getName());
			
			try {
				radioList.get(dashboard.getColumnCount() - 1).setSelected(true);
			} catch (ArrayIndexOutOfBoundsException e) {
				Clients.showNotification("No widgets are present in Dashboard. Choose a layout.", "info", getSelf(), "middle_center", 3000, true);
			}
		} else {
			//Creating a new Dashboard 
			
			//Setting two column layout as default
			radioList.get(1).setSelected(true);
		}
		
	}
	
	@Listen("onClick = #doneButton")
	public void done() {
		if(parent instanceof Window) {
			dashboard.setName(nameTextbox.getValue());		
			dashboard.setColumnCount(Integer.parseInt(layoutRadiogroup.getSelectedItem().getValue().toString()));
			Events.sendEvent("onLayoutChange", parent, null);
		} else {
			Dashboard dashboard = new Dashboard();
			dashboard.setName(nameTextbox.getValue());
			dashboard.setColumnCount(Integer.parseInt(layoutRadiogroup.getSelectedItem().getValue().toString()));
			dashboard.setUpdatedDate(new Date(Calendar.getInstance().getTime().getTime()));
			
			//Deciding Columns and rows
			Integer panelCount = null;
			if( dashboard.getColumnCount() == 1){
				panelCount = 2;
			} else if (dashboard.getColumnCount() == 2){
				panelCount = 4;
			} else if (dashboard.getColumnCount() == 3){
				panelCount = 9;
			}
			
			if(LOG.isDebugEnabled()){
				LOG.debug("Creating A New Dashboard.. and adding panels");
			}
			for(int i=1; i <= dashboard.getColumnCount(); i++){
				for(int j=1; j <= panelCount ; j++){
					if(j > (panelCount/dashboard.getColumnCount())*(i-1)  && 
							j <= (panelCount/dashboard.getColumnCount())*i) {
						if(LOG.isDebugEnabled()){
							LOG.debug("Adding panel " + j);
						}
						final Portlet portlet = new Portlet();
						
						//generating portlet id
						Integer portletId = j ;
						portlet.setId(portletId);
						portlet.setColumn(i - 1);
						portlet.setWidgetState(Constants.STATE_EMPTY);
						dashboard.getPortletList().add(portlet);
					} else{
						continue;
					}	
				}
			}
			
			Events.sendEvent("onCloseDialog", parent, dashboard);
		}
		
		this.getSelf().detach();
	}
}
