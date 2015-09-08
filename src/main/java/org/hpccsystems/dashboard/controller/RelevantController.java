package org.hpccsystems.dashboard.controller;

import java.rmi.RemoteException;
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
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class RelevantController extends SelectorComposer<Component>{
	
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(RelevantController.class);
	
	private static final String RELEVANT_GROUP_TYPE_ID_QUERY = "beijingiac_claim_group_type_list_srvc";
	private static final String RELEVANT_GROUP_ID_QUERY = "beijingiac_claim_group_list_srvc";
	private static final String RELEVANT_CLAIM_IDS_QUERY = "relevant_claimslist";
	
	private Portlet portlet;
	private RelevantData relevantData;
	private Button doneButton;
	@Wire
	private Combobox claimCombobox;
	@Wire
	private Combobox groupTypeIdCombobox;
	@Wire
    private Combobox groupIdCombobox;
	
	@WireVariable
	private HPCCQueryService hpccQueryService;
	@WireVariable
    private HPCCService hpccService;
	
	private ListModelList<String> groupTypeIdModel = new ListModelList<String>();
	
	private ListModelList<String> groupIdModel = new ListModelList<String>();
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		portlet = (Portlet) Executions.getCurrent().getAttribute(Constants.PORTLET);
		relevantData = (RelevantData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
		doneButton = (Button) Executions.getCurrent().getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
		
		doneButton.setDisabled(false);
		Set<Field> fields = hpccQueryService.getColumns(RELEVANT_CLAIM_IDS_QUERY, relevantData.getHpccConnection(), false, null);
	      LOG.debug("fields -->"+fields);
	      
	      ChartData inputParamData = new TableData();
	      inputParamData.setHpccConnection(relevantData.getHpccConnection());
	      List<String> files = new ArrayList<String>();
	      files.add(RELEVANT_CLAIM_IDS_QUERY);
	      inputParamData.setFiles(files);
	      
	      List<Attribute> attributes = new ArrayList<Attribute>();
	      Attribute attr = new Attribute();
	      attr.setColumn(fields.iterator().next().getColumnName());
	      attributes.add(attr);	      
	      ((TableData)inputParamData).setAttributes(attributes);
	      
	      ListModelList<String> inputParam = new ListModelList<String>();
	      Map<String, List<Attribute>> inputs= hpccQueryService.fetchTableData((TableData)inputParamData,portlet.getTitleColumns());
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
	      
	     populateGroupTypeId();		
	}
	
	private void populateGroupTypeId() {
	   try {
        Set<String> groupTypes = hpccQueryService.getRelevantGroupInfo(RELEVANT_GROUP_TYPE_ID_QUERY,relevantData,false);
        groupTypeIdModel.addAll(groupTypes);
        groupTypeIdCombobox.setModel(groupTypeIdModel);
    } catch (RemoteException | HpccConnectionException e) {
        LOG.error(Constants.EXCEPTION,e);
        Clients.showNotification(Labels.getLabel("unableToFetchGroupTypeIds"), "error", groupTypeIdCombobox, "after_center", 3000, true);
    }
        
    }

    @Listen("onClick = #submitBtn")
    public void onVisualizeButtonClickForRelevant(Event event) {
    	System.out.println("VisualizeButton Clicked...");
    	//Events.sendEvent("onClick$doneButton", this.getSelf().getParent(), null);
    	//Events.sendEvent("onDrawingLiveChart", window, portlet);
    	//Events.sendEvent(new Event("onClick", doneButton, null));
    	relevantData.setGroupId(groupIdModel.getSelection().iterator().next());
    	LOG.debug("relevantData --->"+relevantData);
    	Events.postEvent("closeEditWindow", doneButton, null);
    }
	
	@Listen("onChange = #claimCombobox")
	public void onChangeClaimIds(){
	    relevantData.setClaimId(claimCombobox.getSelectedItem().getLabel());
	}
	
	@Listen("onSelect = #groupTypeIdCombobox")
	public void onSelectGroupType(SelectEvent<Component, String> event){
	    String selectedGroupType = event.getSelectedObjects().iterator().next();
	    relevantData.setGroupTypeId(selectedGroupType);
	    
	    try {
            Set<String> groupIds = hpccQueryService.getRelevantGroupInfo(RELEVANT_GROUP_ID_QUERY,relevantData,true);
            groupIdModel.addAll(groupIds);
            groupIdCombobox.setModel(groupIdModel);
        } catch (RemoteException | HpccConnectionException e) {
           LOG.error(Constants.EXCEPTION,e);
           Clients.showNotification(Labels.getLabel("unableToFetchGroupIds"), "error", groupTypeIdCombobox, "after_center", 3000, true);
        }
	    
	}
	
}
