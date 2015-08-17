package org.hpccsystems.dashboard.chart.entity;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CommonFilter {
    private Set<Filter> filters;
    private List<InputParam> inputParams;
   
    public Set<Filter> getFilters() {
        return filters;
    }
    public void setFilters(Set<Filter> filters) {
        this.filters = filters;
    }
    
    public List<InputParam> getInputParams() {
        return inputParams;
    }
    public void setInputParams(List<InputParam> inputParams) {
        this.inputParams = inputParams;
    }
}
