package org.hpccsystems.dashboard.entity.widget;

public class NumericFilter extends Filter {
    private Number minValue;
    private Number maxValue;
    
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
    @Override
    public String generateFilterSQL() {
        // TODO Auto-generated method stub
        return null;
    }
}
