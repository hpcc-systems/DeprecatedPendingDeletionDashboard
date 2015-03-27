package org.hpccsystems.dashboard.entity;

import java.util.List;

import org.hpccsystems.dashboard.chart.entity.InputParam;

public class RequestParams {
    private String dashbaordId;
    private List<InputParam> inputParams;
    
    public List<InputParam> getInputParams() {
        return inputParams;
    }
    public void setInputParams(List<InputParam> inputParams) {
        this.inputParams = inputParams;
    }
    public String getDashbaordId() {
        return dashbaordId;
    }
    public void setDashbaordId(String dashbaordId) {
        this.dashbaordId = dashbaordId;
    }
    public boolean hasInputParams() {
        return inputParams != null && !inputParams.isEmpty();
    }
    
    @Override
    public String toString() {
        return "RequestParams [dashbaordId=" + dashbaordId + ", inputParams=" + inputParams + "]";
    }
}
