package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.ListModelList;

public class XYChart extends Widget{

    private Attribute attribute;
    private List<Measure> measures;
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
        measures.stream().forEach(everyMeasure->{
            if(everyMeasure.getAggregation()!=null && everyMeasure.getAggregation()!= AGGREGATION.NONE){
            sql.append(everyMeasure.getAggregation())
            .append("(")
            .append(everyMeasure.getFile())
            .append(DOT)
            .append(everyMeasure.getColumn())
            .append(")");
            } else {
                sql.append(everyMeasure.getFile())
                .append(DOT)
                .append(everyMeasure.getColumn());                
            }
            sql.append(COMMA);
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

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasure(List<Measure> measures) {
        this.measures = measures;
    }

    public Attribute getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(Attribute groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

    @Override
    public VisualElement generateVisualElement() {

        StringBuilder builder = null;
        StringBuilder meaureLabels = null;
        VisualElement visualElement = new VisualElement();
        // TODO:Need to set chart type using Hipie's 'Element' class
        visualElement.setType(this.getChartConfiguration().getHipieChartId());
        visualElement.addCustomOption(new ElementOption("_chartType",
                new FieldInstance(null, this.getChartConfiguration()
                        .getHipieChartName())));

        visualElement.setName(DashboardUtil.removeSpaceSplChar(this.getName()));
        visualElement.setBasis(output);

        RecordInstance ri = new RecordInstance();
        visualElement.setBasisQualifier(ri);

        // Attribute settings
        builder = new StringBuilder();
        builder.append("Attribute").append("_").append(this.getName());
        ri.add(new FieldInstance(null, builder.toString()));
        visualElement.addOption(new ElementOption(VisualElement.LABEL,
                new FieldInstance(null, builder.toString())));

        // Measures settings
        getMeasures().listIterator().forEachRemaining(measure -> {
            builder = new StringBuilder();
            // generates Name as 'Measure1_chartName[ie: getName()]'
                builder.append("Measure")
                        .append(getMeasures().indexOf(measure) + 1).append("_")
                        .append(this.getName());
                meaureLabels.append(builder.toString()).append(",");
                ri.add(new FieldInstance(
                        (measure.getAggregation() != null) ? measure
                                .getAggregation().toString() : null, builder
                                .toString()));
            });

        // TODO:Need to check how behaves for multiple measures
        meaureLabels.deleteCharAt(meaureLabels.length() - 1);
        visualElement.addOption(new ElementOption(VisualElement.WEIGHT,
                new FieldInstance(null, meaureLabels.toString())));

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
                new FieldInstance(null, getAttribute().getColumn())));
        attributeInput.setType(InputElement.TYPE_FIELD);
        inputs.add(attributeInput);
        
        getMeasures().listIterator().forEachRemaining(measure -> {
            StringBuilder measureName = new StringBuilder();
            // generates Name as 'Measure1_chartName(ie: getName())'
                measureName.append("Measure")
                        .append(getMeasures().indexOf(measure) + 1).append("_")
                        .append(this.getName());
                InputElement measureInput = new InputElement();
                measureInput.setName(measureName.toString());
                measureInput.addOption(new ElementOption(Element.LABEL,
                        new FieldInstance(null, measure.getColumn())));
                measureInput.setType(InputElement.TYPE_FIELD);
                inputs.add(measureInput);
            });

        return inputs;
    }

}
