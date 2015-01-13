package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.ChartTypes;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.zkoss.zul.ListModelList;

public class XYChart extends Widget{

    private Attribute attribute;
    private List<Measure> measures;
    private Attribute groupAttribute;
        
    @Override
    public List<String> getColumns() {
        List<String> columnList=new ArrayList<String>();
        columnList.add(attribute.getDisplayName());
        measures.stream().forEach(measure->{
            columnList.add(measure.getDisplayName());
        });        
        return columnList;
    }

    @Override
    public String generateSQL() {
        StringBuilder sql=new StringBuilder();
        sql.append("SELECT ")
        .append(getLogicalFile())
        .append(Constants.DOT)
        .append(attribute.getColumn())
        .append(Constants.COMMA);
        measures.stream().forEach(everyMeasure->{
            if(everyMeasure.getAggregation()!=null && everyMeasure.getAggregation()!= AGGREGATION.NONE){
            sql.append(everyMeasure.getAggregation())
            .append("(")
            .append(getLogicalFile())
            .append(Constants.DOT)
            .append(everyMeasure.getColumn())
            .append(")");
            } else {
                sql.append(getLogicalFile())
                .append(Constants.DOT)
                .append(everyMeasure.getColumn());                
            }
            if(measures.indexOf(everyMeasure)!=measures.size()-1)
            sql.append(Constants.COMMA);
        });
        sql.append(" FROM ")
        .append(getLogicalFile());
        
        if((this.getFilters()!=null)&&(!this.getFilters().isEmpty())){
            Iterator<Filter> filters=this.getFilters().iterator();            
            Filter localFilter;
            boolean firstTime=true;                        
            while(filters.hasNext()){
                localFilter = filters.next();
                if(firstTime&&localFilter.hasValues()){
                    sql.append(" WHERE ");
                    sql.append(localFilter.generateFilterSQL(getLogicalFile()));
                    firstTime=false;
                    }else if(localFilter.hasValues()){
                        sql.append(" AND ");
                        sql.append(localFilter.generateFilterSQL(getLogicalFile()));
                    }                
            }            
        }
        sql.append(" GROUP BY ").append(getLogicalFile()).append(Constants.DOT)
        .append(attribute.getColumn());
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
    
    public void addMeasure(Measure measure){
        if(this.measures!=null){
            this.measures.add(measure);
    }else{
            this.measures=new ArrayList<Measure>();
            this.measures.add(measure);
        }
    }
    
    public void removeMeasure(Measure measure){
        this.measures.remove(measure);
    }

    public Attribute getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(Attribute groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

    @Override
    public VisualElement generateVisualElement() {

        StringBuilder meaureLabels = new StringBuilder();
        VisualElement visualElement = new VisualElement();
        
        // TODO:Need to set chart type using Hipie's 'Element' class
        visualElement.setType(this.getChartConfiguration().getType());
        visualElement.addCustomOption(new ElementOption("_chartType",
                new FieldInstance(null, this.getChartConfiguration()
                        .getHipieChartName())));

        visualElement.setName(DashboardUtil.removeSpaceSplChar(this.getName()));

        RecordInstance ri = new RecordInstance();
        visualElement.setBasisQualifier(ri);

        // Attribute settings
        ri.add(new FieldInstance(null, getPluginAttribute()));

        // Measures settings
        getMeasures().listIterator().forEachRemaining(measure -> {           
                meaureLabels.append(getPluginMeasure(measure)).append(",");
                ri.add(new FieldInstance(
                        (measure.getAggregation() != null) ? measure
                                .getAggregation().toString() : null,getPluginMeasure(measure) ));
            });

        // TODO:Need to check how behaves for multiple measures
        meaureLabels.deleteCharAt(meaureLabels.length() - 1);
        if(ChartTypes.LINE.getChartCode().equals(this.getChartConfiguration().getType())
                || ChartTypes.SCATTER.getChartCode().equals(this.getChartConfiguration().getType())
                || ChartTypes.STEP.getChartCode().equals(this.getChartConfiguration().getType())){
            visualElement.addOption(new ElementOption(VisualElement.X,
                    new FieldInstance(null, getPluginAttribute())));
            visualElement.addOption(new ElementOption(VisualElement.Y,
                    new FieldInstance(null, meaureLabels.toString())));
        }else{
            visualElement.addOption(new ElementOption(VisualElement.LABEL,
                    new FieldInstance(null, getPluginAttribute())));
            visualElement.addOption(new ElementOption(VisualElement.WEIGHT,
                    new FieldInstance(null, meaureLabels.toString())));
        }
       

        // Setting Tittle for chart
        visualElement.addOption(new ElementOption(VisualElement.TITLE,
                new FieldInstance(null, this.getTitle())));

        return visualElement;
    }

    @Override
    public Map<String, String> getInstanceProperties() {
        Map<String, String> fieldNames = new HashMap<String, String>();
        fieldNames.put(getPluginAttribute(), this.getAttribute().getColumn());
        getMeasures().stream().forEach(
                measure -> {
                    fieldNames.put(this.getPluginMeasure(measure),
                            measure.getColumn());
                });
        return fieldNames;
    }

    @Override
    public List<InputElement> generateInputElement() {
        List<InputElement> inputs = new ListModelList<InputElement>();
        
        InputElement attributeInput = new InputElement();
        attributeInput.setName(getPluginAttribute());
        attributeInput.addOption(new ElementOption(Element.LABEL,
                new FieldInstance(null, getAttribute().getColumn())));
        attributeInput.setType(InputElement.TYPE_FIELD);
        inputs.add(attributeInput);
        
        getMeasures().listIterator().forEachRemaining(measure -> {
                InputElement measureInput = new InputElement();
                measureInput.setName(getPluginMeasure(measure));
                measureInput.addOption(new ElementOption(Element.LABEL,
                        new FieldInstance(null, measure.getColumn())));
                measureInput.setType(InputElement.TYPE_FIELD);
                inputs.add(measureInput);
            });

        return inputs;
    }

    @Override
    public boolean isConfigured() {
        return (this.getAttribute()!=null)&&(this.getMeasures()!=null)&&(!this.getMeasures().isEmpty());
    }

    @Override
    public List<String> getSQLColumns() {
        List<String> sqlColumnList=new ArrayList<String>();
        int listSize=0;
        sqlColumnList.add(attribute.getDisplayName());
        Iterator<Measure> measureIterator=measures.iterator();
        while(measureIterator.hasNext()){
            Measure measure=measureIterator.next();            
        if((measure.getAggregation()!=null)&&(measure.getAggregation()!=AGGREGATION.NONE)){
            sqlColumnList.add(measure.getAggregation().toString()+"out"+listSize);
        }else{
            sqlColumnList.add(measure.getDisplayName());
        }   
        listSize++;
        }
        return sqlColumnList;
    }
    
    
    /**
     * generates Name as 'Attribute_chartName(ie: getName())'
     * @return String
     */
    public String getPluginAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append("Attribute").append("_").append(this.getName());
        return builder.toString();
    }

    /**
     * generates Name as 'Measure1_chartName(ie: getName())'
     * @return String
     */
    public String getPluginMeasure(Measure measure) {
        StringBuilder measureName = new StringBuilder();
        measureName.append("Measure")
                .append(getMeasures().indexOf(measure) + 1).append("_")
                .append(this.getName());
        return measureName.toString();
    }

    @Override
    public void editVisualElement(VisualElement visualElement) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeInput(InputElement inputElement) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeInstanceProperty(LinkedHashMap<String, String[]> props) {
        // TODO Auto-generated method stub
        
    }
}
