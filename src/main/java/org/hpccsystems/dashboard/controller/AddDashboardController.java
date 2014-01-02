package org.hpccsystems.dashboard.controller;

import java.util.HashMap;
import java.util.Map;

import org.hpccsystems.dashboard.common.Constants;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * AddDashboardController class is used to add a new dash board on the page 
 * controller class for add_dash_board.zul
 */
public class AddDashboardController  extends  GenericForwardComposer<Component>{

	private static final long serialVersionUID = 1L;
	
	private Div div;
	
	@Wire
	Window addDashboard;
	
	@Wire
	Textbox nameTxt;
		
	@Wire
	Button addBtn;
	

	@Wire
	Radiogroup layoutRgrp;
	
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		//Retrieve parameters passed here by the caller
		div = (Div) arg.get(Constants.PARENT);
		
		//Listener event when 'Add Dashboard' button is clicked
		final EventListener<Event> closeClick = new EventListener<Event>() {
			public void onEvent(final Event arg0) throws Exception {
				final Map<String,String> paramMap = new HashMap<String, String>();
				paramMap.put(Constants.DASHBOARD_NAME, nameTxt.getValue());
				paramMap.put(Constants.DASHBOARD_LAYOUT,layoutRgrp.getSelectedItem().getValue().toString());
				Events.sendEvent(new Event("onCloseDialog", div, 
						paramMap));
				addDashboard.detach();
			}
		};		
		addBtn.addEventListener(Events.ON_CLICK, closeClick);
				
	}
	
}
