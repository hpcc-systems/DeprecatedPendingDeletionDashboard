package org.hpccsystems.dashboard.controller.component;

import org.hpccsystems.dashboard.chart.entity.AdvancedFilter;
import org.hpccsystems.dashboard.common.Constants;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;

public class FilterRowRenderer implements RowRenderer<AdvancedFilter>{

	@Override
	public void render(final Row row, final AdvancedFilter filter, int index) throws Exception {
		Label droppedColum = new Label(filter.getColumnName());
		droppedColum.setParent(row);
		
		Combobox operatorCombobox = new Combobox();
		operatorCombobox.setHflex("1");
		
		Comboitem operatorEqual = new Comboitem("=");
		operatorEqual.setParent(operatorCombobox);		
		Comboitem operatorGraeterEqual = new Comboitem(">=");
		operatorGraeterEqual.setParent(operatorCombobox);
		Comboitem operatorLessEqual = new Comboitem("<=");
		operatorLessEqual.setParent(operatorCombobox);
		Comboitem operatorLess = new Comboitem("<");
		operatorLess.setParent(operatorCombobox);
		Comboitem operatorGraeter = new Comboitem(">");
		operatorGraeter.setParent(operatorCombobox);
		Comboitem operatorNot = new Comboitem("<>");
		operatorNot.setParent(operatorCombobox);
		
		operatorCombobox.setParent(row);
		operatorCombobox.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				filter.setOperator(((Combobox)event.getTarget()).getSelectedItem().getLabel());
			}
		});
		//Showing the applied operator while retrieving from DB
		if(filter.getOperator() != null){
			//TODO:go for better way..
			for(Component comp :operatorCombobox.getChildren()){
				if(((Comboitem)comp).getLabel().equals(filter.getOperator())){
					operatorCombobox.setSelectedItem((Comboitem)comp);
				}
			}
		}
		Textbox valueTextbox = new Textbox();
		valueTextbox.setHflex("1");
		valueTextbox.setParent(row);
		valueTextbox.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				filter.setOpeartorValue(((Textbox)event.getTarget()).getValue());	
			}
		});
		
		//Showing the applied value while retrieving from DB
		if (filter.getOpeartorValue() != null) {
			valueTextbox.setValue(filter.getOpeartorValue());
		}
		
		Hbox hbox = new Hbox();
		hbox.setHflex("1");
		Combobox modifierCombobox = new Combobox();
		modifierCombobox.setHflex("1");
		Comboitem modifierOne = new Comboitem();
		modifierOne.setLabel("*");		
		modifierOne.setParent(modifierCombobox);
		Textbox modifierValue = new Textbox();
		modifierValue.setHflex("1");
		modifierCombobox.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				filter.setModifier(((Combobox)event.getTarget()).getSelectedItem().getLabel());	
			}
		});
		
		//Showing the applied operator while retrieving from DB
		if (filter.getModifier() != null) {
			// TODO:go for better way..
			for (Component comp : modifierCombobox.getChildren()) {
				if (((Comboitem) comp).getLabel().equals(filter.getModifier())) {
					modifierCombobox.setSelectedItem((Comboitem) comp);
				}
			}
		}

		modifierValue.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				filter.setModifierValue(((Textbox)event.getTarget()).getValue());	
			}
			
		});
		
		//Showing the applied value while retrieving from DB
		if (filter.getModifierValue() != null) {
			modifierValue.setValue(filter.getModifierValue());
		}
		modifierCombobox.setParent(hbox);
		modifierValue.setParent(hbox);
		hbox.setParent(row);
		
		Button deleteButton = new Button();
		deleteButton.setLabel("DELETE");
		deleteButton.setParent(row);	
		deleteButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				Events.postEvent(Constants.ON_DELETE_FILTER,row.getParent().getParent(),filter);
			}
		});
		
	}
	
	
}
