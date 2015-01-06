package org.hpccsystems.dashboard.chart.cluster;

import java.util.List;

import com.google.gson.annotations.Expose;

public class ClusterNode {
    @Expose
    private List<String> detail;
    @Expose
    private String type;
    
    private List<Relation> connectedNodes;
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    public List<Relation> getConnectedNodes() {
        return connectedNodes;
    }
    public void setConnectedNodes(List<Relation> connectedNodes) {
        this.connectedNodes = connectedNodes;
    }
    public List<String> getDetail() {
        return detail;
    }
    public void setDetail(List<String> detail) {
        this.detail = detail;
    }
}
