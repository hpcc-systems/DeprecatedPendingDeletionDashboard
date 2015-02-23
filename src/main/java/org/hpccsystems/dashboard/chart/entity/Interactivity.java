package org.hpccsystems.dashboard.chart.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Interactivity {
    
private String sourceId;
private String targetId;
private Field sourceColumn;
private Field tragetColumn;

@XmlElement
public String getSourceId() {
    return sourceId;
}
public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
}
@XmlElement
public String getTargetId() {
    return targetId;
}
public void setTargetId(String targetId) {
    this.targetId = targetId;
}
@XmlElement
public Field getSourceColumn() {
    return sourceColumn;
}
public void setSourceColumn(Field sourceColumn) {
    this.sourceColumn = sourceColumn;
}
@XmlElement
public Field getTragetColumn() {
    return tragetColumn;
}
public void setTragetColumn(Field tragetColumn) {
    this.tragetColumn = tragetColumn;
}



}
