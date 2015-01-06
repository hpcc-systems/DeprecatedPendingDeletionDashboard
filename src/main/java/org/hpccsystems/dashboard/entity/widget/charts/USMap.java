package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.zkoss.zul.ListModelList;

public class USMap extends Widget{

    private Attribute state;
    private Measure measure;
    
    @Override
    public List<String> getColumns() {
        List<String> columnList=new ArrayList<String>();
        columnList.add(state.getDisplayName());
        columnList.add(measure.getDisplayName());
        return columnList;
    }

    @Override
    public String generateSQL() {
        StringBuilder sql=new StringBuilder();        
        sql.append("SELECT ")
        .append(getLogicalFile())
        .append(Constants.DOT)
        .append(state.getColumn())
        .append(Constants.COMMA);
        if(measure.getAggregation()!=null && measure.getAggregation()!= AGGREGATION.NONE){
        sql.append(measure.getAggregation())
        .append("(")
        .append(getLogicalFile())
        .append(Constants.DOT)
        .append(measure.getColumn())
        .append(")");
        }else{
            sql.append(getLogicalFile())
            .append(Constants.DOT)
            .append(measure.getColumn());
        }
        sql.append(" FROM ")
        .append(getLogicalFile());
        
        if((this.getFilters()!=null)&&(!this.getFilters().isEmpty())){
                sql.append(" WHERE ");
                Iterator<Filter> filters=this.getFilters().iterator();
                while(filters.hasNext()){
                    sql.append(filters.next().generateFilterSQL(getLogicalFile()));
                    if(filters.hasNext()){
                        sql.append(" AND ");
                    }
                }                           
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

        VisualElement visualElement = new VisualElement();
        // TODO:Need to set chart type using Hipie's 'Element' class
        visualElement.setType(this.getChartConfiguration().getHipieChartId());
        

        visualElement.setName(DashboardUtil.removeSpaceSplChar(this.getName()));

        RecordInstance ri = new RecordInstance();
        visualElement.setBasisQualifier(ri);

        // Attribute settings
        ri.add(new FieldInstance(null,getPluginAttribute()));
        visualElement.addOption(new ElementOption(VisualElement.STATE,
                new FieldInstance(null, getPluginAttribute())));

        // Measures settings
        ri.add(new FieldInstance((getMeasure().getAggregation() != null) ? getMeasure()
                .getAggregation().toString() : null, getPluginMeasure()));

        visualElement.addOption(new ElementOption(VisualElement.WEIGHT,
                new FieldInstance(null, getPluginMeasure())));

        // Setting Tittle for chart
        visualElement.addOption(new ElementOption(VisualElement.TITLE,
                new FieldInstance(null, this.getTitle())));
        
      //Setting color
        visualElement.addOption(new ElementOption(VisualElement.COLOR,
                new FieldInstance(null, "Red_Yellow_Blue")));

        return visualElement;

    }

    @Override
    public Map<String, String> getInstanceProperties() {
        Map<String, String> fieldNames = new HashMap<String, String>();
        fieldNames.put(getPluginAttribute(), this.getState().getColumn());
        fieldNames.put(getPluginMeasure(), this.getMeasure().getColumn());
        return fieldNames;
    }

    @Override
    public List<InputElement> generateInputElement() {

        List<InputElement> inputs = new ListModelList<InputElement>();

        InputElement attributeInput = new InputElement();
        attributeInput.setName(getPluginAttribute());
        attributeInput.addOption(new ElementOption(Element.LABEL,
                new FieldInstance(null, getState().getColumn())));
        attributeInput.setType(InputElement.TYPE_FIELD);
        inputs.add(attributeInput);

        InputElement measureInput = new InputElement();
        measureInput.setName(getPluginMeasure());
        measureInput.addOption(new ElementOption(Element.LABEL,
                new FieldInstance(null, getMeasure().getColumn())));
        measureInput.setType(InputElement.TYPE_FIELD);
        inputs.add(measureInput);

        return inputs;

    }
    
    @Override
    public boolean isConfigured() {
        return (this.getState()!=null)&&(this.getMeasure()!=null);
    }

    @Override
    public List<String> getSQLColumns() {
        List<String> sqlColumnList=new ArrayList<String>();
        sqlColumnList.add(state.getDisplayName());
        if((measure.getAggregation()!=null)&&(measure.getAggregation()!=AGGREGATION.NONE)){
            sqlColumnList.add(measure.getAggregation().toString()+"out1");
        }else{
            sqlColumnList.add(measure.getDisplayName());
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
     *  generates Name as 'Measure1_chartName[ie: getName()]'
     * @return String
     */
    public String getPluginMeasure() {
        StringBuilder builder = new StringBuilder();
        builder.append("Measure").append("_").append(this.getName());
        return builder.toString();
    }

}
