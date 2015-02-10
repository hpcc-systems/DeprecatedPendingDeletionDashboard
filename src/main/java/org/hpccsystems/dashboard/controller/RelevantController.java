package org.hpccsystems.dashboard.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.RelevantData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class RelevantController extends SelectorComposer<Component>{
	
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(RelevantController.class);
	
	private Portlet portlet;
	private RelevantData relevantData;
	private Button doneButton;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		portlet = (Portlet) Executions.getCurrent().getAttribute(Constants.PORTLET);
		relevantData = (RelevantData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
		doneButton = (Button) Executions.getCurrent().getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
		
		doneButton.setDisabled(false);
		
	}
	
	@Listen("onClick = #submitBtn")
    public void onVisualizeButtonClickForRelevant(Event event) {
    	System.out.println("VisualizeButton Clicked...");
    	//Events.sendEvent("onClick$doneButton", this.getSelf().getParent(), null);
    	//Events.sendEvent("onDrawingLiveChart", window, portlet);
    	//Events.sendEvent(new Event("onClick", doneButton, null));
    	Events.postEvent("closeEditWindow", doneButton, null);
    }
	
}
