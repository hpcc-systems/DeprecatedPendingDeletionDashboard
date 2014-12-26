package org.hpccsystems.dashboard.entity.widget;

import java.util.List;

public abstract class Widget {
    private String name;
    private List<Filter> filters;
    
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
    
}
