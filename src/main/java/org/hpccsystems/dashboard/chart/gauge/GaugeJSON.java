package org.hpccsystems.dashboard.chart.gauge;

import java.util.List;

public class GaugeJSON {
    private String portletId;
    private List<GaugeElement> data;
    private String valueLabel;
    
    public String getPortletId() {
        return portletId;
    }
    public void setPortletId(String portletId) {
        this.portletId = portletId;
    }
    public List<GaugeElement> getData() {
        return data;
    }
    public void setData(List<GaugeElement> data) {
        this.data = data;
    }
    public String getValueLabel() {
        return valueLabel;
    }
    public void setValueLabel(String valueLabel) {
        this.valueLabel = valueLabel;
    }
}
