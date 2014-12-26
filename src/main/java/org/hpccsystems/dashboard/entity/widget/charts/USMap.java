package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.List;

import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;

public class USMap extends Widget{

    private Attribute states;
    private Measure measure;
    private static final String DOT=".";
    private static final String COMMA=" , ";
    
    @Override
    public List<String> getColumns() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String generateSQL() {
        StringBuilder sql=new StringBuilder();        
        sql.append("SELECT ")
        .append(states.getFile())
        .append(DOT)
        .append(states.getColumn())
        .append(COMMA);
        if(measure.getAggregation()!=null && measure.getAggregation()!= AGGREGATION.NONE){
        sql.append(measure.getAggregation())
        .append("(")
        .append(measure.getFile())
        .append(DOT)
        .append(measure.getColumn())
        .append(")");
        }else{
            sql.append(measure.getFile())
            .append(DOT)
            .append(measure.getColumn());
        }
        sql.append(" FROM ")
        .append(states.getFile());
        
        if((this.getFilters()!=null)&&(!this.getFilters().isEmpty())){
                sql.append(" WHERE ");
                this.getFilters().stream().forEach(eachFilter->{
                    sql.append(eachFilter.generateFilterSQL())
                    .append(" AND ");
                });                
                sql.substring(0, sql.length()-6);
            }
        return sql.toString();
    }

    public Attribute getStates() {
        return states;
    }

    public void setStates(Attribute states) {
        this.states = states;
    }

    public Measure getMeasure() {
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

}
