package org.hpccsystems.dashboard.chart.cluster;

import java.util.List;

import com.google.gson.annotations.Expose;

public class ClusterJSON {
    @Expose
    private List<String> types;
    @Expose
    private List<String> linkTypes;
    @Expose
    private List<ClusterNode> nodes;
    @Expose
    private List<ClusterLink> links;
    
    public List<String> getTypes() {
        return types;
    }
    public void setTypes(List<String> types) {
        this.types = types;
    }
    public List<ClusterNode> getNodes() {
        return nodes;
    }
    public void setNodes(List<ClusterNode> nodes) {
        this.nodes = nodes;
    }
    public List<ClusterLink> getLinks() {
        return links;
    }
    public void setLinks(List<ClusterLink> links) {
        this.links = links;
    }
    public List<String> getLinkTypes() {
        return linkTypes;
    }
    public void setLinkTypes(List<String> linkTypes) {
        this.linkTypes = linkTypes;
    }
}
