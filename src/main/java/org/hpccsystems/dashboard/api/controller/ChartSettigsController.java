package org.hpccsystems.dashboard.api.controller;

import org.hpccsystems.dashboard.api.entity.ChartConfiguration;
import org.hpccsystems.dashboard.api.entity.Field;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.ChartPanel;
import org.hpccsystems.dashboard.entity.Portlet;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

public class ChartSettigsController extends SelectorComposer<Component>{

	private static final long serialVersionUID = 1L;
	
	@Wire
	Listbox measureListBox;
	@Wire
	Listbox attributeListBox;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		Portlet portlet = (Portlet) Executions.getCurrent().getArg().get(Constants.PORTLET);
		ChartConfiguration configuration = (ChartConfiguration) Executions.getCurrent().getArg().get(Constants.PARAMS);
		ChartPanel chartPanel = (ChartPanel) Executions.getCurrent().getArg().get(Constants.PARENT);
		
		Listitem listItem;
		for (Field field : configuration.getFields()) {
			listItem = new Listitem(field.getColumnName());
			listItem.setDraggable("true");
			if(checkNumeric(field.getColumnName(), field.getDataType())){
				listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.NUMERIC_DATA);
				listItem.setParent(measureListBox);
			} else {
				listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.STRING_DATA);
				listItem.setParent(attributeListBox);
			}
		}
		
	}
	
	private boolean checkNumeric(String column, String dataType) 
	{
		dataType = dataType.toLowerCase();
			if(dataType.contains("integer")	|| 
					dataType.contains("real") || 
					dataType.contains("decimal") ||  
					dataType.contains("unsigned"))	{
				return true;
			}
		return false;
	}
}
