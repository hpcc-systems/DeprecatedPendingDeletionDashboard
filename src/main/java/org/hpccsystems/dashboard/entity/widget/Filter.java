package org.hpccsystems.dashboard.entity.widget;

import java.util.List;

public class Filter extends Field {
    private List<String> values;
    private Number minValue;
    private Number maxValue;
    public List<String> getValues() {
        return values;
    }
    public void setValues(List<String> values) {
        this.values = values;
    }
    public Number getMinValue() {
        return minValue;
    }
    public void setMinValue(Number minValue) {
        this.minValue = minValue;
    }
    public Number getMaxValue() {
        return maxValue;
    }
    public void setMaxValue(Number maxValue) {
        this.maxValue = maxValue;
    }
}
