package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.List;

import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;

public class Pie extends Widget {
    private Attribute weight;
    private Measure label;
    private static final String DOT = ".";
    private static final String COMMA = " , ";
    private static StringBuilder sql=new StringBuilder();

    @Override
    public String generateSQL() {        
        sql.append("SELECT ")
        .append(weight.getFile())
        .append(DOT)
        .append(weight.getColumn())
        .append(COMMA);
        if(label.getAggregation()!=null && label.getAggregation()!= AGGREGATION.NONE){
            sql.append(label.getAggregation())
            .append("(")
            .append(label.getFile())
            .append(DOT)
            .append(label.getColumn())
            .append(")");
        }else{
            sql.append(label.getFile())
            .append(DOT)
            .append(label.getColumn());
        }
        sql.append(" FROM ")
        .append(weight.getFile());
        
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
        return weight;
    }

    public void setWeight(Attribute weight) {
        this.weight = weight;
    }

    public Measure getLabel() {
        return label;
    }

    public void setLabel(Measure label) {
        this.label = label;
    }
}
