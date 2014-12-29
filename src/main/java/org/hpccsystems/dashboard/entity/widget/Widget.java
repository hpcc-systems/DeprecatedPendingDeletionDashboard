package org.hpccsystems.dashboard.entity.widget;

import java.util.List;
import java.util.Map;

import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.Constants.CHART_TYPES;

public abstract class Widget {
    private String name;
    private String logicalFile;
    private List<Filter> filters;
    private CHART_TYPES type;
    
    public abstract List<String> getColumns();
    public abstract String generateSQL();
    public abstract VisualElement generateVisualElement();
    public abstract List<InputElement> generateInputElement();
    public abstract Map<String, String> getInstanceProperties();
    
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
    public String getLogicalFile() {
        return logicalFile;
    }
    public void setLogicalFile(String logicalFile) {
        this.logicalFile = logicalFile;
    }    
    
}
