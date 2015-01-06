package org.hpccsystems.dashboard.chart.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.zkoss.util.media.AMedia;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;

public class FileConverter {
    
    private static final  Log LOG = LogFactory.getLog(FileConverter.class);
    
    public FileConverter() {
    }
    
    
    /**
     * Writing table content into Excel File
     */
    public void exportListboxToExcel(Listbox listBox,String chartTitle) {
        org.zkoss.exporter.excel.ExcelExporter exporter = new org.zkoss.exporter.excel.ExcelExporter();    
        exporter.getExportContext();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {        
            exporter.export(listBox, outputStream);
            String fileName = "report";
            if(chartTitle==null || Constants.CHART_TITLE.equalsIgnoreCase(chartTitle)) {
                fileName = "Excel_Table_Report";
            }
            AMedia amedia = new AMedia(fileName +".xlsx", "xls", "application/file", outputStream.toByteArray());
            Filedownload.save(amedia);    
        } catch(Exception ex){
            LOG.error(Constants.EXCEPTION , ex);
        } finally{
            try{
                outputStream.close();
            }catch(IOException ex) {
                LOG.error(Constants.EXCEPTION, ex);
            }
        }
        
    }
    
    /**
     * Writing table content into CSV File
     */
    public void exportListboxToCsv(Listbox listBox,String chartTitle) {
        try {
            StringBuilder comma = new StringBuilder(",");
            StringBuilder fileContent = new StringBuilder();
            StringBuilder headerData = null;
            for (Object head : listBox.getHeads()) {
                headerData = new StringBuilder("");
                int headCount = 1;
                for (Object header : ((Listhead) head).getChildren()) {
                    headerData.append(((Listheader) header).getLabel());
                    if (headCount != ((Listhead) head).getChildren().size()) {
                        headerData.append(comma);
                    }
                    headCount++;
                }
                fileContent.append(headerData).append("\n");
            }
            StringBuilder itemData = null;
            int cellCount = 0;
            StringBuilder cellData = null;
            for (Object item : listBox.getItems()) {
                itemData = new StringBuilder("");
                cellCount = 1;
                for (Object cell : ((Listitem) item).getChildren()) {
                    cellData = new StringBuilder();
                    cellData.append(((Listcell) cell).getLabel());
                    if (cellData.toString().matches("^[a-zA-Z0-9 -!$%^&*()_+|~]*$")
                            && !cellData.toString().matches("^[0-9]*$")) {
                        cellData = new StringBuilder("\"").append(cellData).append("\"");
                    }
                    itemData.append(cellData.toString());
                    if (cellCount != ((Listitem) item).getChildren().size()) {
                        itemData.append(comma);
                    }
                    cellCount++;
                }
                fileContent.append(itemData).append("\n");
            }
            String fileName = "report";
            if (chartTitle == null || Constants.CHART_TITLE.equalsIgnoreCase(chartTitle)) {
                fileName = "Csv_Table_Report";
            }
            Filedownload.save(fileContent.toString().getBytes(), "text/plain", fileName + ".csv");
        } catch (Exception ex) {
            LOG.error(Constants.EXCEPTION, ex);
        }
    }
}
