package org.hpccsystems.dashboard.util;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import java.util.Map;

import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.Measure;
import org.hpccsystems.dashboard.common.Constants;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;

public class UiGenerator {
    
    /**
     * Creates Accordion for all the files
     */
    public static void generateTabboxChildren(ChartData chartData,Tabbox filesTabbox) {
        Listitem listItem;
        Listbox fileListBox;
        Tab fileTab;
        Tabpanel fileTabpanel;    
        for (Map.Entry<String, List<Field>> entry : chartData.getFields().entrySet()) {
            //creating tab for a file
            fileTab = new Tab(entry.getKey());
            fileTab.setParent(filesTabbox.getFirstChild());
            
            fileTabpanel = new Tabpanel();
            fileTabpanel.setSclass("collapsiblePanel");
            fileListBox = new Listbox();
            fileListBox.setVflex(true);
            fileListBox.setParent(fileTabpanel);
            fileTabpanel.setParent(filesTabbox.getLastChild());
            
            //Appending columns to the tab for a file
            for (Field field : entry.getValue()) {
                listItem = new Listitem(field.getColumnName());
                listItem.setAttribute(Constants.FIELD, field);
                if(field.getDataType().equals(Constants.DATA_TYPE_DATASET_STRING)) {
                    listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.DATA_TYPE_DATASET);
                } else if(DashboardUtil.checkNumeric(field.getDataType())) {
                    listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.DATA_TYPE_NUMERIC);
                } else {
                    listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.DATA_TYPE_STRING);
                }
                listItem.setDraggable(Constants.TRUE);
                listItem.setParent(fileListBox);
            }
        }
    }
    
    
    /**
     * Aggregate Function selection listener
     */
    static EventListener<SelectEvent<Component, Object>> selectAggregateFunctionListener = new EventListener<SelectEvent<Component, Object>>() {

        @Override
        public void onEvent(SelectEvent<Component, Object> event) throws Exception {
            Listitem selectedItem = (Listitem) event.getSelectedItems().iterator().next();
            Popup popup = (Popup) selectedItem.getParent().getParent();
            Listcell listcell = (Listcell) popup.getParent();
            Measure measure = (Measure) listcell.getParent().getAttribute(Constants.MEASURE);
            measure.setAggregateFunction(selectedItem.getValue().toString());
            Button button = null;
            for (Component component : listcell.getChildren()) {
                if (component instanceof Button) {
                    button = (Button) component;
                }
            }
            button.setLabel(selectedItem.getLabel());
            popup.close();
        }
    };
    
    public static void generateTabboxChildren(Tabbox measureTabbox, Tabbox attributeTabbox,
            Map<String, List<Field>> fieldMap, boolean createMeasureForAttributes) {
        Listitem listItem;
        Listcell listcell;
        Listbox attributesListBox;
        Tab measureTab;
        Tab attributeTab;
        Tabpanel measureTabpanel;
        Tabpanel attributeTabpanel;

        for (Map.Entry<String, List<Field>> entry : fieldMap.entrySet()) {
            final Listbox measuresListBox = new Listbox();
            measuresListBox.setVflex("1");
            attributesListBox = new Listbox();
            attributesListBox.setVflex("1");
            
            for (Field field : entry.getValue() ) {
                listItem = new Listitem();
                listcell = new Listcell(field.getColumnName());
                listItem.setAttribute(Constants.FIELD, field);
                listItem.appendChild(listcell);
                listItem.setDraggable("true");
                if(DashboardUtil.checkNumeric(field.getDataType())){
                    listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.DATA_TYPE_NUMERIC);
                    final Measure measure = new Measure(field.getColumnName(), Constants.NONE);
                    listItem.setAttribute(Constants.MEASURE, measure);
                    
                    final Popup popup = new Popup();
                    popup.setWidth("100px");
                    popup.setZclass(Constants.STYLE_POPUP);
                    final Button button = new Button("None");
                    button.setZclass("btn btn-xs");
                    button.setStyle("font-size: 10px; float: right;");
                    button.setPopup(popup);
                    
                    Listbox listbox = new Listbox();
                    listbox.setMultiple(false);
                    listbox.appendItem("Average", Constants.AVERAGE);
                    listbox.appendItem("Count", Constants.COUNT);
                    listbox.appendItem("Minimum", Constants.MINIMUM);
                    listbox.appendItem("Maximum", Constants.MAXIMUM);
                    listbox.appendItem("Sum", Constants.SUM);
                    listbox.appendItem("None", Constants.NONE);
                    
                    listbox.addEventListener(Events.ON_SELECT, selectAggregateFunctionListener);
                    
                    popup.appendChild(listbox);
                        listcell.appendChild(popup);
                        listcell.appendChild(button);
                        
                    listItem.setParent(measuresListBox);
                    
                } else {
                    listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.DATA_TYPE_STRING);
                    //creating measure from string column only when logical file is used
                    if(createMeasureForAttributes) {
                        final String columnName = field.getColumnName();
                        final Popup popup1 = new Popup();
                        popup1.setWidth("200px");
                        popup1.setZclass(Constants.STYLE_POPUP);

                        final Button btn = new Button();
                        btn.setSclass("glyphicon glyphicon-cog btn btn-link img-btn");
                        btn.setStyle("float:right");
                        btn.setVisible(false);
                        btn.setPopup(popup1);

                        Listbox listbox = new Listbox();
                        listbox.setMultiple(false);
                        listbox.appendItem("Create Measure", "count");
                        listbox.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<Component, Object>>() {

                            @Override
                            public void onEvent(SelectEvent<Component, Object> event) throws Exception {
                                Listitem selectedItem = (Listitem) event.getSelectedItems().iterator().next();
                                if (!"count".equals(selectedItem.getValue())) {
                                    return;
                                }

                                // Create Measure
                                Listitem listItem = new Listitem();
                                Listcell listcell = new Listcell(columnName);
                                listItem.appendChild(listcell);
                                listItem.setDraggable("true");
                                listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.DATA_TYPE_NUMERIC);
                                final Measure measure = new Measure(columnName, "count");
                                listItem.setAttribute(Constants.MEASURE, measure);

                                final Popup popup = new Popup();
                                popup.setWidth("100px");
                                popup.setZclass(Constants.STYLE_POPUP);
                                final Button button = new Button("Count");
                                button.setZclass("btn btn-xs");
                                button.setStyle("font-size: 10px; float: right;");
                                button.setPopup(popup);

                                Listbox listbox = new Listbox();
                                listbox.setMultiple(false);
                                listbox.appendItem("Count", "count");

                                listbox.addEventListener(Events.ON_SELECT, selectAggregateFunctionListener);

                                popup.appendChild(listbox);
                                listcell.appendChild(popup);
                                listcell.appendChild(button);

                                listItem.setParent(measuresListBox);

                                btn.setVisible(false);
                                btn.setDisabled(true);
                                popup1.close();
                            }
                        });

                        popup1.appendChild(listbox);
                        listcell.appendChild(btn);
                        listcell.appendChild(popup1);

                        listcell.addEventListener(Events.ON_MOUSE_OVER,    new EventListener<Event>() {
                                    @Override
                                    public void onEvent(Event arg0) throws Exception {
                                        if (!btn.isDisabled()) {
                                            btn.setVisible(true);
                                        }
                                    }
                                });

                        listcell.addEventListener(Events.ON_MOUSE_OUT, new EventListener<Event>() {
                                    @Override
                                    public void onEvent(Event arg0)    throws Exception {
                                        if (!popup1.isVisible()) {
                                            btn.setVisible(false);
                                        }
                                    }
                                });
                    }
                    listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.DATA_TYPE_STRING);
                    attributesListBox.appendChild(listItem);
                }
            }
            
            //Attaching the measure/Attribute listbox into Tabs
            measureTab = new Tab();
            measureTab.setLabel(entry.getKey());
            measureTabbox.getTabs().appendChild(measureTab);
            
            measureTabpanel = new Tabpanel();
            measureTabpanel.setSclass("collapsiblePanel");
            measureTabpanel.appendChild(measuresListBox);
            measureTabbox.getTabpanels().appendChild(measureTabpanel);
            
            attributeTab = new Tab();
            attributeTab.setLabel(entry.getKey());
            attributeTabbox.getTabs().appendChild(attributeTab);
            
            attributeTabpanel = new Tabpanel();
            attributeTabpanel.setSclass("collapsiblePanel");
            attributeTabpanel.appendChild(attributesListBox);
            attributeTabbox.getTabpanels().appendChild(attributeTabpanel);
        }
    }
    
}
