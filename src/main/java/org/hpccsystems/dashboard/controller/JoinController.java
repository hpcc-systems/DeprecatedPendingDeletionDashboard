package org.hpccsystems.dashboard.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.Join;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
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
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Window;

/**
 * Controls the Join Screen
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class JoinController extends SelectorComposer<Component>{

    private static final long serialVersionUID = 1L;

    ChartData chartData;
    Portlet portlet;
    
    @Wire
    Listbox file1ListBox;
    @Wire
    Listbox file2ListBox;
    @Wire
    Combobox file1Combobox;
    @Wire
    Combobox file2Combobox;
    @Wire 
    Label joinType; 
    @Wire 
    Label fileOneColumn; 
    @Wire 
    Label joinCondition; 
    @Wire 
    Label fileTwoColumn;
    @Wire
    Combobox joinsCombobox;
    @Wire
    Combobox conditionsCombobox;
    @Wire
    Listbox joinConditionsListbox;
    @Wire
    Panel joinConditionPanel;
    
    @Wire
    Button addJoin;
    @Wire
    Button proceedBtn;
    
    @WireVariable
    HPCCService hpccService;
    private Window parentWindow;
    
    private Join currentJoin;
    private Set<String> filesInRelation;
    
    private static final Log LOG = LogFactory.getLog(JoinController.class);
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        Execution execution = Executions.getCurrent();
        chartData = (ChartData) execution.getAttribute(Constants.CHART_DATA);
        portlet = (Portlet) execution.getAttribute(Constants.PORTLET);
        parentWindow = (Window) Executions.getCurrent().getAttribute(Constants.PARENT);
        
        //Adding files to Combo box
        Comboitem fileComboItem;
        for(String selectedFile :chartData.getFiles()){
            fileComboItem = new Comboitem();
            fileComboItem.setLabel(selectedFile);
            fileComboItem.setValue(selectedFile);
            fileComboItem.setParent(file1Combobox);            
            fileComboItem = new Comboitem();
            fileComboItem.setLabel(selectedFile);
            fileComboItem.setValue(selectedFile);
            fileComboItem.setParent(file2Combobox);            
        }
        
        currentJoin = new Join();
        
        //Setting default Values
        conditionsCombobox.setSelectedItem((Comboitem) conditionsCombobox.getFirstChild());
        joinsCombobox.setSelectedItem((Comboitem) joinsCombobox.getFirstChild());
        onJoinTypeChange(null);
        onRelationChange(null);
        
        filesInRelation = new HashSet<String>();
    }
    
    @Listen("onSelect = #file1Combobox")
    public void onFile1Select(SelectEvent<Component, Object> event) throws Exception {
        Comboitem selectedItem =  (Comboitem) event.getSelectedItems().iterator().next();
        
        if(file2Combobox.getSelectedItem() != null 
                && selectedItem.getLabel().equals(file2Combobox.getSelectedItem().getLabel())) {
            Clients.showNotification(Labels.getLabel("fileAlreadySelected"), "error", file1Combobox, "end_center", 3000, true);
            file1ListBox.setVisible(false);
            return;
        }
        
        onFileSelect(event);
    }
    
    @Listen("onSelect = #file2Combobox")
    public void onFile2Select(SelectEvent<Component, Object> event) throws Exception {
        Comboitem selectedItem =  (Comboitem) event.getSelectedItems().iterator().next();
        
        if( file1Combobox.getSelectedItem() != null 
                && selectedItem.getLabel().equals(file1Combobox.getSelectedItem().getLabel())) {
            file2ListBox.setVisible(false);
            Clients.showNotification(Labels.getLabel("fileAlreadySelected"), "error", file2Combobox, "start_center", 3000, true);
            return;
        }
        
        onFileSelect(event);
    }
    
    public void onFileSelect(SelectEvent<Component, Object> event) throws Exception {
        Combobox combobox = (Combobox)event.getTarget();
        Comboitem selectedItem = combobox.getSelectedItem();
        
        Set<Field> columnSet;
        Listitem columnListItem;
        Listbox selectedFileListbox = (Listbox) combobox.getParent().getLastChild();
        
        columnSet = hpccService.getColumns(selectedItem.getLabel(), chartData.getHpccConnection());
        
        //Clear Listitems without header
        List<Listitem> listemsToRemove = new ArrayList<Listitem>();
        for (Component component : selectedFileListbox.getChildren()) {
            if(component instanceof Listitem) {
                listemsToRemove.add((Listitem) component);
            }
        }
        for (Listitem listitem : listemsToRemove) {
            listitem.detach();
        }
        
        for(Field field :columnSet){
            columnListItem = new Listitem();
            columnListItem.setAttribute(Constants.FIELD, field);
            Listcell listcell = new Listcell();
            if(DashboardUtil.checkNumeric(field.getDataType())) {
                listcell.setIconSclass("glyphicon-numeric");
            } else {
                listcell.setIconSclass("glyphicon-string");
            }
            listcell.setLabel(field.getColumnName());
            columnListItem.appendChild(listcell);
            selectedFileListbox.appendChild(columnListItem);    
        }
        
        selectedFileListbox.setVisible(true);
    }

    
    /**
     * Column select listener for Left Side
     * @param event
     */
    @Listen("onSelect = #file1ListBox")
    public void onFirstColumnSelect(SelectEvent<Component, Object> event) {
        Listitem selectedItem = (Listitem) event.getSelectedItems().iterator().next();
        if(isSameDataType()) {
            StringBuilder builder = new StringBuilder(file1Combobox.getSelectedItem().getLabel());
            builder.append(".").append(selectedItem.getLabel());
            currentJoin.setFirstFileColumn(builder.toString());
            fileOneColumn.setValue(builder.toString());
            fileOneColumn.setSclass("wrappedText selectedText");
            
            enableAddRelation();
        } else {
            selectedItem.setSelected(false);
            Clients.showNotification(Labels.getLabel("datatypeMismatch"), "error", selectedItem.getParent(), "end_center", 3000, true);
        }
    }
    
    /**
     * Column select listener for Right Side
     * @param event
     */
    @Listen("onSelect = #file2ListBox")
    public void onSecondColumnSelect(SelectEvent<Listitem, Object> event) {
        Listitem selectedItem = (Listitem) event.getSelectedItems().iterator().next();
        if(isSameDataType()) {
            StringBuilder builder = new StringBuilder(file2Combobox.getSelectedItem().getLabel());
            builder.append(".").append(selectedItem.getLabel());
            currentJoin.setSecondFileColumn(builder.toString());
            fileTwoColumn.setValue(builder.toString());
            fileTwoColumn.setSclass("wrappedText selectedText");
            
            enableAddRelation();
        } else {
            selectedItem.setSelected(false);
            Clients.showNotification(Labels.getLabel("datatypeMismatch"), "error", selectedItem.getParent(), "start_center", 3000, true);
        }
    }
    
    /**
     * @return
     *     Whether both columns' data type matches
     */
    private boolean isSameDataType() {
        if(file1ListBox.getSelectedItem() != null 
                && file2ListBox.getSelectedItem() != null) {
            Field field1 = (Field) file1ListBox.getSelectedItem().getAttribute(Constants.FIELD);
            Field field2 = (Field) file2ListBox.getSelectedItem().getAttribute(Constants.FIELD);
            
            if(DashboardUtil.checkNumeric(field1.getDataType()) &&
                    DashboardUtil.checkNumeric(field2.getDataType())) {
                return true;
            } else if (!DashboardUtil.checkNumeric(field1.getDataType()) &&
                    !DashboardUtil.checkNumeric(field2.getDataType())) {
                return true;
            } else {
                return false;
            }
        }
        
        return true;
    }
    
    
    /**
     * Checks all weather all required fields are chosen and makes 'add relation' button visible
     */
    private void enableAddRelation() {
        if(currentJoin.getSql() != null) {
            addJoin.setDisabled(false);
        }
    }
    
    /**
     * Event Listener to transfer the join conditions to edit chart page.
     */
    @Listen("onClick = #proceedBtn")
    public void onClickProceed(Event event) throws Exception {
        
        if(chartData.getJoins()!=null && !chartData.getJoins().isEmpty()) {
            //Removing files for which no relations are defined
            chartData.getFiles().retainAll(filesInRelation);
            if(LOG.isDebugEnabled()) {
                LOG.debug("Relations are defined");
            }
            Events.sendEvent("onIncludeDetach", parentWindow, Constants.EDIT_WINDOW_JOIN_DATA);
        } else {
            Clients.showNotification(Labels.getLabel("noRelationDefined"), "error", this.getSelf(), "middle_center", 3000, true);
        }
    }
        
    @Listen("onClick = #addJoin")
    public void onClickAddJoin() {
        
        if(chartData.getJoins() == null) {
            Set<Join> joins = new LinkedHashSet<Join>();
            chartData.setJoins(joins);
        }
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("Current Join - " + currentJoin.getSql());
        }
        
        if(chartData.getJoins().add(currentJoin)) {
            Listitem listitem = new Listitem(currentJoin.getSql());
            joinConditionsListbox.appendChild(listitem);
            
            //Re instantiating Current Join
            currentJoin = new Join(currentJoin.getType(), 
                    currentJoin.getCondition(), 
                    null, null);
            
            //Clearing selected columns
            file1ListBox.clearSelection();
            fileOneColumn.setValue(Labels.getLabel("chooseColumn1"));
            fileOneColumn.setSclass("");
            file2ListBox.clearSelection();
            fileTwoColumn.setValue(Labels.getLabel("chooseColumn2"));
            fileTwoColumn.setSclass("");
            addJoin.setDisabled(true);
        } else {
            Clients.showNotification(Labels.getLabel("joinconditionalreadyadded"), "error", joinConditionPanel, "middle_center", 3000, true);
            return;
        }
        
        // Adding Files to the relation
        filesInRelation.add(file1Combobox.getSelectedItem().getLabel());
        filesInRelation.add(file2Combobox.getSelectedItem().getLabel());
        
    }
    
    /**
     * Appends the join type(inner,outer) to the condition
     * @param event
     */
    @Listen("onChange = #joinsCombobox")
    public void onJoinTypeChange(Event event) {
        currentJoin.setType((String) joinsCombobox.getSelectedItem().getValue());
        joinType.setValue(joinsCombobox.getSelectedItem().getLabel());
        joinType.setSclass("selectedText");
        enableAddRelation();
    }
    
    /**
     * Appends relatin(=,<,>) to the condition
     * @param event
     */
    @Listen("onChange = #conditionsCombobox")
    public void onRelationChange(Event event) {
        currentJoin.setCondition((String) conditionsCombobox.getSelectedItem().getValue());
        joinCondition.setValue(conditionsCombobox.getSelectedItem().getLabel());
        joinCondition.setSclass("selectedText");
        enableAddRelation();
    }

}
