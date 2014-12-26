package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.List;

import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;

public class XYChart extends Widget{

    private Attribute attribute;
    private List<Measure> measure;
    private Attribute groupAttribute;
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
        .append(attribute.getFile())
        .append(DOT)
        .append(attribute.getColumn())
        .append(COMMA);
        measure.stream().forEach(everyMeasure->{
            sql.append(everyMeasure.getFile())
            .append(DOT)
            .append(everyMeasure.getColumn())
            .append(COMMA);
        });
        sql.substring(0, sql.length()-4);
        sql.append(" FROM ")
        .append(attribute.getFile());
        
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

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public List<Measure> getMeasure() {
        return measure;
    }

    public void setMeasure(List<Measure> measure) {
        this.measure = measure;
    }

    public Attribute getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(Attribute groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

}
