package org.hpccsystems.dashboard.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.api.entity.ApiChartConfiguration;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.entity.InputParam;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.utils.TableRenderer;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.InputListitem;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.hpccsystems.dashboard.util.UiGenerator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditTableController extends SelectorComposer<Component> {
    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(EditTableController.class);

    @Wire
    private Listbox targetList;    
    @Wire
    private Div tableHolder;
    @Wire
    private Listbox filterList;
    @Wire
    private Panel inputPanel;
    @Wire
    private Listbox inputParams;
    @Wire
    private Panel filterPanel;
    @Wire
    private Tabbox sourceTabBox;
    
    @Wire
    private Checkbox changeIndicatorCheckbox;
    
    @WireVariable
    private AuthenticationService  authenticationService;    
    @WireVariable
    private TableRenderer tableRenderer;    
    @WireVariable
    private HPCCService hpccService;
    @WireVariable
    private HPCCQueryService hpccQueryService;

    private TableData tableData;
    private Portlet portlet;
    private Button doneButton;
    
    private Set<Attribute> selectedColumns;
    
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        portlet = (Portlet) Executions.getCurrent().getAttribute(Constants.PORTLET);
        tableData = (TableData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
        doneButton = (Button) Executions.getCurrent().getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
        
        targetList.addEventListener(Events.ON_DROP, dropListener);
        
        selectedColumns = new HashSet<Attribute>();
        
        this.getSelf().addEventListener("onDrawChart", drawTable);

        Map<String, List<Field>> fieldMap = new LinkedHashMap<String, List<Field>>();
        
        if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)) {
            ApiChartConfiguration configuration = (ApiChartConfiguration) Executions.getCurrent().getAttribute(Constants.CIRCUIT_CONFIG);
            fieldMap.put(configuration.getDatasetName(), configuration.getFields());
        } else {
        		List<Field> fields;
        		 QuerySchema querySchema = null;
                for (String fileName : tableData.getFiles()) {
                    fields = new ArrayList<Field>();
                    
                    if(!tableData.getIsQuery()){
                        fields.addAll(hpccService.getColumns(fileName, tableData.getHpccConnection()));
                    }else{//Roxie Query - fetching fields/columns of Roxie queries
                        
                        // Displaying Input parameter Form
                        filterPanel.setVisible(false);
                        inputPanel.setVisible(true);
                      
                        querySchema = hpccQueryService.getQuerySchema(fileName, tableData.getHpccConnection(),
                        		tableData.isGenericQuery(), tableData.getInputParamQuery());
                        fields.addAll(querySchema.getFields());
                        
                        // Constructing Roxie query input parameters
                        constructInputParameters(querySchema);
                    }
                    fieldMap.put(fileName, fields);
                }
        	
        }
        if(fieldMap != null && !fieldMap.isEmpty()){
        	//Setting fields to ChartData
        	tableData.setFields(fieldMap);
        }
            
        if(Constants.CIRCUIT_APPLICATION_ID.equals(authenticationService.getUserCredential().getApplicationId())) {
            try {
                Set<Field> schemaSet = hpccService.getColumns(tableData.getFiles().get(0), tableData.getHpccConnection());
                for (Attribute column : tableData.getAttributes()) {
                    boolean columnExist = false;
                    for(Field field : schemaSet){
                        if(column.getColumn().equals(field.getColumnName().trim())){
                            columnExist =true;
                            break;
                        }
                    }
                    if(!columnExist){
                        throw new Exception("Column doesn't exist");
                    }
                }                
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
            
        }
        
        
        if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
            for(Attribute attribute : tableData.getAttributes()){
                selectedColumns.add(attribute);
                targetList.appendChild(createTargetListItem(attribute));
            }
            
            //TODO: Add else part
            if(tableData.getAttributes() != null && !tableData.getAttributes().isEmpty()) {
                Clients.showBusy(tableHolder,"Retrieving data");
                Events.echoEvent(new Event("onDrawChart", EditTableController.this.getSelf()));
            }
        }  
        
        //Change indicator enabling
        changeIndicatorCheckbox.setChecked(tableData.getEnableChangeIndicators());
        
        //Generating Source List
        UiGenerator.generateTabboxChildren(tableData, sourceTabBox);
        
        // Create Measures and Attributes
       // UiGenerator.generateTabboxChildren(measureTabbox, attributeTabbox, tableData.getFields(), false);
        
        //Creating Filter rows
        if(tableData.getIsFiltered()) {
            for (Filter filter : tableData.getFilters()) {
                if(!filter.getIsCommonFilter()){
                    createFilterListItem(filter);
                }
            }
        }
    }
    
    EventListener<Event> drawTable = new EventListener<Event>() {

        @Override
        public void onEvent(Event arg0) throws Exception {
            try {
                Map<String, List<Attribute>> tableDataMap = null;
                tableDataMap = hpccService.fetchTableData(tableData);
                tableData.setHpccTableData(tableDataMap);
                
                tableHolder.getChildren().clear();
                tableHolder.appendChild(tableRenderer.constructTableWidget(portlet, tableData, true));
            } catch (Exception e) {
                Clients.showNotification(Labels.getLabel("tableCreationFailed"), "error", tableHolder, "middle_center", 3000, true);
                LOG.error(Constants.EXCEPTION, e);
                return;
            }
            Clients.clearBusy(tableHolder);
            doneButton.setDisabled(false);
        }
        
    };
    
    
    EventListener<DropEvent> dropListener = new EventListener<DropEvent>() {
        
        @Override
        public void onEvent(DropEvent event) throws Exception {
            Listitem draggedItem = (Listitem) event.getDragged();
            
            if("targetList".equals(draggedItem.getParent().getId())){
                //Dragged Within Target List
                if(event.getTarget() instanceof Listbox) {
                    //Dropped on List box
                    targetList.appendChild(draggedItem);
                } else {
                    //Dropped on List item
                    targetList.insertBefore(draggedItem, event.getTarget());
                }
            } else {
                //Dragged from Source List
                Attribute attribute = new Attribute(draggedItem.getLabel());
                Tabpanel parentTabpanel = (Tabpanel)draggedItem.getParent().getParent();
                attribute.setFileName(parentTabpanel.getLinkedTab().getLabel());
                if(selectedColumns.add(attribute)) {
                    if(event.getTarget() instanceof Listbox) {
                        //Dropped on Listbox
                        targetList.appendChild(createTargetListItem(attribute));
                    } else {
                        //Dropped on Listitem
                        targetList.insertBefore(createTargetListItem(attribute), event.getTarget());
                    }
                } else {
                	 Clients.showNotification("Column exists", "error", targetList, "middle_center", 3000, true);
                }
            }
            
            //Only for API
            if (Constants.CIRCUIT_APPLICATION_ID.equals(authenticationService.getUserCredential().getApplicationId())) {
                //Enabling Done button
                if(targetList.getChildren().size() > 1) {
                    doneButton.setDisabled(false);
                } else {
                    doneButton.setDisabled(true);
                }
                
                // Code to update the selected columns since the draw table is not  applicaple for circuit config flow
                List<Attribute> selectedTableColumns = tableData.getAttributes();
                Listitem listitem;
                selectedTableColumns.clear();
                for (Component component : targetList.getChildren()) {
                    if (component instanceof Listitem) {
                        listitem = (Listitem) component;
                        Attribute selectedAttribute = (Attribute) listitem.getAttribute(Constants.ATTRIBUTE);
                        selectedTableColumns.add(selectedAttribute);
                    }
                }
            }
        }
    };
    
    /**
     * Creates New List Item for target list
     * @param attribute
     * @return
     */
    private Listitem createTargetListItem(Attribute attribute) {
        Listitem listitem = new Listitem();
        listitem.setDraggable("true");
        listitem.setDroppable("true");
        
        Listcell listCell = new Listcell();
        Button closeBtn = new Button();
        closeBtn.setSclass("glyphicon glyphicon-remove btn btn-link img-btn");
        closeBtn.setStyle("float:right");
        closeBtn.addEventListener(Events.ON_CLICK, attributeRemoveListener);
        listCell.appendChild(closeBtn);
        listitem.setAttribute(Constants.ATTRIBUTE, attribute);
        
        Textbox textBox = new Textbox();
        textBox.setInplace(true);
        textBox.setStyle("border: none;    color: black; width: 150px;");
        textBox.setValue(attribute.getDisplayName());
       /* if(attribute.getAggregateFunction() == null || Constants.NONE.equals(attribute.getAggregateFunction())){
        	textBox.setValue(attribute.getDisplayName());
        }else{
        	textBox.setValue(attribute.getDisplayName()+ "_" + attribute.getAggregateFunction());
        }*/
        textBox.addEventListener(Events.ON_CHANGE, titleChangeLisnr);
        textBox.setParent(listCell);
        listCell.setParent(listitem);
        
        listitem.addEventListener(Events.ON_DROP, dropListener);
        
        return listitem;
    }
    
    
    // Event Listener for Change of tableColumn title text
    private EventListener<Event> titleChangeLisnr = new EventListener<Event>() {
        @Override
        public void onEvent(final Event event) {
            // Circuit API
            if (Constants.CIRCUIT_APPLICATION_ID.equals(authenticationService.getUserCredential().getApplicationId())) {
                doneButton.setDisabled(false);
            }

            Listitem listitem;
            // To avoid duplicate column name
            Listitem listItem = (Listitem) event.getTarget().getParent().getParent();
            Attribute attribute = (Attribute) listItem.getAttribute(Constants.ATTRIBUTE);
            Textbox textBox = (Textbox) event.getTarget();
            String changedName = textBox.getValue();
            for(Component component : targetList.getChildren()){
                if (component instanceof Listitem) {
                    listitem = (Listitem) component;
                    Attribute currentAttribute = (Attribute) listitem.getAttribute(Constants.ATTRIBUTE);
                    if(currentAttribute.equals(attribute)){
                        attribute.setDisplayName(textBox.getValue());
                    }else if (!currentAttribute.equals(attribute)
                            && !changedName.equals(currentAttribute.getDisplayName())
                            && !changedName.equals(currentAttribute.getColumn())) {
                        attribute.setDisplayName(textBox.getValue());
                    }else{
                        textBox.setFocus(true);
                        Clients.showNotification("Column Name should not be same",
                                "error", targetList, "middle_center", 3000, true);
                    }
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("TableColumn Title is being changed");
            }
        }
    };
    
    
    @Listen("onCheck= #changeIndicatorCheckbox")
    public void onCheckChangeIndicator() {
        tableData.setEnableChangeIndicators(changeIndicatorCheckbox.isChecked());
    }
    
    @Listen("onClick = #drawTable")
    public void drawTable() {
        tableData.getAttributes().clear();
        final Set<String> columnNameSet = new HashSet<String>();
        boolean columNotExists = true;
        if (targetList.getChildren().size() > Constants.ZERO) {
            if (checkDuplicateColumn(columnNameSet, columNotExists)) {
                Clients.showBusy(tableHolder, "Retrieving data");
                Events.echoEvent(new Event("onDrawChart",
                        EditTableController.this.getSelf()));
            } else {
                Clients.showNotification("Column Name should not be same",
                        "error", targetList, "middle_center", 3000, true);
            }
        } else {
            Clients.showNotification(Labels.getLabel("moveSomeColumn"),
                    "error", targetList, "middle_center", 3000, true);
        }

    }
    
    @Listen("onClick=#saveParams")
    public void onSaveParameter(Event event){
        Map<String,String> inputs = new HashMap<String, String>();
        
        for ( Component comp : inputParams.getChildren()) {
            if(comp instanceof InputListitem) {
                InputListitem listitem = (InputListitem) comp;
                inputs.put(listitem.getParamName(), listitem.getInputValue());
            }
        }
        
        InputParam inputparam = null;
        List<InputParam> paramsList = new ArrayList<InputParam>();
        for(Entry<String, String> entry : inputs.entrySet()){
            inputparam = new InputParam(entry.getKey(),entry.getValue());
            paramsList.add(inputparam);
        }
        
        tableData.setInputParams(paramsList);
    }

    private boolean checkDuplicateColumn(final Set<String> columnNameSet,
            final boolean columNotExists) {
        boolean columnExistence = columNotExists;
        Listitem listitem;
        for (final Component component : targetList.getChildren()) {
            if (component instanceof Listitem) {
                listitem = (Listitem) component;
                final Attribute attribute = (Attribute) listitem.getAttribute(Constants.ATTRIBUTE);
                tableData.getAttributes().add(attribute);
                if (attribute.getDisplayName() == null) {
                    columnExistence = columnNameSet.add(attribute.getColumn());
                } else {
                    columnExistence = columnNameSet.add(attribute.getDisplayName());
                }
                if (!columnExistence){
                    break;
                }    
            }
        }
        return columnExistence;
    }
    
    /**
     * Listener to remove the dropped table items from the TargetList
     */
    private EventListener<Event> attributeRemoveListener = new EventListener<Event>() {
        @Override
        public void onEvent(final Event event) {
            Listitem tableItem = (Listitem) event.getTarget().getParent().getParent();
            Attribute attribute = (Attribute) tableItem.getAttribute(Constants.ATTRIBUTE);
            
            
            tableItem.detach();
            
            selectedColumns.remove(attribute);
            tableData.getAttributes().remove(attribute);
            
            tableHolder.getChildren().clear();
        }
    };
    
    private void createFilterListItem(Filter filter) {
        Listitem filterListitem = new Listitem();
        filterListitem.setAttribute(Constants.FILTER, filter);
        Listcell labelCell = new Listcell(filter.getColumn());
        Button playBtn = new Button();
        playBtn.setSclass("glyphicon glyphicon-play btn btn-link img-btn");
        playBtn.setStyle("float:right");
        Popup popup = new Popup();
        popup.setZclass("popup");
        popup.setId(filter.getColumn() + "_filterPopup");
        Include include = new Include();
        include.setDynamicProperty(Constants.PARENT, this.getSelf());
        include.setDynamicProperty(Constants.BUSY_COMPONENT, tableHolder);
        include.setDynamicProperty(Constants.FILTER, filter);
        include.setDynamicProperty(Constants.CHART_DATA, tableData);
        include.setDynamicProperty(Constants.EDIT_WINDOW_DONE_BUTTON, doneButton);

        if (Constants.DATA_TYPE_NUMERIC.equals(filter.getType())) {
            include.setSrc("layout/numeric_filter_popup.zul");
        } else {
            include.setSrc("layout/string_filter_popup.zul");
        }

        labelCell.appendChild(popup);
        popup.appendChild(include);
        playBtn.setPopup(filter.getColumn() + "_filterPopup, position=end_center");

        Button closeBtn = new Button();
        closeBtn.setSclass("glyphicon glyphicon-remove btn btn-link img-btn");
        closeBtn.setStyle("float:right");
        closeBtn.addEventListener(Events.ON_CLICK, filterClearListener);
        labelCell.appendChild(closeBtn);

        labelCell.appendChild(playBtn);
        labelCell.setTooltiptext(filter.getColumn());
        filterListitem.appendChild(labelCell);

        filterList.appendChild(filterListitem);
    }
    
    EventListener<Event> filterClearListener = new EventListener<Event>() {
        public void onEvent(final Event event) throws Exception {    
            Listitem listItem =(Listitem) event.getTarget().getParent().getParent();            
            tableData.getFilters().remove(listItem.getAttribute(Constants.FILTER));
            if(tableData.getFilters().isEmpty()){
                tableData.setIsFiltered(false);
            }
            try {
                Clients.showBusy(tableHolder,"Retrieving data");
                Events.echoEvent(new Event("onDrawChart", EditTableController.this.getSelf()));
            } catch (Exception ex) {
                Clients.showNotification(Labels.getLabel("unableToFetchHpccData"), "error",    EditTableController.this.getSelf(), "middle_center", 3000, true);
                LOG.error(Constants.EXCEPTION, ex);
            }
            listItem.detach();
        }
    };
    
    @Listen("onDrop = #filterList") 
    public void onDropToFilterItem(final DropEvent dropEvent) {
        final Listitem draggedListitem = (Listitem) dropEvent.getDragged();
        if("targetList".equals(draggedListitem.getParent().getId())){
            //TODO: Notify
            return;
        }
        
        Filter filter = new Filter();
        Field field = (Field) draggedListitem.getAttribute(Constants.FIELD);
        filter.setColumn(field.getColumnName());
        if(DashboardUtil.checkNumeric(field.getDataType())) {
            filter.setType(Constants.DATA_TYPE_NUMERIC);
        } else {
            filter.setType(Constants.DATA_TYPE_STRING);
        }
        
        Tabpanel tabpanel = (Tabpanel) draggedListitem.getParent().getParent();
        filter.setFileName(tabpanel.getLinkedTab().getLabel());
        
        if(tableData.getFilters().contains(filter)) {
            Clients.showNotification(Labels.getLabel("columnAlreadyAdded"), "error", filterList, "end_center", 3000, true);
            return;
        }
        createFilterListItem(filter);
    }

    
    /**Gets input parameters for Roxie query
     * @throws Exception
     */
    private void constructInputParameters(QuerySchema querySchema) throws Exception {
        
   	 Map<String,Set<String>> paramValues = querySchema.getInputParams();
   	 
        if(tableData.getInputParams() == null) {        	
            
            InputParam inputParam = null;
            List<InputParam> paramsList = new ArrayList<InputParam>();
            for(Entry<String, Set<String>> entry : paramValues.entrySet()){
            	 InputListitem listitem = new InputListitem(entry.getKey(), entry.getValue(), String.valueOf(portlet.getId()));
                 inputParams.appendChild(listitem);
                 inputParam = new InputParam(entry.getKey());
                 paramsList.add(inputParam);
            }
            
            tableData.setInputParams(paramsList);
            
        } else {//Retrieving from DB
            
            paramValues.entrySet().forEach(entry ->{
                InputListitem listitem = new InputListitem(
                        entry.getKey(), entry.getValue(), String.valueOf(portlet.getId()));
                inputParams.appendChild(listitem);
                try{
                 InputParam appliedInput = tableData .getInputParams() .stream()
                                .filter(input -> input.getName().equals(
                                        entry.getKey())).findAny().get();
                 if (appliedInput != null && appliedInput.getValue() != null) {
                     listitem.setInputValue(appliedInput.getValue());
                 }
                }catch(NoSuchElementException e){
                    //Need not to log.This occurs when an inputparam from Hpcc is not found in portlet's
                    //applied inputparam
                }
                        
                
            });
            
        }
        
    }
}
