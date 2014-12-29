package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.List;
import java.util.Map;

import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;

public class Pie extends Widget {
    private static final String DOT = ".";
    private static final String COMMA = " , ";
    
    private Attribute labels;
    private Measure weight;

    @Override
    public String generateSQL() {        
        StringBuilder sql=new StringBuilder();
        sql.append("SELECT ")
        .append(labels.getFile())
        .append(DOT)
        .append(labels.getColumn())
        .append(COMMA);
        if(weight.getAggregation()!=null && weight.getAggregation()!= AGGREGATION.NONE){
            sql.append(weight.getAggregation())
            .append("(")
            .append(weight.getFile())
            .append(DOT)
            .append(weight.getColumn())
            .append(")");
        }else{
            sql.append(weight.getFile())
            .append(DOT)
            .append(weight.getColumn());
        }
        sql.append(" FROM ")
        .append(labels.getFile());
        
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

    @Override
    public List<String> getColumns() {
        // TODO Auto-generated method stub
        return null;
    }

    public Attribute getWeight() {
        return labels;
    }

    public void setWeight(Attribute weight) {
        this.labels = weight;
    }

    public Measure getLabel() {
        return weight;
    }

    public void setLabel(Measure label) {
        this.weight = label;
    }

    @Override
    public VisualElement generateVisualElement() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getInstanceProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<InputElement> generateInputElement() {
        // TODO Auto-generated method stub
        return null;
    }
}
