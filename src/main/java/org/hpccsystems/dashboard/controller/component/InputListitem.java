package org.hpccsystems.dashboard.controller.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;

public class InputListitem extends Listitem {
    private static final long serialVersionUID = 1L;
    
    private boolean isSelectable;
    private String paramName;
    
    private Listbox popupListbox;

    public InputListitem (String paramName, Set<String> values, String id) {
        this.paramName = paramName;
        
        if(values != null && !values.isEmpty()) {
            Listcell listcell = new Listcell(paramName);
            final Button playBtn = new Button();
            playBtn.setSclass("glyphicon glyphicon-play btn btn-link img-btn");
            playBtn.setStyle("float:right");
            
            final Popup popup = new Popup();        
            popup.setZclass("popup");
            
            popupListbox = new Listbox();
            popupListbox.setWidth("200px");
            popupListbox.setHeight("250px");
            popupListbox.setMultiple(false);
            popupListbox.setCheckmark(true);
            
            Listhead listhead = new Listhead();
            Listheader listheader = new Listheader();
            listheader.setLabel(paramName);
            Button clearButton = new Button("Clear");
            clearButton.setZclass("clear-text-btn btn btn-link btn-sm");
            listheader.appendChild(clearButton);
            
            listhead.appendChild(listheader);
            popupListbox.appendChild(listhead);
            
            //Sorting the values of inputparamere
            List<String> sortedValues = new ArrayList<String>(values);
            Collections.sort(sortedValues);
            for (String value : sortedValues) {
                popupListbox.appendChild(new Listitem(value));
            }
            
            popup.appendChild(popupListbox);
            popup.setId("popup_" + id + paramName);
            playBtn.setPopup("popup_" + id + paramName + ", position=end_center");
            listcell.setPopup("popup_" + id + paramName + ", position=end_center");
            
            listcell.appendChild(popup);
            listcell.appendChild(playBtn);
            
            final Label label = new Label();
            label.setSclass("form-control input-sm");
            listcell.appendChild(label);
            
            popupListbox.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    label.setValue(popupListbox.getSelectedItem().getLabel());    
                    popup.close();
                }
            });
            
            clearButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    popupListbox.setSelectedItem(null);
                    label.setValue(null);
                    popup.close();
                }
                
            });
            
            this.appendChild(listcell);
            this.isSelectable = true;
        } else {
            Listcell listcell = new Listcell();
            
            Label label = new Label(paramName);
            Textbox textbox = new Textbox();
            textbox.setSclass("form-control input-sm");
            
            listcell.appendChild(label);
            listcell.appendChild(textbox);
            
            this.appendChild(listcell);
        }
    }
    
    public String getInputValue() {
        if(isSelectable) {
            if(popupListbox.getSelectedItem() != null) {
                Listcell listcell = (Listcell) popupListbox.getSelectedItem().getFirstChild();
                return listcell.getLabel();
            } else {
                return "";
            }
        } else {
            Textbox textbox = (Textbox) this.getFirstChild().getLastChild();
            return textbox.getValue();
        }
    }
    
    public void setInputValue(String value) {
        if(isSelectable) {
            Listitem itemToselect = null;
            Listcell listcell;
            for (Component component : popupListbox.getChildren()) {
                if(component instanceof Listitem) {
                    listcell = (Listcell) component.getFirstChild();
                    if(value.equals(listcell.getLabel())) {
                        itemToselect = (Listitem) component;
                        break;
                    }
                }
            }
            
            popupListbox.setSelectedItem(itemToselect);
            
            Label label  = (Label) this.getFirstChild().getLastChild();
            label.setValue(value);
        } else {
            Textbox textbox = (Textbox) this.getFirstChild().getLastChild();
            textbox.setValue(value);
        }
    }

    public String getParamName() {
        return paramName;
    }
}
