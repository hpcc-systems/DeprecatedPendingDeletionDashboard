package org.hpccsystems.dashboard.chart.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HpccConnections {
    private List<HpccConnection> hpccConnections;
    
    public HpccConnections() {
        this.hpccConnections = new ArrayList<HpccConnection>();
    }
    
    public void addConnection(HpccConnection connection) {
        this.hpccConnections.add(connection);
    }

    @XmlElement(name="hpccConnection")
    public List<HpccConnection> getHpccConnections() {
        return hpccConnections;
    }

    public void setHpccConnections(List<HpccConnection> hpccConnections) {
        this.hpccConnections = hpccConnections;
    }
    
}
