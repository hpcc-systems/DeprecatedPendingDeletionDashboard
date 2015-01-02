package org.hpccsystems.dashboard.entity.widget;


public abstract class Filter extends Field {
    public Filter() {
    }
    
    public Filter(Field field) {
        super(field);
    }
    
    public abstract String generateFilterSQL();
    
    public abstract boolean hasValues();
    
}