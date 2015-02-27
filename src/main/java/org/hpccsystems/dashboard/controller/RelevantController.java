package org.hpccsystems.dashboard.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.RelevantData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class RelevantController extends SelectorComposer<Component>{
	
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(RelevantController.class);
	
	private Portlet portlet;
	private RelevantData relevantData;
	private Button doneButton;
	@Wire
	private Combobox claimCombobox;
	
	@WireVariable
	private HPCCQueryService hpccQueryService;
	@WireVariable
    private HPCCService hpccService;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		portlet = (Portlet) Executions.getCurrent().getAttribute(Constants.PORTLET);
		relevantData = (RelevantData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
		doneButton = (Button) Executions.getCurrent().getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
		
		doneButton.setDisabled(false);
		Set<Field> fields = hpccQueryService.getColumns("relevant_claimslist", relevantData.getHpccConnection(), false, null);
	      LOG.debug("fields -->"+fields);
	      
	      ChartData inputParamData = new TableData();
	      inputParamData.setHpccConnection(relevantData.getHpccConnection());
	      List<String> files = new ArrayList<String>();
	      files.add("relevant_claimslist");
	      inputParamData.setFiles(files);
	      
	      List<Attribute> attributes = new ArrayList<Attribute>();
	      Attribute attr = new Attribute();
	      attr.setColumn(fields.iterator().next().getColumnName());
	      attributes.add(attr);	      
	      ((TableData)inputParamData).setAttributes(attributes);
	      
	      ListModelList<String> inputParam = new ListModelList<String>();
	      Map<String, List<Attribute>> inputs= hpccQueryService.fetchTableData((TableData)inputParamData);
	      LOG.debug("Input parameters -->"+ inputs);
	      
	      if(inputs.get(fields.iterator().next().getColumnName()) != null){
	          inputs.get(fields.iterator().next().getColumnName()).stream().forEach(attribute ->{
	              Attribute attribut = (Attribute)attribute;
	              inputParam.add(attribut.getColumn());
	          });
	      }
	      
	      claimCombobox.setModel(inputParam);
	      claimCombobox.setItemRenderer((comboitem,value,index)->{
	          comboitem.setLabel(value.toString());
		});
		
	}
	
	@Listen("onClick = #submitBtn")
    public void onVisualizeButtonClickForRelevant(Event event) {
    	System.out.println("VisualizeButton Clicked...");
    	//Events.sendEvent("onClick$doneButton", this.getSelf().getParent(), null);
    	//Events.sendEvent("onDrawingLiveChart", window, portlet);
    	//Events.sendEvent(new Event("onClick", doneButton, null));
    	Events.postEvent("closeEditWindow", doneButton, null);
    }
	
	@Listen("onChange = #claimCombobox")
	public void onChangeClaimIds(){
	    relevantData.setClaimId(claimCombobox.getSelectedItem().getLabel());
	}
	
}
