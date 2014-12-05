package org.hpccsystems.dashboard.chart.gauge;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Measure;

@XmlRootElement
public class GaugeChartData extends ChartData {
    private Attribute attribute;
    private Measure value;
    private Measure total;
    private boolean isTotalRequired;
    
    public GaugeChartData() {
        this.isTotalRequired = true;
    }
    
    @XmlElement
    public Attribute getAttribute() {
        return attribute;
    }
    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }
    
    @XmlElement
    public Measure getValue() {
        return value;
    }
    public void setValue(Measure value) {
        this.value = value;
    }
    
    @XmlElement
    public Measure getTotal() {
        return total;
    }
    public void setTotal(Measure total) {
        this.total = total;
    }
    
    @XmlElement
    public boolean getIsTotalRequired() {
        return isTotalRequired;
    }
    public void setIsTotalRequired(boolean isTotalRequired) {
        this.isTotalRequired = isTotalRequired;
    }
}
