package org.hpccsystems.dashboard.chart.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.HPCCService;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Span;
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
     *            Data to draw table
     * @param isEditing
     *            Specify weather table is to be rendered in the edit widget
     *            screen. Used to compute Height of the rendered table
     * @return Table as Listbox
     */
    public Vbox constructTableWidget(final Portlet portlet,
            TableData chartData, Boolean isEditing) {

        HPCCService hpccService = (HPCCService) SpringUtil.getBean(Constants.HPCC_SERVICE);

        Map<String, List<Attribute>> tableDataMap = chartData.getHpccTableData();
        try {
            if(tableDataMap == null){
                tableDataMap = hpccService.fetchTableData(chartData);
            }
        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION, e);
            return null;
        }

        final Listbox listBox = new Listbox();
        listBox.setMold("paging");
        listBox.setSizedByContent(true);
        listBox.setHflex("1");
        //Adjusting height of the Holder Div based on Filter selection
        if(!chartData.getIsFiltered()){        
            if (isEditing) {
                // .. 542 - 45
                listBox.setHeight("497px"); 
            } else {
                // .. 385 - 30
                listBox.setHeight("355px"); 
            }
        } else {
            if (isEditing) {
                // .. 542 - 30 -22 -25
                listBox.setHeight("475px");             
            } else {
                // .. 385 - 30 -25   
                listBox.setHeight("330px"); 
            }
        }
        listBox.setAutopaging(true);

        Listhead listhead = new Listhead();

        Listheader listheader = null;
        List<List<Attribute>> columnList = new ArrayList<List<Attribute>>();
        List<Attribute> displayColumnList = new ArrayList<Attribute>();
        for (Attribute attribute : chartData.getAttributes()) {
            displayColumnList.add(new Attribute(attribute
                    .getDisplayName()));
        }

        listheader = populateListHeader(chartData, tableDataMap, listBox,
                listhead, listheader, columnList);
        populateListCell(listBox, columnList, chartData);
        listBox.appendChild(listhead);
        Hbox hbox = new Hbox();
        hbox.setStyle("margin-left: 3px");
        Button button = new Button();
        button.setIconSclass(Constants.SAVE_BUTTON);
        button.setZclass(Constants.BUTTON_ZCLASS);
        button.setLabel("Excel");
        button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            public void onEvent(Event event) throws Exception {
                fileConverter.exportListboxToExcel(listBox, portlet.getName());
            }
        });
        hbox.appendChild(button);
        button = new Button();
        button.setIconSclass(Constants.SAVE_BUTTON);
        button.setZclass(Constants.BUTTON_ZCLASS);
        button.setLabel("CSV");
        button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            public void onEvent(Event event) throws Exception {
                TableRenderer.this.fileConverter.exportListboxToCsv(listBox, portlet.getName());
            }
        });
        hbox.appendChild(button);
        Vbox vbox = new Vbox();
        //Appending Chart Title as Data file name
        final Div div = new Div();            
        div.setStyle("margin-top: 3px; margin-left: 5px; height: 7px;");
        
        
        if(chartData.getIsFiltered()){                
            constructFilterTitle(div,chartData);
            vbox.appendChild(div);
        }
        
        vbox.appendChild(listBox);
        vbox.appendChild(hbox);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created table widget..");
        }
        return vbox;
    }

    private void populateListCell(final Listbox listBox,
            List<List<Attribute>> columnList, TableData chartData) {
        Listcell listcell;
        Listitem listitem;
        for (int index = 0; index < columnList.get(0).size(); index++) {
            listitem = new Listitem();
            for (List<Attribute> list : columnList) {
                listcell = new Listcell();
                Attribute listCellValue = list.get(index);
                
                if(chartData.getEnableChangeIndicators() && (getNumericValue(listCellValue.getColumn()) < 0)) {
                    listcell.setIconSclass("z-icon-long-arrow-down");
                    listcell.setStyle("background-color: rgb(255, 113, 113);");
                } else if(chartData.getEnableChangeIndicators() && (getNumericValue(listCellValue.getColumn()) > 0)) {
                    listcell.setIconSclass("z-icon-long-arrow-up");
                    listcell.setStyle("background-color: rgb(136, 255, 136);");
                }
                
                listcell.setLabel(listCellValue.getColumn());
                listcell.setParent(listitem);
            }
            listitem.setParent(listBox);
        }
    }
    
    private int getNumericValue(String value) {
        try {
            return new BigDecimal(value).intValue();
        } catch (Exception e) {
            LOG.error(e);
            return 0;
        }
    }

    private Listheader populateListHeader(TableData chartData,
            Map<String, List<Attribute>> tableDataMap, final Listbox listBox,
            Listhead listhead, Listheader listheader,
            List<List<Attribute>> columnList) {
        for (Map.Entry<String, List<Attribute>> entry : tableDataMap.entrySet()) {
            String columnStr = entry.getKey();
            listheader = listHeader(chartData, listheader, columnStr);
            listheader.setSort("auto");
            listheader.setParent(listhead);
            listhead.setParent(listBox);
            columnList.add(entry.getValue());
        }
        return listheader;
    }

    private Listheader listHeader(TableData chartData, Listheader listheader,
            String columnStr) {
        for (Attribute attribute : chartData.getAttributes()) {
            if (attribute.getColumn().equals(columnStr)) {
                if (attribute.getDisplayName() == null) {
                    listheader = new Listheader(columnStr);
                } else {
                    listheader = new Listheader(attribute.getDisplayName());
                }
                break;
            }
        }
        return listheader;
    }
    
    /**
     * Method to construct Filter title for Table widget
     * @param div
     * @param chartData
     * @return Div
     */        
    private Div constructFilterTitle(Div div, ChartData chartData){
        
        Span filterSpan = new Span();
        filterSpan.setClass("btn-link btn-sm");
        filterSpan.setStyle("float: right; padding: 0px 10px;");
        filterSpan.appendChild(new Label("Filters"));            
        div.appendChild(filterSpan);        
        
        final Div filterContentDiv = new Div();
        StringBuilder styleBuffer = new StringBuilder();
        styleBuffer
            .append("line-height: initial; position: absolute; padding: 2px;")
            .append(" border: 1px solid rgb(124, 124, 124); margin: 5px; ")
            .append("background-color: rgb(177, 177, 177);")
            .append("font-size: small; color: white; z-index: 2; display: none;");
        filterContentDiv.setStyle(styleBuffer.toString());
        
        StringBuilder filterDescription = new StringBuilder();
        
        filterDescription.append(" WHERE ");
        
        Iterator<Filter> filterIterator = chartData.getFilters().iterator(); 
        while (filterIterator.hasNext()) {
            Filter filter = (Filter) filterIterator.next();
            if(LOG.isDebugEnabled()) {
                LOG.debug("Filter -> " + filter);
            }
            
            if(chartData.getIsFiltered() &&
                    Constants.DATA_TYPE_STRING.equals(filter.getType())) {
                filterDescription.append(filter.getColumn());
                filterDescription.append(" IS ");
                
                Iterator<String> iterator = filter.getValues().iterator();
                while(iterator.hasNext()){
                    filterDescription.append(iterator.next());
                    if(iterator.hasNext()){
                        filterDescription.append(", ");
                    }
                }

            } else if (chartData.getIsFiltered() && 
                    Constants.DATA_TYPE_NUMERIC.equals(filter.getType())) {
                filterDescription.append(filter.getColumn());
                filterDescription.append(" BETWEEN " + filter.getStartValue());
                filterDescription.append(" & " + filter.getEndValue());
            }
            
            if(filterIterator.hasNext()){
                filterDescription.append(" AND "); 
            }
        }
        filterContentDiv.appendChild(new Label(filterDescription.toString()));
        filterContentDiv.setVisible(false);
        div.appendChild(filterContentDiv);
        //Shows the filter content
        EventListener<Event> showFilterContent = new EventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                filterContentDiv.setVisible(true);    
            }
        };
        //Hides the filter content
        EventListener<Event> hideFilterContent = new EventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                filterContentDiv.setVisible(false);    
            }
        };
        filterSpan.addEventListener(Events.ON_MOUSE_OVER, showFilterContent);
        filterSpan.addEventListener(Events.ON_MOUSE_OUT, hideFilterContent);
        return div;
    }
    
	public Vbox constructScoredSearchTable(Map<String, List<Attribute>> scoredTableData,Boolean isEditing) {
		Vbox vbox = new Vbox();
		final Listbox listBox = new Listbox();
		listBox.setMold("paging");
		listBox.setSizedByContent(true);
		listBox.setHflex("1");
		listBox.setSpan(true);
		if (isEditing) {
	            // .. 542 - 30 -22
	            listBox.setHeight("445px");             
	        } else {
	            // .. 385 - 30 -25    
	            listBox.setHeight("330px"); 
	        }
		listBox.setAutopaging(true);
		
		Listhead listhead = new Listhead();

		Listheader listheader = null;
		List<List<Attribute>> columnList = new ArrayList<List<Attribute>>();

		listheader = populateScoredTableHeader(scoredTableData, listBox,
				listhead, listheader, columnList);
		if(!columnList.isEmpty()){
			populateScoredTableCell(listBox, columnList);
		}
		
		listBox.appendChild(listhead);
		vbox.appendChild(listBox);
		return vbox;

	}

	private void populateScoredTableCell(Listbox listBox,List<List<Attribute>> columnList) {
		Listcell listcell;
		Listitem listitem;
		for (int index = 0; index < columnList.get(0).size(); index++) {
			listitem = new Listitem();
			for (List<Attribute> list : columnList) {
				listcell = new Listcell();
				Attribute listCellValue = list.get(index);
				listcell.setLabel(listCellValue.getColumn());
				listcell.setParent(listitem);
			}
			listitem.setParent(listBox);
		}

	}

	private Listheader populateScoredTableHeader(
			Map<String, List<Attribute>> scoredTableData, Listbox listBox,
			Listhead listhead, Listheader listheader,
			List<List<Attribute>> columnList) {	
		
        for (Map.Entry<String, List<Attribute>> entry : scoredTableData.entrySet()) {
            String columnStr = entry.getKey();
            listheader =new Listheader(columnStr);
            listheader.setSort("auto");
            listheader.setParent(listhead);
            listhead.setParent(listBox);
            listhead.setSizable(true);
            columnList.add(entry.getValue());
        }        
        return listheader;
    
	}

}
