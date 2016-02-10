package org.hpccsystems.dashboard.chart.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.zkoss.zul.Listbox;

@XmlRootElement
public class TableData extends ChartData {

    private List<Attribute> attributes;
    private boolean enableChangeIndicators;
    private  Map<String, List<Attribute>> hpccTableData;
    private boolean hasInteractivity;
    private Interactivity interactivity;
    //holds the listbox in which the records are rendered.
    //Need this container to set PageSize based on minimized/maximized window
    private Listbox tableContainer;
    

    public TableData() {
    }
    
    public TableData(ChartData chartData) {
        this.setFields(chartData.getFields());
        this.setFiles(chartData.getFiles());
        this.setFilters(chartData.getFilters());
        this.setHpccConnection(chartData.getHpccConnection());
        this.setInputParams(chartData.getInputParams());
        this.setIsFiltered(chartData.getIsFiltered());
        this.setIsQuery(chartData.getIsQuery());
        this.setJoins(chartData.getJoins());
    }

    @XmlElement
    public List<Attribute> getAttributes() {
        if (attributes == null) {
            setAttributes(new ArrayList<Attribute>());
        }
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("TableData [attributes=").append(attributes)
        .append(",hasInteractivity = ").append(hasInteractivity)
        .append(",interactivity = ").append(interactivity).append("]");
        return buffer.toString();
    }

    @XmlElement
    public boolean getEnableChangeIndicators() {
        return enableChangeIndicators;
    }

    public void setEnableChangeIndicators(boolean enableChangeIndicators) {
        this.enableChangeIndicators = enableChangeIndicators;
    }
    
    @XmlTransient
    public Map<String, List<Attribute>> getHpccTableData() {
        return hpccTableData;
    }

    public void setHpccTableData(Map<String, List<Attribute>> hpccTableData) {
        this.hpccTableData = hpccTableData;
    }
    
    @XmlElement
    public boolean getHasInteractivity() {
        return hasInteractivity;
    }

    public void setHasInteractivity(boolean hasInteractivity) {
        this.hasInteractivity = hasInteractivity;
    }

    @XmlElement
    public Interactivity getInteractivity() {
        return interactivity;
    }

    public void setInteractivity(Interactivity interactivity) {
        this.interactivity = interactivity;
    }

    @XmlTransient
    public Listbox getTableContainer() {
        return tableContainer;
    }

    public void setTableContainer(Listbox tableContainer) {
        this.tableContainer = tableContainer;
    }
}
