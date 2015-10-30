package org.hpccsystems.dashboard.controller;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
import org.hpccsystems.dashboard.entity.RelevantGroupType;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class RelevantController extends SelectorComposer<Component>{
	
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(RelevantController.class);
	
	private static final String RELEVANT_GROUP_TYPE_ID_QUERY = "beijingiac_claim_group_type_list_srvc";
	private static final String RELEVANT_GROUP_ID_QUERY = "beijingiac_claim_group_list_srvc";
	private static final String RELEVANT_CLAIM_IDS_QUERY = "relevant_claimslist";
    private static final String SELECT = "--Select--";
    private static final Object PARANTHESIS_OPEN = "(";
    private static final Object PARANTHESIS_CLOSE = ")";
	
	private Portlet portlet;
	private RelevantData relevantData;
    private Button doneButton;
	@Wire
	private Combobox claimCombobox;
	@Wire
	private Combobox groupTypeIdCombobox;
	@Wire
    private Combobox groupIdCombobox;
	@Wire
	private Radiogroup cmbClaim;
	@Wire
    private Radiogroup cmbPolicy;
	@Wire
    private Radiogroup cmbVehicle;
	@Wire
    private Radiogroup cmbPerson;
	
	@WireVariable
	private HPCCQueryService hpccQueryService;
	@WireVariable
    private HPCCService hpccService;
	
	private ListModelList<RelevantGroupType> groupTypeIdModel = new ListModelList<RelevantGroupType>();
	
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
	      inputParam.add(SELECT);
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
	          if(relevantData.getClaimId() != null && value.toString().equals(relevantData.getClaimId())){
	              claimCombobox.setSelectedItem(comboitem);
	          }
		});
	      
	     populateGroupTypeId();		    
	     populateImages();
	}
    
    private void populateImages() {
        
	    if(relevantData.getClaimImage() != null){
	       Radio selectItem = cmbClaim.getItems().stream().filter(radioItem ->
            relevantData.getClaimImage().equals(radioItem.getValue())).findAny().get();
	       cmbClaim.setSelectedItem(selectItem);
	    } 
	    if(relevantData.getPolicyImage() != null){
	           Radio selectItem = cmbPolicy.getItems().stream().filter(radioItem ->
	            relevantData.getPolicyImage().equals(radioItem.getValue())).findAny().get();
	           cmbPolicy.setSelectedItem(selectItem);
	    } 
	    if(relevantData.getVehicleImage() != null){
	           Radio selectItem = cmbVehicle.getItems().stream().filter(radioItem ->
	            relevantData.getVehicleImage().equals(radioItem.getValue())).findAny().get();
	           cmbVehicle.setSelectedItem(selectItem);
	     } 
	    if(relevantData.getPersonImage() != null){
	           Radio selectItem = cmbPerson.getItems().stream().filter(radioItem ->
	            relevantData.getPersonImage().equals(radioItem.getValue())).findAny().get();
	           cmbPerson.setSelectedItem(selectItem);
	     } 
        
    }

    ComboitemRenderer<RelevantGroupType> groupTypeRenderer = (item,data,index) ->{
        StringBuilder builder = new StringBuilder();
        builder.append(data.getDescription()).append(PARANTHESIS_OPEN)
                .append(data.getId()).append(PARANTHESIS_CLOSE);
        item.setLabel(builder.toString());
        item.setValue(data.getId());
	};
	
	private void populateGroupTypeId() {
	   try {
	        Set<RelevantGroupType> groupTypes= new LinkedHashSet<RelevantGroupType>();
	        groupTypes.add(new RelevantGroupType(SELECT,""));
	        groupTypes.addAll(hpccQueryService.getRelevantGroupTypes(RELEVANT_GROUP_TYPE_ID_QUERY,relevantData));
	        groupTypeIdModel.clear();
            groupTypeIdModel.addAll(groupTypes);
            groupTypeIdCombobox.setModel(groupTypeIdModel);
            groupTypeIdCombobox.setItemRenderer(groupTypeRenderer);
            
            //Pre-loading the previouly selected group data 
            if(relevantData.getGroupType() != null){
                List<RelevantGroupType> selectedGroupType = new ArrayList<RelevantGroupType>();               
                selectedGroupType.add(relevantData.getGroupType());
                groupTypeIdModel.setSelection(selectedGroupType);
                
                List<String> selectedGroup = new ArrayList<String>();
                selectedGroup.add(relevantData.getGroupId());
                groupIdModel.addAll(selectedGroup);
                groupIdModel.setSelection(selectedGroup);
                groupIdCombobox.setModel(groupIdModel);
            }
    } catch (RemoteException | HpccConnectionException e) {
        LOG.error(Constants.EXCEPTION,e);
            Clients.showNotification(Labels.getLabel("unableToFetchGroupTypeIds"), Clients.NOTIFICATION_TYPE_ERROR, groupTypeIdCombobox,
                    "after_center", 3000, true);
    }
        
    }

    @Listen("onChange = #claimCombobox")
	public void onChangeClaimIds(){
        relevantData.setClaimId(null);
        if(!SELECT.equals(claimCombobox.getSelectedItem().getLabel())){
            relevantData.setClaimId(claimCombobox.getSelectedItem().getLabel());
        }
	}
	
    @Listen("onSelect = #groupIdCombobox")
    public void onSelectGroupId(SelectEvent<Component, String> event){
        relevantData.setGroupId(null);
        if(!SELECT.equals(groupIdModel.getSelection().iterator().next())){
            relevantData.setGroupId(event.getSelectedObjects().iterator().next());
        }
    }
    
	@Listen("onSelect = #groupTypeIdCombobox")
	public void onSelectGroupType(SelectEvent<Component, String> event){
	    relevantData.setGroupType(null);
	    relevantData.setGroupId(null);
	    groupIdModel.clearSelection();
	    if(!SELECT.equals(groupTypeIdModel.getSelection().iterator().next().getId())){
	        RelevantGroupType selectedGroupType = groupTypeIdModel.getSelection().iterator().next();
	        relevantData.setGroupType(selectedGroupType);
	        
	        try {
	            Set<String> groupIds = new LinkedHashSet<String>();
	            groupIds.add(SELECT);
	            groupIds.addAll(hpccQueryService.getRelevantGroups(RELEVANT_GROUP_ID_QUERY,relevantData));
	            groupIdModel.clear();
	            groupIdModel.addAll(groupIds);
	            groupIdCombobox.setModel(groupIdModel);
	        } catch (RemoteException | HpccConnectionException e) {
	           LOG.error(Constants.EXCEPTION,e);
                Clients.showNotification(Labels.getLabel("unableToFetchGroupIds"), Clients.NOTIFICATION_TYPE_ERROR, groupTypeIdCombobox,
                        "after_center", 3000, true);
	        }
	    }
	    
	}
	
	public RelevantData getRelevantData() {
        return relevantData;
    }

    public void setRelevantData(RelevantData relevantData) {
        this.relevantData = relevantData;
    }
	
}
