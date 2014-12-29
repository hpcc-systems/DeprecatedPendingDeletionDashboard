package org.hpccsystems.dashboard.entity.widget;

import org.hpccsystems.dashboard.Constants.AGGREGATION;

public class Measure extends Field {

    private AGGREGATION aggregation;
    private String displayName;

    public Measure() {
    }
    
    public Measure(Field field) {
        super(field);
        this.setDisplayName(field.getColumn());
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public AGGREGATION getAggregation() {
        return aggregation;
    }

    public void setAggregation(AGGREGATION aggregation) {
        this.aggregation = aggregation;
    }

}
