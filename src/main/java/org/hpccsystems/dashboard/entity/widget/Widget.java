package org.hpccsystems.dashboard.entity.widget;

import java.util.List;

import org.hpccsystems.dashboard.Constants.CHART_TYPES;

public abstract class Widget {
    private String name;
    private List<Filter> filters;
    private CHART_TYPES type;
    
    public abstract List<String> getColumns();
    public abstract String generateSQL();
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<Filter> getFilters() {
        return filters;
    }
    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }
    public CHART_TYPES getType() {
        return type;
    }
    public void setType(CHART_TYPES type) {
        this.type = type;
    }    
    
}
