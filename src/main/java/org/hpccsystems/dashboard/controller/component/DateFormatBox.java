package org.hpccsystems.dashboard.controller.component;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.common.Constants;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

public class DateFormatBox extends Vlayout{
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(DateFormatBox.class);
    
    private final Popup parent;
    
    private Listbox listbox;
    private Label sampleLabel;
    private Combobox combobox;
    private Textbox textbox;
    
    public DateFormatBox(Popup parent) {
        this.parent = parent;
        
        listbox = new  Listbox();
        listbox.setWidth("200px");
        listbox.setHeight("250px");
        
        Listhead listhead = new Listhead();
        Listheader listheader = new Listheader("Date formatter");
        Button button = new Button();
        button.setSclass("glyphicon glyphicon-ok btn btn-link img-btn");
        button.setStyle("float:right");
        button.addEventListener(Events.ON_CLICK, saveListener);
        listheader.appendChild(button);
        listhead.appendChild(listheader);
        listbox.appendChild(listhead);
        
        Listitem listitem = new Listitem();
        Listcell listcell = new Listcell();
        combobox = new Combobox();
        combobox.addEventListener(Events.ON_SELECT, selectListener);
        listcell.appendChild(combobox);
        listitem.appendChild(listcell);
        listbox.appendChild(listitem);
        
        listbox.appendChild(new Listitem("Specify date format in chosen column"));
        
        listitem = new Listitem();
        listcell = new Listcell();
        Label label = new Label("Date format:");
        textbox = new Textbox();
        textbox.setSclass("form-control input-sm");
        textbox.addEventListener(Events.ON_CHANGING, formatChangingListener);
        listcell.appendChild(label);
        listcell.appendChild(textbox);
        listitem.appendChild(listcell);
        listbox.appendChild(listitem);
        
        listitem = new Listitem();
        listcell = new Listcell();
        label = new Label("Sample Value:");
        listcell.appendChild(label);
        sampleLabel = new Label();
        sampleLabel.setSclass("form-control input-sm");
        listcell.appendChild(sampleLabel);
        listitem.appendChild(listcell);
        listbox.appendChild(listitem);
        
        this.appendChild(listbox);
    }
    
    
    EventListener<SelectEvent<Component, Object>> selectListener = new EventListener<SelectEvent<Component,Object>>() {
        
        @Override
        public void onEvent(SelectEvent<Component, Object> event) throws Exception {
            Comboitem comboitem = (Comboitem) event.getSelectedItems().iterator().next();
            
            Attribute attribute = (Attribute) comboitem.getAttribute(Constants.ATTRIBUTE);
            if(attribute.getDateFormat() != null 
                    && !attribute.getDateFormat().isEmpty()) {
                textbox.setValue(attribute.getDateFormat());
                sampleLabel.setValue(new SimpleDateFormat(attribute.getDateFormat()).format(new Date()));
            } else {
                textbox.setValue(null);
                sampleLabel.setValue(null);
            }
        }
    };
    
    EventListener<Event> saveListener  = new EventListener<Event>() {

        @Override
        public void onEvent(Event event) throws Exception {
            Attribute attribute = (Attribute) combobox.getSelectedItem().getAttribute(Constants.ATTRIBUTE);
            
            try {
                new SimpleDateFormat(textbox.getValue()).format(new Date());
            } catch (IllegalArgumentException e) {
                LOG.debug(e);
                Clients.showNotification("Date format is invalid");
            }
            
            if(textbox.getValue() != null && !textbox.getValue().isEmpty()) {
                attribute.setDateFormat(textbox.getValue());
                showCalenderIcon(attribute);
            }
            
            parent.close();
        }
        
    };
    
    EventListener<InputEvent> formatChangingListener = new EventListener<InputEvent>() {
        
        @Override
        public void onEvent(InputEvent event) throws Exception {
            try {
                sampleLabel.setValue(new SimpleDateFormat(event.getValue()).format(new Date()));
            } catch (IllegalArgumentException e) {
                LOG.debug(e);
                sampleLabel.setValue("Invalid date format");
            }
        }
    };
    
    private void showCalenderIcon(Attribute attribute) {
        Events.postEvent("onDateformatAdded",parent, attribute);
    }
    
    public void addAttribute(Attribute attribute) {
        Comboitem comboitem = new Comboitem(attribute.getDisplayName());
        comboitem.setAttribute(Constants.ATTRIBUTE, attribute);
        combobox.appendChild(comboitem);
        
        if(attribute.getDateFormat() != null &&
                !attribute.getDateFormat().isEmpty()) {
            showCalenderIcon(attribute);
            
            if( combobox.getSelectedIndex() == -1) {
                textbox.setValue(attribute.getDateFormat());
                sampleLabel.setValue(new SimpleDateFormat(attribute.getDateFormat()).format(new Date()));
                combobox.setSelectedItem(comboitem);
            }
        }
    }
    
    public void removeAttribute(Attribute attribute) {
        Comboitem comboitemToDetach = null;
        for (Component component : combobox.getChildren()) {
            comboitemToDetach = (Comboitem) component;
            if(attribute.equals(comboitemToDetach.getAttribute(Constants.ATTRIBUTE))) {
                break;
            }
        }
        
        if(combobox.getSelectedIndex() != -1 
                && attribute.equals(combobox.getSelectedItem().getAttribute(Constants.ATTRIBUTE))) {
            combobox.setSelectedItem(null);
            textbox.setValue(null);
            sampleLabel.setValue(null);
        }
        
        if(comboitemToDetach != null) {
            comboitemToDetach.detach();
        }
    }
}
