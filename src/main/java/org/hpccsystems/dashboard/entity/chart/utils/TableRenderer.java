package org.hpccsystems.dashboard.entity.chart.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.HPCCService;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.SpringUtil;
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
		public Vbox constructTableWidget(final Portlet portlet, Boolean isEditing){		
			
			HPCCService hpccService = (HPCCService) SpringUtil.getBean("hpccService");
			
			LinkedHashMap<String, List<String>> tableDataMap;
			try {
				tableDataMap = hpccService.fetchTableData(portlet.getChartData());
			} catch (Exception e) {
				Clients.showNotification("Unexpected error. Could not construct table.", true);
				LOG.error("Table data error", e);
				return null;
			}
			
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
				listheader.setSort("auto");
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
					fileConverter.exportListboxToExcel(listBox,portlet.getName());
				}
			});
			hbox.appendChild(button);
			
			button = new Button();
			button.setIconSclass("glyphicon glyphicon-save");
			button.setZclass("btn btn-xs btn-info");
			button.setLabel("CSV");
			button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					TableRenderer.this.fileConverter.exportListboxToCsv(listBox,portlet.getName());
				}
			});
			hbox.appendChild(button);
			
			Vbox vbox = new Vbox();
			vbox.appendChild(listBox);
			vbox.appendChild(hbox);
			
			if(LOG.isDebugEnabled()) {
				LOG.debug("Created table widget..");
			}
			
			return vbox;
		}
}
