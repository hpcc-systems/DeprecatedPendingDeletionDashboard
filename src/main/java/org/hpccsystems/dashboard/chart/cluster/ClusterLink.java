package org.hpccsystems.dashboard.chart.cluster;

import com.google.gson.annotations.Expose;

public class ClusterLink {
    @Expose
    private int source;
    @Expose
    private int target;
    @Expose
    private String type;
    
    public int getSource() {
        return source;
    }
    public void setSource(int source) {
        this.source = source;
    }
    public int getTarget() {
        return target;
    }
    public void setTarget(int target) {
        this.target = target;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
