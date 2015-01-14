package org.hpccsystems.dashboard.manage;

import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.Contract;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.HIPIEService;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.FLOW;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.DashboardService;
import org.hpccsystems.dashboard.util.HipieSingleton;
import org.hpccsystems.dashboard.util.HipieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.json.JSONObject;
import org.zkoss.json.parser.JSONParser;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Window;

import com.google.gson.JsonObject;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DashboardController extends SelectorComposer<Component> {
	private static final String LAYOUT = "layout";
    private static final String WS_ECL = "WsEcl";
    private static final String WS_WORKUNITS = "WsWorkunits";
    private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

	@WireVariable
	private AuthenticationService authenticationService;
	@WireVariable
	private DashboardService dashboardService;
	@Wire
	private Div chartDiv;

	private Dashboard dashboard;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		dashboard = (Dashboard) Executions.getCurrent().getAttribute(
				Constants.ACTIVE_DASHBOARD);
		
		// OnEditChart 
        comp.addEventListener("onEditChart", event -> {
            JSONObject json = (JSONObject) new JSONParser().parse(event.getData().toString()); 
            LOGGER.info(json.get("chartId").toString());
            editChart(json.get("chartId").toString());
        });
		
     // OnDeleteChart 
        comp.addEventListener("onDeleteChart", event -> {
            JSONObject json = (JSONObject) new JSONParser().parse(event.getData().toString()); 
            LOGGER.info(json.get("chartId").toString());
        });
        
		if(dashboard.getCompositionName() != null){
            drawChart(true);
        } else {
            drawChart(false);
        }
		
		chartDiv.addEventListener("onSave", event->{		   
		   dashboard.setLayout(event.getData().toString());
		   dashboardService.updateLayout(dashboard);
		});

	}

	/**
	 * Renders chart in dashboard container
	 */
	private void drawChart(boolean isLive) {
		try {

			 String viaualizationURL = isLive ? dashboard.generateVisualizationURL() : "[]";
            if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("viaualizationURL -->" + viaualizationURL);
			}
			JsonObject chartObj = new JsonObject();
			chartObj.addProperty(Constants.URL, viaualizationURL);
			chartObj.addProperty(Constants.TARGET, chartDiv.getUuid());
			chartObj.addProperty(Constants.HPCC_ID, dashboard.getHpccId());
            chartObj.addProperty(WS_WORKUNITS, dashboard.getHpccConnection().getESPUrl() + WS_WORKUNITS);
            chartObj.addProperty(WS_ECL, dashboard.getHpccConnection().getRoxieServiceUrl() + WS_ECL);
            chartObj.addProperty(LAYOUT, dashboard.getLayout());

            String data = StringEscapeUtils.escapeJavaScript(chartObj.toString());
            Clients.evalJavaScript("visualizeDDLChart('" + data + "')");
		} catch (Exception e) {
			LOGGER.error(Constants.EXCEPTION, e);
			Clients.showNotification("Unable to recreate chart",
					Clients.NOTIFICATION_TYPE_ERROR, chartDiv, "middle_center",
					5000, true);
		}
	}
	
	@Listen("onClick = #saveLayout")
	public void onSaveLayout() {
	    Clients.evalJavaScript("saveLayout('"+chartDiv.getUuid()+"')");
	}

    @Listen("onClick = #addWidget")
    public void onAddWidget() {
        Window window = (Window) Executions.createComponents("widget/config.zul", null, new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                WidgetConfiguration widgetConfig = new WidgetConfiguration(dashboard, chartDiv);
                widgetConfig.setFlowType(FLOW.NEW);
                put(Constants.WIDGET_CONFIG, widgetConfig);
            }
        });

        window.doModal();
    }

	@Listen("onClick = #deleteDashboard")
	public void deleteDashboard() {
        EventListener<ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                Component component = DashboardController.this.getSelf().getParent().getParent().getFellow("navHolder");
                Events.postEvent(Constants.ON_DELTE_DASHBOARD, component, dashboard);
            }

        };

		Messagebox.show(
                Labels.getLabel("deletedashboard"), 
                Labels.getLabel("deletedashboardtitle"), 
                new Messagebox.Button[] {
                    Messagebox.Button.YES, Messagebox.Button.NO 
                }, 
                Messagebox.QUESTION, 
                clickListener);
	}
	
	  public void editChart(String chartName){
	        HIPIEService hipieService = HipieSingleton.getHipie();
	        String userId = authenticationService.getUserCredential().getId();
	        Composition composition = null;
	        ContractInstance contractInstance = null;
	        try {
	            composition = hipieService.getComposition(userId, dashboard.getCompositionName());
	            contractInstance = composition.getContractInstanceByName(composition.getName());
	          
	         Widget widget =  HipieUtil.getVisualElementWidget(contractInstance,chartName);
	         WidgetConfiguration widgetConfiguration = new WidgetConfiguration(dashboard, chartDiv);
	         widgetConfiguration.setWidget(widget);
	         widgetConfiguration.setFlowType(FLOW.EDIT);
	         //Opens Edit window
	         Window window = (Window) Executions.createComponents("widget/config.zul", this.getSelf(), new HashMap<String, Object>() {
	             private static final long serialVersionUID = 1L;
	             {
	                 put(Constants.WIDGET_CONFIG, widgetConfiguration);
	             }
	         });

	         window.doModal();
	        } catch (Exception e) {
	          LOGGER.debug(Constants.EXCEPTION,e);
	        }
	        
	    }

 }
