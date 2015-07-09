package org.hpccsystems.dashboard.controller; 

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.api.entity.ApiChartConfiguration;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.tree.entity.Level;
import org.hpccsystems.dashboard.chart.tree.entity.LevelElement;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.ImageGridPopup;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.hpccsystems.dashboard.util.UiGenerator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
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
import org.zkoss.zul.Anchorchildren;
import org.zkoss.zul.Anchorlayout;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;

/**
 * Class to process with tree layout
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditTreeController extends SelectorComposer<Component>{

    private static final long serialVersionUID = -8870098984402357898L;
    private static final Log LOG = LogFactory.getLog(EditTreeController.class);
    
    @Wire
    private Tabbox filesTabbox;
    
    @Wire
    private Vbox levelsContainer;    
    
    @Wire
    private Button addLevel;
    
    @Wire
    private Label rootLabel;
    @Wire
    private Combobox rootCombobox;
    
    @Wire
    private Include treeFilterHolder;
    
    @Wire
    private Div chart;
    
    private TreeData treeData;
    private Button doneButton;
    private Portlet portlet;

    @WireVariable
    private AuthenticationService authenticationService;
    @WireVariable
    private HPCCService hpccService;
    @WireVariable
    private ChartRenderer chartRenderer;
    @WireVariable
    private HPCCQueryService hpccQueryService;
    
    private List<Level> levels;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        LOG.debug("Inside EditTreeController -->");

        Execution execution = Executions.getCurrent();
        Map<String, List<Field>> fieldMap = null;
        treeData = (TreeData) execution.getAttribute(Constants.CHART_DATA);
        portlet = (Portlet) execution.getAttribute(Constants.PORTLET);
        doneButton = (Button) execution.getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
        
        this.getSelf().addEventListener("onDrawChart", drawChartListener);
        this.getSelf().addEventListener("onPopulateList", populateCombobox);
        
        // API chart config flow without chart
        if (authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)) {
            ApiChartConfiguration configuration = (ApiChartConfiguration) execution.getAttribute(Constants.CIRCUIT_CONFIG);
            fieldMap = new LinkedHashMap<String, List<Field>>();
            List<Field> fields = new ArrayList<Field>();
            fields.addAll(configuration.getFields());
            fieldMap.put(configuration.getDatasetName(), fields);
        } else {
        	if( treeData.getFields() == null){
        		 try {
                     fieldMap = new LinkedHashMap<String, List<Field>>();
                     List<Field> fields;
                     QuerySchema querySchema = null;
                     for (String fileName : treeData.getFiles()) {
                         fields = new ArrayList<Field>();
                         if(!treeData.getIsQuery()){
                         	fields.addAll(hpccService.getColumns(fileName,treeData.getHpccConnection()));
                         }else{
                        	 querySchema = hpccQueryService.getQuerySchema(fileName, treeData.getHpccConnection(),
                        			 treeData.isGenericQuery(),treeData.getInputParamQuery());
                         	fields.addAll(querySchema.getFields());
                         }
                         fieldMap.put(fileName, fields);
                     }
                 } catch (Exception e) {
                     Clients.showNotification(
                             Labels.getLabel("unableToFetchColumns"), "error", comp,"middle_center", 3000, true);
                     LOG.error(Constants.EXCEPTION, e);
                     return;
                 }        		
        	}
           
        }
        if(fieldMap != null && !fieldMap.isEmpty()){
        	//Setting fields to ChartData
        	treeData.setFields(fieldMap);
        }
        UiGenerator.generateTabboxChildren(treeData, filesTabbox);
        
        levels = new ArrayList<Level>();
        if(treeData.getLevels() != null && treeData.getLevels().size() > 1) {
            //Live chart present
            for (Level level : treeData.getLevels()) {
                levels.add(level);
                appendLevelItem(level);
            }
            
            Clients.showBusy(chart, "Retriving data");
            Events.echoEvent(new Event("onPopulateList", this.getSelf()));
            
            Clients.showBusy(chart, "Retriving data");
            Events.echoEvent(new Event("onDrawChart", this.getSelf()));
        } else {
            //New Chart
            //Creating default Two Levels
            Level level = new Level();
            levels.add(level);
            appendLevelItem(level);
            level = new Level();
            levels.add(level);
            appendLevelItem(level);
        }
        
      //Setting params for filter include
        treeFilterHolder.setDynamicProperty(Constants.BUSY_COMPONENT, chart);
        treeFilterHolder.setDynamicProperty(Constants.PARENT, this.getSelf());
        if (treeData.getIsQuery()) {
            treeFilterHolder.setSrc("layout/input_parameters.zul");
            Events.sendEvent(Constants.CREATE_PARAM_EVENT, treeFilterHolder, null);
        } /*else {
            treeFilterHolder.setSrc("layout/filter.zul");
        }*/
        
        
    }
    
    EventListener<DropEvent> dropListener = new EventListener<DropEvent>() {
        
        @Override
        public void onEvent(DropEvent event) throws Exception {
            Listitem listitem = (Listitem) event.getDragged();
            Anchorlayout anchorlayout = (Anchorlayout) event.getTarget();
            
            Level associatedLevel = (Level) anchorlayout.getParent().getParent().getParent().getAttribute(Constants.LEVEL);
            Field field = (Field) listitem.getAttribute(Constants.FIELD);
            LevelElement element = new LevelElement(listitem.getLabel());
            Tabpanel tabpanel = (Tabpanel) listitem.getParent().getParent();
            element.setFileName(tabpanel.getLinkedTab().getLabel());
            element.setIsColumn(true);
            if(DashboardUtil.checkNumeric(field.getDataType())) {
                element.setDataType(Constants.DATA_TYPE_NUMERIC);
            } else {
                element.setDataType(Constants.DATA_TYPE_STRING);
            }
            
            validateLevelElement(element);
            
            if(associatedLevel.getElements() == null) {
                associatedLevel.setElements(new ArrayList<LevelElement>());
            }
            associatedLevel.getElements().add(element);
            
            Anchorchildren anchorchildren = (Anchorchildren) anchorlayout.getFirstChild();
            anchorchildren.setSclass("");
            anchorchildren.appendChild(createLevelElement(element));
        }

    };


    /**
     * Checking for - Already dropped
     * @param element
     */
    private void validateLevelElement(LevelElement element) {
        for (Level level : levels) {
            if(level.getElements() != null && level.getElements().contains(element)) {
                Clients.showNotification("Element exists", "error", levelsContainer, "end_center", 3000, true);
                return;
            }
        }            
    }
    private Hbox createLevelElement(LevelElement element) {
        Hbox hbox = new Hbox();
        hbox.setAttribute(Constants.LEVEL_ELEMENT, element);
        
        if(element.getIsColumn()) {
            hbox.setSclass("levelColumn");
        } else {
            hbox.setSclass("levelConjenction");
        }
        
        hbox.appendChild(new Label(element.getName()));
        hbox.setPack("center");
        Button button = new Button();
        button.setIconSclass("glyphicon glyphicon-remove-circle");
        button.setSclass("btn btn-link img-btn");
        button.addEventListener(Events.ON_CLICK, removeLevelElement);        
        hbox.appendChild(button);
        return hbox;
    }
    
    EventListener<Event> removeLevelElement = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            LevelElement element = (LevelElement) event.getTarget().getParent().getAttribute(Constants.LEVEL_ELEMENT);
            Hbox hbox = (Hbox) event.getTarget().getParent();
            Anchorchildren anchorchildren = (Anchorchildren) hbox.getParent();
            Panel panel = (Panel) anchorchildren.getParent().getParent().getParent().getParent();
            
            Level level= (Level) panel.getAttribute(Constants.LEVEL);
            
            level.getElements().remove(element);
            if(level.getElements().isEmpty()) {
                anchorchildren.setSclass("dropZone");
            }
            
            hbox.detach();
        }
    }; 
   
    
    @Listen("onClick=#addLevel")
    public void onAddLevel() {
        Level level = new Level();
        levels.add(level);
        appendLevelItem(level);
    }
    
    
    private void appendLevelItem(final Level level) {
        int levelCount = levels.size();
        
        Panel panel = new Panel();
        panel.setAttribute(Constants.LEVEL, level);
        panel.setClosable(true);
        panel.setCollapsible(true);
        panel.setSclass("levelPanel");
        panel.setParent(levelsContainer);
        panel.setTitle("Level "+ levelCount);
      
        Caption caption = new Caption();   
        
        final Button picButton =new Button();
        ImageGridPopup imagePopup;
        if(level.getImgSrc() != null)       {
			picButton.setImage(new StringBuilder("/demo/chart/icons/").append(level.getImgSrc()).append(".png").toString());				
			picButton.setHoverImage(new StringBuilder("/demo/chart/icons/").append(level.getImgSrc()).append("_c.png").toString());
			imagePopup = new ImageGridPopup(level.getImgSrc());
        }else{
        	 picButton.setImage("/demo/chart/icons/circle.png");
             picButton.setHoverImage("/demo/chart/icons/circle_c.png");
             imagePopup = new ImageGridPopup();
        }
       
        picButton.setTooltip("Select image for Level");
        picButton.setHeight("16px");
        picButton.setWidth("16px");
        picButton.setParent(caption);
        caption.appendChild(imagePopup);
        picButton.setPopup(imagePopup);
        picButton.setSclass("btn btn-link img-btn");
        panel.appendChild(caption);
        
        caption.addEventListener("onIconChange", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				String iconName = (String)event.getData();
				picButton.setImage(new StringBuilder("/demo/chart/icons/").append(iconName).append(".png").toString());				
				picButton.setHoverImage(new StringBuilder("/demo/chart/icons/").append(iconName).append("_c.png").toString());
				level.setImgSrc(iconName);
			}
		});
        
        panel.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                levels.remove(level);
                
                //Re-labeling Panel titles
                for (Component component : levelsContainer.getChildren()) {
                    if(component instanceof Panel) {
                        Panel panel = (Panel) component;
                        Level associatedLevel = (Level) panel.getAttribute(Constants.LEVEL);
                        panel.setTitle("Level " + (1 + levels.indexOf(associatedLevel)));
                    }
                }
            }
        });
        
        Panelchildren panelChildren = new Panelchildren();
        panelChildren.setParent(panel);
        Vbox vbox=new Vbox();
        vbox.setHflex("1");
        
        Anchorlayout anchorlayout = new Anchorlayout();
        anchorlayout.setDroppable(Constants.TRUE);
        anchorlayout.addEventListener(Events.ON_DROP, dropListener);
        final Anchorchildren anchorchildren = new Anchorchildren();
        if(level.getElements() == null || level.getElements().isEmpty()) {
            anchorchildren.setSclass("dropZone");
        } else {
            anchorchildren.setSclass("");
            for (LevelElement levelElement : level.getElements()) {
                anchorchildren.appendChild(createLevelElement(levelElement));
            }
        }
        anchorchildren.setHflex("1");
        anchorchildren.setStyle("min-height: 100px;");
        anchorchildren.setParent(anchorlayout);
        anchorlayout.setParent(vbox);
        vbox.setParent(panelChildren);
        
        final Popup popup = new Popup();
        popup.setZclass("popup");
        popup.setParent(panelChildren);
        Hbox hbox=new Hbox();
        hbox.setParent(popup);
        final Textbox textbox = new Textbox();
        textbox.setParent(hbox);
        Button button = new Button();
        button.setLabel("Add");
        button.setZclass("btn btn-sm btn-info");
        button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                if(textbox.getValue().toString().length() < 1) {
                    //TODO: Notify
                    return;
                }
                
                LevelElement element = new LevelElement(textbox.getValue());
                element.setIsColumn(false);
                if(level.getElements() == null) {
                    level.setElements(new ArrayList<LevelElement>());
                }
                level.getElements().add(element);
                
                anchorchildren.setSclass("");
                anchorchildren.appendChild(createLevelElement(element));
                textbox.setValue("");
                popup.close();
            }
        });
        button.setParent(hbox);
        
        Button addConjnBtn = new Button();
        addConjnBtn.setLabel("Add Conjunction");
        addConjnBtn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                popup.open(event.getTarget(), "end_center");
                textbox.setFocus(true);
            }
        });
        addConjnBtn.setZclass("btn btn-xs btn-info levelInsideBtn");
        addConjnBtn.setParent(vbox);
        
        levelsContainer.insertBefore(panel,addLevel);
    }
    
    /**
     * Fetches rootKey/Level 1 values
     */
    @Listen("onClick=#drawTree")
    public void drawTree(Event event) {
        
        //removing the available root values
        if(rootCombobox.getChildren() != null)    {    
        rootCombobox.getChildren().clear();
        }
        
        //clearing the edit window
        Clients.evalJavaScript("clearChart('" + Constants.EDIT_WINDOW_CHART_DIV +  "')");
        
        //Checking for a minimum of 2 Levels
        if(levels.size() < 2) {
            Clients.showNotification(Labels.getLabel("treeLessthanTwoLevels"), "error", levelsContainer, "end_center", 3000, true);
            return;
        }
        
        //Checking for at least one column in each level
        boolean isColumnPresent = false;
        for (Level level : levels) {
            if(level.getElements() != null && !level.getElements().isEmpty()) {
                for (LevelElement element : level.getElements()) {
                    if(element.getIsColumn()) {
                        isColumnPresent = true;
                        break;
                    }
                }
                if(!isColumnPresent) {
                    Clients.showNotification(Labels.getLabel("treeAtleastOneColumnForLevel").concat(String.valueOf(" " + (levels.indexOf(level) + 1))), "error", levelsContainer, "end_center", 5000, true);
                    return;
                }
                isColumnPresent = false;
            } else {
                Clients.showNotification(Labels.getLabel("treeAtleastOneColumnForLevel").concat(String.valueOf(" " + (levels.indexOf(level) + 1))), "error", levelsContainer, "end_center", 5000, true);
                return;
            }
        }
        
        treeData.setLevels(levels);
        
        Clients.showBusy(chart, "Retriving data");
        Events.echoEvent(new Event("onPopulateList", this.getSelf()));
    }

    /**
     * Constructs Tree
     * @param event
     */
    @Listen("onSelect=#rootCombobox")
    public void onSelectRoot(SelectEvent<Component, Object> event) {
        Comboitem selectedItem = (Comboitem) event.getSelectedItems().iterator().next();
        
        @SuppressWarnings("unchecked")
        Map<String, String> rootValueMap = (Map<String, String>) selectedItem.getAttribute(Constants.VALUE);
        
        treeData.setRootValueMap(rootValueMap);
        
        Clients.showBusy(chart, "Retriving data");
        Events.echoEvent(new Event("onDrawChart", this.getSelf()));
        
        doneButton.setDisabled(false);
    }
    
    EventListener<Event> drawChartListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
        	HttpSession httpSession =  (HttpSession)Executions.getCurrent().getSession().getNativeSession();
        	httpSession.setAttribute(Constants.EDIT_WINDOW_CHART_DIV,treeData);
        	
            chartRenderer.constructTreeJSON(treeData, portlet,Constants.EDIT_WINDOW_CHART_DIV);
            chartRenderer.drawChart(Constants.EDIT_WINDOW_CHART_DIV, portlet);
            Clients.clearBusy(chart);            
        }
    };
    
    
    EventListener<Event> populateCombobox = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event)throws Exception {
        	try {
            //Removing available values
            rootCombobox.getChildren().clear();
            
            List<List<String>> valueList;
			
				valueList = hpccService.getRootValues(treeData, treeData.getLevels().get(0), null);
			
            StringBuilder stringBuilder;
            
            addlevelElement(valueList);
            
            rootCombobox.setVisible(true);
            
            //Creating Label
            stringBuilder = new StringBuilder();
            for (LevelElement element : levels.get(0).getElements()) {
                stringBuilder.append(element.getName());
            }
            stringBuilder.append(": ");
            rootLabel.setValue(stringBuilder.toString());
            rootLabel.setVisible(true);
            
            
            Clients.clearBusy(chart);
        	} catch (RemoteException | HpccConnectionException e) {
				 LOG.error(Constants.EXCEPTION, e);
				Clients.showNotification("Unable to fetch Hpcc root values",
						"error", EditTreeController.this.getSelf(),
						"middle_center", 5000, true);
				 Clients.clearBusy(chart);
               return;
			}
        }
    };

    /**
     * Appends level elements to Level
     * @param valueList
     */
    protected void addlevelElement(List<List<String>> valueList) {
        StringBuilder stringBuilder;
        Iterator<String> valueIterator;
        String value;
        Map<String, String> valueMap;
        for (List<String> list : valueList) {
            valueMap = new LinkedHashMap<String, String>();
            stringBuilder = new StringBuilder();
            valueIterator = list.iterator();
            for (LevelElement element : levels.get(0).getElements()) {
                if(element.getIsColumn()) {
                    value = valueIterator.next();
                    stringBuilder.append(value);
                    valueMap.put(element.getFileName() + "." + element.getName(), value);
                } else {
                    stringBuilder.append(element.getName());
                }
            }
            Comboitem comboitem = new Comboitem(stringBuilder.toString());
            comboitem.setAttribute(Constants.VALUE, valueMap);
            rootCombobox.appendChild(comboitem);
            if(treeData.getRootValueMap() != null && treeData.getRootValueMap().equals(valueMap)) {
                rootCombobox.setSelectedItem(comboitem);
            }
        }
    }
    
}

