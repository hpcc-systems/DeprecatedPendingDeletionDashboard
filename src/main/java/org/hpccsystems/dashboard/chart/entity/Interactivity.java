package org.hpccsystems.dashboard.chart.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Interactivity {
    
private int sourceId;
private int targetId;
private String sourceColumn;
private String tragetColumn;
private String filterValue;


@XmlElement
public int getSourceId() {
    return sourceId;
}
public void setSourceId(int sourceId) {
    this.sourceId = sourceId;
}
@XmlElement
public int getTargetId() {
    return targetId;
}
public void setTargetId(int targetId) {
    this.targetId = targetId;
}
@XmlElement
public String getSourceColumn() {
    return sourceColumn;
}
public void setSourceColumn(String sourceColumn) {
    this.sourceColumn = sourceColumn;
}
@XmlElement
public String getTragetColumn() {
    return tragetColumn;
}
public void setTragetColumn(String tragetColumn) {
    this.tragetColumn = tragetColumn;
}
@XmlElement
public String getFilterValue() {
    return filterValue;
}
public void setFilterValue(String filterValue) {
    this.filterValue = filterValue;
}
@Override
public String toString() {
    return "Interactivity [sourceId=" + sourceId + ", targetId=" + targetId
            + ", sourceColumn=" + sourceColumn + ", tragetColumn="
            + tragetColumn + "]";
}



}
