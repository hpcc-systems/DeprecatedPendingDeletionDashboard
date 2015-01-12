package org.hpccsystems.dashboard.util;

import java.util.Map;

import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.ChartTypes;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.entity.widget.charts.Pie;
import org.hpccsystems.dashboard.entity.widget.charts.USMap;
import org.hpccsystems.dashboard.entity.widget.charts.XYChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HipieUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HipieUtil.class);
    
   public static Widget getVisualElementWidget(VisualElement visualElement){


       Map<String, ChartConfiguration> chartTypes = Constants.CHART_CONFIGURATIONS;

       ChartConfiguration chartConfig = chartTypes.get(visualElement.getType());
       Widget widget = null;
       
       if (chartConfig.getType() == ChartTypes.PIE.getChartCode()
               || chartConfig.getType() == ChartTypes.DONUT.getChartCode()) {
           widget = new Pie();
       } else if (chartConfig.getType() == ChartTypes.BAR.getChartCode()
               || chartConfig.getType() == ChartTypes.COLUMN.getChartCode()
               || chartConfig.getType() == ChartTypes.LINE.getChartCode()
               || chartConfig.getType() == ChartTypes.SCATTER.getChartCode()
               || chartConfig.getType() == ChartTypes.STEP.getChartCode()
               || chartConfig.getType() == ChartTypes.AREA.getChartCode()) {
           widget = new XYChart();
       } else if (chartConfig.getType() == ChartTypes.US_MAP.getChartCode()) {
           widget = new USMap();
       }
       widget.setTitle(visualElement.getName());
       widget.setChartConfiguration(chartConfig);
       LOGGER.debug("ve --->"+visualElement.toString());
       
       LOGGER.debug("file --->"+visualElement.getFileName());
       LOGGER.debug("Ri -->"+visualElement.getBasisQualifier().toString());
       LOGGER.debug("filter -->"+visualElement.getBasisFilter());
       LOGGER.debug("filter -->"+visualElement.getBasisQualifier().getFieldList());
       
       LOGGER.debug("weight -->"+visualElement.getOption(VisualElement.WEIGHT));
       LOGGER.debug("label -->"+visualElement.getOption(VisualElement.LABEL));
       LOGGER.debug("fields -->"+visualElement.getOption(VisualElement.WEIGHT).getParams());
       visualElement.getOption(VisualElement.WEIGHT).getParams().stream().forEach(field ->{
           LOGGER.debug("field -->"+field.getAssignment());
           LOGGER.debug("field -->"+field.getCanonicalName());
           LOGGER.debug("field -->"+field.getFieldLabel());
           LOGGER.debug("field -->"+field.getName());
           LOGGER.debug("field -->"+field.getOrigin());
           LOGGER.debug("field -->"+field.getType());
           LOGGER.debug("field -->"+field.getTypeBase());
           LOGGER.debug("field -->"+field.getQuoted());
           
           Field measureField = new Field();
           measureField.setColumn(field.getName());
           measureField.setDataType("unsigned");
           Measure measure = new Measure(measureField);
           measure.setAggregation(AGGREGATION.valueOf(field.getType()));
           measure.setDisplayName(field.getFieldLabel());
        
       });
       //( (Pie) widget).setWeight(measure);
       
       visualElement.getOption(VisualElement.LABEL).getParams().stream().forEach(field ->{
           LOGGER.debug("field -->"+field.getAssignment());
           LOGGER.debug("field -->"+field.getCanonicalName());
           LOGGER.debug("field -->"+field.getFieldLabel());
           LOGGER.debug("field -->"+field.getName());
           LOGGER.debug("field -->"+field.getOrigin());
           LOGGER.debug("field -->"+field.getType());
           LOGGER.debug("field -->"+field.getTypeBase());
           LOGGER.debug("field -->"+field.getQuoted());
           
           Field attributeField = new Field();
           attributeField.setColumn(field.getName());
           attributeField.setDataType("string");
          
           Attribute attribute = new Attribute(attributeField);
           attribute.setDisplayName(field.getFieldLabel());
       });
       
       //( (Pie) widget).setWeight(attribute);
      
   
        return null;
    }

}
