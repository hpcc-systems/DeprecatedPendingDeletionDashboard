package org.hpccsystems.dashboard.chart.entity;

import java.util.List;

public class XYModel {
    private List<Object> xAxisValues;
    private List<Object> yAxisValues;
    
    public XYModel() {
    }
    
    public final List<Object> getyAxisValues() {
        return yAxisValues;
    }
    public final void setyAxisValues(final List<Object> yAxisValues) {
        this.yAxisValues = yAxisValues;
    }

    public List<Object> getxAxisValues() {
        return xAxisValues;
    }

    public void setxAxisValues(List<Object> xAxisValues) {
        this.xAxisValues = xAxisValues;
    }

    @Override
    public String toString() {
        return "XYModel [xAxisValues=" + xAxisValues + ", yAxisValues="
                + yAxisValues + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((xAxisValues == null) ? 0 : xAxisValues.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        XYModel other = (XYModel) obj;
        if (xAxisValues == null) {
            if (other.xAxisValues != null)
                return false;
        } else if (xAxisValues.size() == other.xAxisValues.size() 
                && xAxisValues.get(0).equals(other.xAxisValues.get(0))){
            return true;
        }    
        return false;
            
        
    }
    
    
}
