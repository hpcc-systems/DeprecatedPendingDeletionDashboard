package org.hpccsystems.dashboard.entity.chart.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.controller.EditChartController;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Vbox;

/**
 * TableRenderer class is used to construct Table Widget.
 *
 */
public class TableRenderer {
		
		private static final Log LOG = LogFactory.getLog(TableRenderer.class); 
	
		FileConverter fileConverter = new FileConverter();
		
		
		/**
		 * Constructs a table widget
		 * 
		 * @param tableDataMap
		 * 	Data to draw table
		 * @param isEditing
		 *  Specify weather table is to be rendered in the edit widget screen. Used to compute Height of the rendered table
		 * @return
		 * 	Table as Listbox
		 */	
		public Vbox constructTableWidget(LinkedHashMap<String, List<String>> tableDataMap, Boolean isEditing,final String chartTitle){		
			
			final Listbox listBox = new Listbox();
			listBox.setMold("paging");
			listBox.setSizedByContent(true);
			listBox.setHflex("1");
			
			if(isEditing) {
				listBox.setHeight("512px"); //.. 542 - 30
			} else {
				listBox.setHeight("355px"); //.. 385 - 30
			}
			listBox.setAutopaging(true);
			
			Listhead listhead = new Listhead();
			
			Listheader listheader;
			Listcell listcell;
			Listitem listitem;
			
			List<List<String>> columnList = new ArrayList<List<String>>();
			for (Map.Entry<String, List<String>> entry : tableDataMap.entrySet()) {
				String columnStr = entry.getKey();
				listheader = new Listheader(columnStr);
				listheader.setParent(listhead);
				listhead.setParent(listBox);
				columnList .add(entry.getValue());
			}
			
			for (int index = 0; index < columnList.get(0).size(); index++) {
				listitem = new Listitem();
				for (List<String> list : columnList) {
					listcell = new Listcell();
					String listCellValue = list.get(index);
					listcell.setLabel(listCellValue);
					listcell.setParent(listitem);
				}
				listitem.setParent(listBox);
			}
			
			listBox.appendChild(listhead);
						
			Hbox hbox = new Hbox();
			hbox.setStyle("margin-left: 3px");
			
			Button button = new Button();
			button.setIconSclass("glyphicon glyphicon-save");
			button.setZclass("btn btn-xs btn-info");
			button.setLabel("Excel");
			button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					fileConverter.exportListboxToExcel(listBox,chartTitle);
				}
			});
			hbox.appendChild(button);
			
			button = new Button();
			button.setIconSclass("glyphicon glyphicon-save");
			button.setZclass("btn btn-xs btn-info");
			button.setLabel("CSV");
			button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					TableRenderer.this.fileConverter.exportListboxToCsv(listBox,chartTitle);
				}
			});
			hbox.appendChild(button);
			
			Vbox vbox = new Vbox();
			vbox.appendChild(listBox);
			vbox.appendChild(hbox);
			
			return vbox;
		}
}
