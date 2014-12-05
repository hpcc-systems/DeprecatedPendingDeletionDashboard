package org.hpccsystems.dashboard.chart.cluster;

public class Relation {
    private String id;
    private String relation;
    
    public Relation(String id) {
        this.id = id;
    }
    public Relation(String id, String relation) {
        this.id = id;
        this.relation = relation;
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getRelation() {
        return relation;
    }
    public void setRelation(String relation) {
        this.relation = relation;
    }
}
