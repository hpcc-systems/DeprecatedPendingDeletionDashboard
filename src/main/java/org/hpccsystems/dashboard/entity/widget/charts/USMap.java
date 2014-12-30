package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.List;
import java.util.Map;

import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.zkoss.zul.ListModelList;

public class USMap extends Widget{

    private Attribute state;
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
        .append(state.getFile())
        .append(DOT)
        .append(state.getColumn())
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
        .append(state.getFile());
        
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

    public Attribute getState() {
        return state;
    }

    public void setState(Attribute state) {
        this.state = state;
    }

    public Measure getMeasure() {
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    @Override
    public VisualElement generateVisualElement() {

        StringBuilder builder = null;
        VisualElement visualElement = new VisualElement();
        // TODO:Need to set chart type using Hipie's 'Element' class
        visualElement.setType(this.getChartConfiguration().getHipieChartId());
        visualElement.addCustomOption(new ElementOption("_chartType",
                new FieldInstance(null, this.getChartConfiguration()
                        .getHipieChartName())));

        visualElement.setName(DashboardUtil.removeSpaceSplChar(this.getName()));

        RecordInstance ri = new RecordInstance();
        visualElement.setBasisQualifier(ri);

        // Attribute settings
        builder = new StringBuilder();
        builder.append("Attribute").append("_").append(this.getName());
        ri.add(new FieldInstance(null, builder.toString()));
        visualElement.addOption(new ElementOption(VisualElement.LABEL,
                new FieldInstance(null, builder.toString())));

        // Measures settings
        builder = new StringBuilder();
        // generates Name as 'Measure1_chartName[ie: getName()]'
        builder.append("Measure").append("_").append(this.getName());
        ri.add(new FieldInstance((getMeasure().getAggregation() != null) ? getMeasure()
                .getAggregation().toString() : null, builder.toString()));

        visualElement.addOption(new ElementOption(VisualElement.WEIGHT,
                new FieldInstance(null, builder.toString())));

        // Setting Tittle for chart
        visualElement.addOption(new ElementOption(VisualElement.TITLE,
                new FieldInstance(null, this.getTitle())));

        return visualElement;

    }

    @Override
    public Map<String, String> getInstanceProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<InputElement> generateInputElement() {

        List<InputElement> inputs = new ListModelList<InputElement>();

        StringBuilder attributeName = null;
        attributeName = new StringBuilder();
        // generates Name as 'Attribute_chartName(ie: getName())'
        attributeName.append("Attribute").append("_").append(this.getName());
        InputElement attributeInput = new InputElement();
        attributeInput.setName(attributeName.toString());
        attributeInput.addOption(new ElementOption(Element.LABEL,
                new FieldInstance(null, getState().getColumn())));
        attributeInput.setType(InputElement.TYPE_FIELD);
        inputs.add(attributeInput);

        StringBuilder measureName = new StringBuilder();
        // generates Name as 'Measure1_chartName(ie: getName())'
        measureName.append("Measure").append("_").append(this.getName());
        InputElement measureInput = new InputElement();
        measureInput.setName(measureName.toString());
        measureInput.addOption(new ElementOption(Element.LABEL,
                new FieldInstance(null, getMeasure().getColumn())));
        measureInput.setType(InputElement.TYPE_FIELD);
        inputs.add(measureInput);

        return inputs;

    }

}
