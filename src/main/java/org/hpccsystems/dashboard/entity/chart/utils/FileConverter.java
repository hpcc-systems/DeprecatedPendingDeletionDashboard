package org.hpccsystems.dashboard.entity.chart.utils;

import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.util.media.AMedia;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;

public class FileConverter {
	
	private static final  Log LOG = LogFactory.getLog(FileConverter.class); 
	
	/**
	 * Writing table content into Excel File
	 */
	public void exportListboxToExcel(Listbox listBox) {
		org.zkoss.exporter.excel.ExcelExporter exporter = new org.zkoss.exporter.excel.ExcelExporter();	
		exporter.getExportContext();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
		exporter.export(listBox, outputStream);
		
		AMedia amedia = new AMedia("Excel_Table_Report.xlsx", "xls", "application/file", outputStream.toByteArray());
		Filedownload.save(amedia);
		
		outputStream.close();
		}
		catch(Exception ex)
		{
			LOG.error("Exception while writing to Excel" , ex);
		}
		
	}
	
	/**
	 * Writing table content into CSV File
	 */
	public void exportListboxToCsv(Listbox listBox)
	{
		try
		{
		StringBuffer comma = new StringBuffer(",");
		StringBuffer fileContent = new StringBuffer();

		for (Object head : listBox.getHeads()) {
			StringBuffer headerData = new StringBuffer("");
			int headCount = 1;
			for (Object header : ((Listhead) head).getChildren()) {				
				headerData.append(((Listheader) header).getLabel());
				if(headCount != ((Listhead) head).getChildren().size() )
				{
				headerData.append(comma);
				}
				headCount++;
			}
			fileContent.append(headerData).append("\n");
		}
		
		for (Object item : listBox.getItems()) {
			StringBuffer itemData = new StringBuffer("");
			int cellCount = 1;
			for (Object cell : ((Listitem) item).getChildren()) {
				StringBuffer cellData = new StringBuffer();
				 cellData.append(((Listcell) cell).getLabel());
				if(cellData.toString().matches("^[a-zA-Z0-9 -!$%^&*()_+|~]*$")
						 && !cellData.toString().matches("^[0-9]*$"))
				{
					cellData=new StringBuffer("\"").append(cellData).append("\"");
				}
				itemData.append(cellData.toString());
				if(cellCount != ((Listitem) item).getChildren().size())
				{
					itemData.append(comma);
				}
				cellCount++;
			}
			fileContent.append(itemData).append("\n");
		}
		Filedownload.save(fileContent.toString().getBytes(), "text/plain",
				"Csv_Table_Report.csv");
	}
	catch(Exception ex)
	{
		LOG.error("Exception while writing to Csv" , ex);
	}
	}


}
