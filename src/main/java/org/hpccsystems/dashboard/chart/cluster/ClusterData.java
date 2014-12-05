package org.hpccsystems.dashboard.chart.cluster;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ChartData;

@XmlRootElement
public class ClusterData  extends ChartData {
    private Attribute id;
    private Attribute relation;
    private Attribute category;
    private List<Attribute> details;
    
    @XmlElement
    public Attribute getId() {
        return id;
    }
    public void setId(Attribute id) {
        this.id = id;
    }
    @XmlElement
    public Attribute getRelation() {
        return relation;
    }
    public void setRelation(Attribute relation) {
        this.relation = relation;
    }
    @XmlElement
    public Attribute getCategory() {
        return category;
    }  
    public void setCategory(Attribute type) {
        this.category = type;
    }
    @XmlElement
    public List<Attribute> getDetails() {
        return details;
    }
    public void setDetails(List<Attribute> details) {
        this.details = details;
    }
}
