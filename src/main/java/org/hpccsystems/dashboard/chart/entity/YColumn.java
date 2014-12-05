package org.hpccsystems.dashboard.chart.entity;

public class YColumn {
    private String name;
    private boolean isSecondary;
    
    public YColumn(String name) {
        this.name = name;
    }
    public YColumn(String name, boolean isSecondary) {
        this.name = name;
        this.isSecondary = isSecondary;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isSecondary() {
        return isSecondary;
    }
    public void setSecondary(boolean isSecondary) {
        this.isSecondary = isSecondary;
    }
}
