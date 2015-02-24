package org.hpccsystems.dashboard.chart.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Interactivity {
    
private int sourceId;
private int targetId;
private Attribute sourceColumn;
private String tragetColumn;

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
public Attribute getSourceColumn() {
    return sourceColumn;
}
public void setSourceColumn(Attribute sourceColumn) {
    this.sourceColumn = sourceColumn;
}
@XmlElement
public String getTragetColumn() {
    return tragetColumn;
}
public void setTragetColumn(String tragetColumn) {
    this.tragetColumn = tragetColumn;
}



}
