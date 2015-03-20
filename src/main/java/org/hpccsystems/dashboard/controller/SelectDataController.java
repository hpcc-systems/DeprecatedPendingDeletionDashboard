package org.hpccsystems.dashboard.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.entity.HpccConnection;
import org.hpccsystems.dashboard.chart.entity.HpccConnections;
import org.hpccsystems.dashboard.chart.entity.InputParam;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.FileMeta;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.util.EncryptDecrypt;
import org.hpccsystems.dashboard.util.FileListTreeModel;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treecol;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SelectDataController extends SelectorComposer<Component>{

    private static final long serialVersionUID = 1L;
    private static final  Log LOG = LogFactory.getLog(SelectDataController.class);
    private static final String CONFIG_FILE = "hpcc_config.xml";
    @WireVariable
    private HPCCService hpccService;
    @WireVariable
    private ChartService chartService;
    @WireVariable
    private HPCCQueryService hpccQueryService;

    @Wire
    private Textbox username;
    @Wire
    private Textbox password;
    @Wire
    private Checkbox sslCheckbox;
    @Wire
    private Intbox wssqlport;
    @Wire
    private Intbox espport;
    @Wire
    private Intbox wsEclPort;
    @Wire
    private Combobox fileType;
    @Wire
    private Panel formPanel;
    @Wire
    private Tree tree;
    @Wire
    private Listbox selectedFilesListbox;
    @Wire
    private Button visualizeBtn;
    @Wire
    private Vbox viewHpccVbox;
    @Wire
    private Vbox editHpccVbox;
    @Wire
    private Label hpccUrl;
    @Wire
    private Combobox clusters;    
    @Wire
    private Button submitBtn;
    @Wire
    private Treecol treeColumn;    
    @Wire
    private Textbox clusterIp;
    @Wire
    private Hbox clusterNameHbox;
    @Wire
    private Button getClustersBtn;
    @Wire
    private Div roxieportLabel;      
    @Wire
    private Vbox defaultsContainer;
    @Wire
    private Combobox defaultConnections;
    @Wire
    private Button proceedBtn;
    
    private ChartData chartData;
    private Window parentWindow;
    private HpccConnection hpccConnection;
    private Portlet portlet;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        chartData = (ChartData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
        parentWindow = (Window) Executions.getCurrent().getAttribute(Constants.PARENT);
        hpccConnection = (HpccConnection) Sessions.getCurrent().getAttribute(Constants.HPCC_CONNECTION);
        portlet = (Portlet)Executions.getCurrent().getAttribute(Constants.PORTLET);
        
        //Hides Hpcc data select page,Because common filters enabled
        if (hpccConnection != null) { 
            if(Constants.LOGICAL_FILE.equals(hpccConnection.getDatasource())){
                proceedBtn.setLabel(Labels.getLabel("fetchFields"));
                chartData.setIsQuery(false);
            }else if(Constants.QUERY.equals(hpccConnection.getDatasource())){
                proceedBtn.setLabel(Labels.getLabel("fetchQueries"));
                chartData.setIsQuery(true);
            }
                    
            editHpccVbox.setVisible(false);
            viewHpccVbox.setVisible(true);
            defaultsContainer.setVisible(false);
            
            StringBuilder builder = new StringBuilder();
            builder.append(hpccConnection.getHostIp());
            hpccUrl.setValue(builder.toString());
        } else {
            InputStream is = SelectDataController.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            JAXBContext jaxbContext = JAXBContext.newInstance(HpccConnections.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            HpccConnections connections = (HpccConnections) jaxbUnmarshaller.unmarshal(is);
            
            List<HpccConnection> defaulHpccConnections = connections.getHpccConnections();
            EncryptDecrypt decrypter = new EncryptDecrypt("");
            defaulHpccConnections.stream().forEach(connection -> {
                try {
                    connection.setPassword(decrypter.decrypt(connection.getPassword()));
                } catch (Exception e) {
                    LOG.debug(Constants.EXCEPTION + e);
                }
            });
            
            if(LOG.isDebugEnabled()) {
                LOG.debug("Default HPCC Connections - " + defaulHpccConnections);
            }
            
            if(!defaulHpccConnections.isEmpty()) {
                defaultsContainer.setVisible(true);
                editHpccVbox.setVisible(false);
                Comboitem comboitem;
                for (HpccConnection defaultHpccConnection : defaulHpccConnections) {
                    comboitem = new Comboitem(defaultHpccConnection.getName());
                    if(defaultHpccConnection.getName() == null) {
                        comboitem.setLabel(defaultHpccConnection.getHostIp());
                    }
                    //if datasource not defined default to logical file
                    if(defaultHpccConnection.getDatasource() == null) {
                    	defaultHpccConnection.setDatasource(Constants.LOGICAL_FILE);
                    }
                    
                    comboitem.setValue(defaultHpccConnection);
                    defaultConnections.appendChild(comboitem);
                }
            } else {
                defaultsContainer.setVisible(false);
                editHpccVbox.setVisible(true);
            }
            
          //selecting 'logical file' as default
            fileType.setSelectedIndex(0);
            //If chartData is null, chart category is not added in EditWidgetController
            chartData.setIsQuery(false);
        }      
        
                
        if(LOG.isDebugEnabled()) {
            LOG.debug("Checkbox" + sslCheckbox);
        }
        
    }

    @Listen("onClick = #proceedBtn")
    public void onProcced(Event event) {
        LOG.debug("data source -->"+hpccConnection.getDatasource());
        chartData.setHpccConnection(hpccConnection);
        if(Constants.LOGICAL_FILE.equals(hpccConnection.getDatasource())){
            if(constructFileBrowser(hpccConnection)) {
                Button btn = (Button) event.getTarget();
                btn.setDisabled(true);
            }
        }else if(Constants.QUERY.equals(hpccConnection.getDatasource())){
            if(constructQueryBrowser(hpccConnection)) {
                Button btn = (Button) event.getTarget();
                btn.setDisabled(true);
            }
        }
    }
    
    @Listen("onClick = #submitBtn")
    public void onFormSubmit(Event event) {
        
        boolean isValidData = validateHpccData(true);
        if(isValidData){
            updateHpccConnectionObject();
            if(chartData.getIsQuery()) {
                
                treeColumn.setLabel("Choose a query to Visualize");
                if(constructQueryBrowser(hpccConnection)) {
                    Button btn = (Button) event.getTarget();
                    btn.setDisabled(true);
                }
            } else {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Hpcc Connection - " + hpccConnection);
                }
                
                if(constructFileBrowser(hpccConnection)) {
                    Button btn = (Button) event.getTarget();
                    btn.setDisabled(true);
                }
            }
            
            chartData.setHpccConnection(hpccConnection);
        }
        
    }
    
    @Listen("onSelect = #defaultConnections")
    public void onHpccConnectionSelect(SelectEvent<Component, Object> event) {
        Comboitem comboitem = (Comboitem) event.getSelectedItems().iterator().next();
        HpccConnection selectedConnection = comboitem.getValue();
        clusterIp.setValue(selectedConnection.getHostIp());
        espport.setValue(selectedConnection.getEspPort());
        wssqlport.setValue(selectedConnection.getWssqlPort());
        wsEclPort.setValue(selectedConnection.getWsEclPort());
        username.setValue(selectedConnection.getUsername());
        password.setValue(selectedConnection.getPassword());
        sslCheckbox.setChecked(selectedConnection.getIsSSL());
        for(Component comp: fileType.getChildren())
        {
        	comboitem=(Comboitem)comp;
        	if(comboitem.getValue().equals(selectedConnection.getDatasource()))
        	{
        		fileType.setSelectedIndex(comboitem.getIndex());
        		showHideQueryUIElements(selectedConnection.getDatasource());
        		break;
        	}
        }
        editHpccVbox.setVisible(true);
        
        getClusterList();
    }
    
    
    @Listen("onClick = #defineConnection")
    public void onCreateNewConnection() {
        clusterIp.setValue(null);
        espport.setValue(null);
        wssqlport.setValue(null);
        wsEclPort.setValue(null);
        username.setValue(null);
        password.setValue(null);    
        sslCheckbox.setChecked(false);
        
        editHpccVbox.setVisible(true);
        
        if(Constants.QUERY.equals(fileType.getSelectedItem().getValue())){
        	showHideQueryUIElements(Constants.QUERY);
		}
        
        defaultConnections.setSelectedItem(null);
    }
    
    private void updateHpccConnectionObject() {
        if(hpccConnection == null){
            hpccConnection = new HpccConnection();
        }
        hpccConnection.setUsername(username.getValue());
        hpccConnection.setPassword(password.getValue());
        hpccConnection.setHostIp(clusterIp.getValue());
        hpccConnection.setIsSSL(sslCheckbox.isChecked());
        hpccConnection.setWssqlPort(wssqlport.getValue());
        hpccConnection.setEspPort(espport.getValue());
        hpccConnection.setWsEclPort(wsEclPort.getValue());
        
        
        if(clusters.getSelectedItem() != null) {
            hpccConnection.setClusterType(String.valueOf(clusters.getSelectedItem().getValue()));
        }
        
        chartData.setHpccConnection(hpccConnection);
    }
    
    /**Constructs the Roxie query tree to select files
     * @param hpccConnection
     * @return boolean
     */
    private boolean constructQueryBrowser(HpccConnection hpccConnection) {

        FileMeta fileMeta = new FileMeta();
        fileMeta.setScope("");
        fileMeta.setFileName("ROOT");
        fileMeta.setIsDirectory(true);
        try {
            fileMeta.setChildlist(hpccService.getQueries(chartData.getHpccConnection(),
            		chartService.getCharts().get(portlet.getChartType()).getCategory()));
        } catch (Exception e) {
            Clients.showNotification(Labels.getLabel("plzCheckProvidedHPCCCrendentials"), "error", username.getParent().getParent(), "after_center", 3000, true);
            LOG.error(Constants.EXCEPTION, e);
            return false;
        }
        FileListTreeModel fileListTreeModel = new FileListTreeModel(fileMeta, chartData);
        tree.setModel(fileListTreeModel);
        tree.setVisible(true);
        
        tree.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<Component, Object>>() {

            @Override
            public void onEvent(SelectEvent<Component, Object> event)throws Exception {
                Tree targetTree = (Tree) event.getTarget();
                if(targetTree.getSelectedItems().size() > 1){
                	 //Enabling multiple selection  of query only for tree/any Hierarchy chart
                    if(Constants.CATEGORY_HIERARCHY != chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                    	 Clients.showNotification(Labels.getLabel("chooseOneQuery"),"error", tree, "middle_center", 3000, true);
                         Treeitem item = (Treeitem)event.getSelectedItems().iterator().next();
                         item.setSelected(false);
                         return;
                    }
                }
                selectedFilesListbox.getChildren().clear();
                
                addSelectedItem(targetTree);                        
            }
        });
        
        formPanel.setOpen(false);
        
        return true;
    
    }

    @Listen("onSelect = #fileType")
    public void onSelectFileType(SelectEvent<Component, Object> event){        
        Comboitem selecteditem = (Comboitem)event.getSelectedItems().iterator().next();    
        String fileTypeSelected = String.valueOf(selecteditem.getValue());
        showHideQueryUIElements(fileTypeSelected);
    }
    
    private void showHideQueryUIElements(String dataSource) {
    	if(Constants.LOGICAL_FILE.equals(dataSource)) {
            chartData.setIsQuery(false);
            submitBtn.setLabel(Labels.getLabel("fetchFields"));
            
            clusterNameHbox.setVisible(false);
           // inputParamHbox.setVisible(false);
            if(hpccConnection != null) {
                hpccConnection.setClusterType(null);
            }
            
            roxieportLabel.setVisible(false);
            wsEclPort.setVisible(false);
            
            submitBtn.setDisabled(false);
        }else if(Constants.QUERY.equals(dataSource)){
            chartData.setIsQuery(true);
            submitBtn.setLabel(Labels.getLabel("fetchQueries"));
            
            roxieportLabel.setVisible(true);
            wsEclPort.setVisible(true);
            if(hpccConnection != null) {
                hpccConnection.setWsEclPort(null);
            }
            clusterNameHbox.setVisible(true);
            //inputParamHbox.setVisible(true);
            getClustersBtn.setVisible(true);
            clusters.setVisible(false);
            submitBtn.setDisabled(true);
            
        }
    }
    
    @Listen("onClick = #getClustersBtn")
    public void onGetClusters(Event event) {
    	 if(validateHpccData(false)) {
    		getClusterList();
    	 }
    } 
    
   public void getClusterList() {
       
        try {
            updateHpccConnectionObject();
            
            List<String> clusterNames = hpccService.getClusters(hpccConnection);
            
            //Clearing existing clusters
            clusters.getChildren().clear();
            
            Comboitem comboitem;
            for (String clusterName : clusterNames) {
                comboitem = new Comboitem(clusterName);
                comboitem.setValue(clusterName);
                clusters.appendChild(comboitem);
            }
            getClustersBtn.setVisible(false);
            clusters.setVisible(true);
            
            submitBtn.setDisabled(false);
        } catch (HpccConnectionException e) {
            Clients.showNotification(Labels.getLabel("plzCheckProvidedHPCCCrendentials"), "error", username.getParent().getParent(), "after_center", 3000, true);
            LOG.error(Constants.EXCEPTION, e);
        }
    }
    
    /**
     * Validates Hpcc connection data
     * @return boolean
     */
    private boolean validateHpccData(boolean isSubmit) {
         if(clusterIp.getValue() == null || clusterIp.getValue().trim().length() ==  0){
                Clients.showNotification(Labels.getLabel("emptyIP"),"error", clusterIp, "end_center", 3000, true);
                return false;
        } else if(espport.getValue() == null || espport.getValue() < 1){
            Clients.showNotification(Labels.getLabel("emptyESPPort"),"error", espport, "end_center", 3000, true);
            return false;
        } else if(wssqlport.getValue() == null || wssqlport.getValue() < 1){
            Clients.showNotification(Labels.getLabel("emptyWSSQLPort"),"error", wssqlport, "end_center", 3000, true);
            return false;
        } else if(chartData.getIsQuery() && (wsEclPort.getValue() == null || wsEclPort.getValue() < 1)){
            Clients.showNotification(Labels.getLabel("emptyRoxiePort"),"error", wsEclPort, "end_center", 3000, true);
            return false;
        }
         
        if(isSubmit && chartData.getIsQuery() && (clusters.getSelectedItem() == null)) {
            Clients.showNotification(Labels.getLabel("emptyClusterName"),"error", clusters, "end_center", 3000, true);
            return false;
        }
        return true;
    }
    
    /**Adds the selected file or query to selectedFileslistbox
     * @param targetTree
     */
    private void addSelectedItem(Tree targetTree){
        Listitem listitem;
        for (Treeitem treeitem : targetTree.getSelectedItems()) {
            if(treeitem.getLastChild() instanceof Treerow) {
                Treerow treerow = (Treerow) treeitem.getLastChild();
                Treecell treecell = (Treecell) treerow.getLastChild();
                Label label = (Label) treecell.getLastChild();
                
                listitem = new Listitem(label.getValue());
                selectedFilesListbox.appendChild(listitem);
            } else {
                if(treeitem.isOpen()) {
                    treeitem.setOpen(false);
                } else {
                    treeitem.setOpen(true);
                }
                treeitem.setSelected(false);
            }
        }
    }

    /**
     * Constructs the file browser tree to select files
     * @param hpccConnection
     * @return
     *     Success or Failure as boolean
     */
    private boolean constructFileBrowser(HpccConnection hpccConnection) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setScope("");
        fileMeta.setFileName("ROOT");
        fileMeta.setIsDirectory(true);
        try {
            fileMeta.setChildlist(hpccService.getFileList(fileMeta.getScope(), chartData.getHpccConnection()));
        } catch (Exception e) {
            Clients.showNotification(Labels.getLabel("plzCheckProvidedHPCCCrendentials"), "error", username.getParent().getParent(), "after_center", 3000, true);
            LOG.error(Constants.EXCEPTION, e);
            return false;
        }
        FileListTreeModel fileListTreeModel = new FileListTreeModel(fileMeta, chartData);
        tree.setModel(fileListTreeModel);
        tree.setVisible(true);
        
        tree.addEventListener(Events.ON_SELECT, new EventListener<Event>() {

            public void onEvent(Event event) {
                Tree targetTree = (Tree) event.getTarget();
                
                selectedFilesListbox.getChildren().clear();
                
                addSelectedItem(targetTree);
                
                if(selectedFilesListbox.getChildren().size() > 1) {
                    visualizeBtn.setLabel(Labels.getLabel("defineRelations"));
                } else {
                    visualizeBtn.setLabel(Labels.getLabel("visualize"));
                }
            }
        });
        
        formPanel.setOpen(false);
        
        return true;
    }

    @Listen("onClick = #visualizeBtn")
    public void onVisualizeButtonClick(Event event) {
        if(!selectedFilesListbox.getChildren().isEmpty()) {
            List<String> files = new ArrayList<String>();
            Listitem listitem;
            for (Component component : selectedFilesListbox.getChildren()) {
                listitem = (Listitem) component;
                files.add(listitem.getLabel());
            }
            chartData.setFiles(files);
            //fetching file columns
            List<Field> fields;
            QuerySchema querySchema = null;
            Map<String, List<Field>> fieldMap = new LinkedHashMap<String, List<Field>>();
            for (String fileName : chartData.getFiles()) {
                fields = new ArrayList<Field>();
                try {                	
	                if(!chartData.getIsQuery()){
						fields.addAll(hpccService.getColumns(fileName, chartData.getHpccConnection()));						
	                } else {
	                	LOG.debug("FileName: "+fileName);
	                	LOG.debug("chartData.isGenericQuery(): "+chartData.isGenericQuery());
	                	LOG.debug("chartData.getInputParamQuery(): "+chartData.getInputParamQuery());
	                	
	                	if(Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()){
	                	    querySchema = hpccQueryService.getQuerySchema(fileName, chartData.getHpccConnection(),
                                    chartData.isGenericQuery(), 
                                    chartData.getInputParamQuery());
                            fields.addAll(querySchema.getFields());
	                	}
	                }
                } catch (Exception e) {
                	LOG.error(Constants.EXCEPTION, e);
                	Clients.showNotification(Labels.getLabel("unableToFetchColumns"), Clients.NOTIFICATION_TYPE_ERROR, tree, "middle_center", 2000, false);
                	return;
				}
                
                fieldMap.put(fileName, fields);
            }
            
            //Setting fields to ChartData
            chartData.setFields(fieldMap);
            if(!chartData.getIsQuery()){
               if (Sessions.getCurrent().getAttribute(Constants.COMMON_FILTERS) != null) {
                    // Setting common filters for Newly created chart
                    @SuppressWarnings("unchecked")
                    Set<Filter> filterSet = (Set<Filter>) Sessions.getCurrent().getAttribute(Constants.COMMON_FILTERS);
                    for (Filter filter : filterSet) {
                        if( (Constants.DATA_TYPE_STRING.equals(filter.getType()) && (filter.getValues() != null)) ||
                                (Constants.DATA_TYPE_NUMERIC.equals(filter.getType()) && filter.getStartValue() != null && filter.getEndValue() != null) ) {
                            for (Map.Entry<String, List<Field>> entry : chartData.getFields().entrySet()) {
                                if(filter.getFileName().equals(entry.getKey())) {
                                    for (Field field : entry.getValue()) {
                                        if(filter.getColumn().equals(field.getColumnName())) {
                                            chartData.setIsFiltered(true);
                                            chartData.getFilters().add(filter);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                if (Sessions.getCurrent().getAttribute(Constants.COMMON_FILTERS) != null) {
                    // Setting common filters for Newly created chart
                    @SuppressWarnings("unchecked")
                    Set<InputParam> inputParamrSet = (Set<InputParam>) Sessions.getCurrent().getAttribute(Constants.COMMON_FILTERS);
                   Set<String> inputsName =  querySchema.getInputParams().keySet();
                   if(chartData.getInputParams() == null){
                       chartData.setInputParams(new ArrayList<InputParam>());
                   }
                    for (InputParam inpuparam : inputParamrSet) {
                        if(inputsName.contains(inpuparam.getName())){
                            chartData.getInputParams().add(inpuparam);
                        }
                    }
                }
            }
            Events.sendEvent("onIncludeDetach", parentWindow, Constants.EDIT_WINDOW_TYPE_DATA_SELECTION);
        } else {
            Clients.showNotification(Labels.getLabel("plzChooseaFile"), "warning", tree, "middle_center", 2000, false);
        }
    }
    
}
