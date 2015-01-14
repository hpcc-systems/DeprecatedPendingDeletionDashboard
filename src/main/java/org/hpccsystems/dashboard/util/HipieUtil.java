package org.hpccsystems.dashboard.util;

import java.util.Map;

import org.hpcc.HIPIE.Contract;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
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
    
   public static Widget getVisualElementWidget(ContractInstance contractInstance,String chartName){
       
       LOGGER.debug("contractInstance -->"+contractInstance.toString());
       Contract contract = contractInstance.getContract();
       
       VisualElement visualElement = getVisualElement(contract,chartName);
       
       LOGGER.debug("Output -->"+visualElement.getBasis().getName());
       LOGGER.debug("Input -->"+visualElement.getBasis().getBase());
       LOGGER.debug("logical file -->"+contractInstance.getPrecursors().get(visualElement.getBasis().getBase()).getProperty("LogicalFilename"));
       Map<String, ChartConfiguration> chartTypes = Constants.CHART_CONFIGURATIONS;

       ChartConfiguration chartConfig = chartTypes.get(visualElement.getType());
       Widget widget = null;
       
       if (chartConfig.getType() == ChartTypes.PIE.getChartCode()
               || chartConfig.getType() == ChartTypes.DONUT.getChartCode()) {
           widget = new Pie();
           ( (Pie) widget).setWeight(createMeasre(visualElement.getOption(VisualElement.WEIGHT),contractInstance));
           ( (Pie) widget).setLabel(cretaeAttribute(visualElement.getOption(VisualElement.LABEL),contractInstance));
           
       } else if (chartConfig.getType() == ChartTypes.BAR.getChartCode()
               || chartConfig.getType() == ChartTypes.COLUMN.getChartCode()
               || chartConfig.getType() == ChartTypes.LINE.getChartCode()
               || chartConfig.getType() == ChartTypes.SCATTER.getChartCode()
               || chartConfig.getType() == ChartTypes.STEP.getChartCode()
               || chartConfig.getType() == ChartTypes.AREA.getChartCode()) {
           widget = new XYChart();
           //TODO:set measures and attribute
           
       } else if (chartConfig.getType() == ChartTypes.US_MAP.getChartCode()) {
           widget = new USMap();
           //TODO:set state and measure;
           
       }
       widget.setName(visualElement.getName());
       widget.setChartConfiguration(chartConfig);
       widget.setTitle(visualElement.getOption(VisualElement.TITLE)
                .getParams().get(0).getName());
       //getting used logical file
       ContractInstance hookedRawdataset = contractInstance.getPrecursors().get(visualElement.getBasis().getBase());
       widget.setLogicalFile(hookedRawdataset.getProperty("LogicalFilename").substring(1));
       LOGGER.debug("file --->"+hookedRawdataset.getProperty("LogicalFilename").substring(1));
       LOGGER.debug("Title -->"+visualElement.getOption(VisualElement.TITLE).getParams().get(0).getName());
       LOGGER.debug("Ri -->"+visualElement.getBasisQualifier().toString());
       LOGGER.debug("filter -->"+visualElement.getBasisQualifier().getFieldList());
       
       LOGGER.debug("weight -->"+visualElement.getOption(VisualElement.WEIGHT));
       LOGGER.debug("label -->"+visualElement.getOption(VisualElement.LABEL));
      
       LOGGER.debug("widget -->"+widget);
       LOGGER.debug("Pie -->"+(Pie)widget);
       return widget;
    }

public static VisualElement getVisualElement(Contract contract ,String chartName) {

    VisualElement visualization = contract.getVisualElements().iterator().next();
    VisualElement visualElement = (VisualElement)visualization.getChildElement(chartName);
    return visualElement;
}

private static Attribute cretaeAttribute(ElementOption option,ContractInstance contractInstance) {
    
    FieldInstance fieldInstance = option.getParams().get(0);
    
        LOGGER.debug("field -->"+fieldInstance.getAssignment());
        LOGGER.debug("field -->"+fieldInstance.getCanonicalName());
        LOGGER.debug("field -->"+fieldInstance.getFieldLabel());
        LOGGER.debug("field -->"+fieldInstance.getName());
        LOGGER.debug("field -->"+fieldInstance.getOrigin());
        LOGGER.debug("field -->"+fieldInstance.getType());
        LOGGER.debug("field -->"+fieldInstance.getTypeBase());
        LOGGER.debug("field -->"+fieldInstance.getQuoted());
        //TODO:once drea's fix merged, need to fetch actual colum name from contract instance
        //like contractInstance.getProperty("Measure_piechart2")
        Field attributeField = new Field();
        attributeField.setColumn(contractInstance.getProperty(fieldInstance.getName()));
        attributeField.setDataType("string");
       
        Attribute attribute = new Attribute(attributeField);
        attribute.setDisplayName(fieldInstance.getFieldLabel());
    
        return attribute;
}

private static Measure createMeasre(ElementOption option,ContractInstance contractInstance) {
    
    FieldInstance fieldInstance = option.getParams().get(0);
        LOGGER.debug("field -->"+fieldInstance.getAssignment());
        LOGGER.debug("field -->"+fieldInstance.getCanonicalName());
        LOGGER.debug("field -->"+fieldInstance.getFieldLabel());
        LOGGER.debug("field -->"+fieldInstance.getName());
        LOGGER.debug("field -->"+fieldInstance.getOrigin());
        LOGGER.debug("field -->"+fieldInstance.getType());
        LOGGER.debug("field -->"+fieldInstance.getTypeBase());
        LOGGER.debug("field -->"+fieldInstance.getQuoted());
        
        //TODO:once drea's fix merged, need to fetch actual colum name from contract instance
        //like contractInstance.getProperty("Attribute_piechart2")
        Field measureField = new Field();
        measureField.setColumn(contractInstance.getProperty(fieldInstance.getName()));
        measureField.setDataType("unsigned");
        Measure measure = new Measure(measureField);
        if(fieldInstance.getType() != null){
            measure.setAggregation(AGGREGATION.valueOf(fieldInstance.getType()));
        }else{
            measure.setAggregation(AGGREGATION.NONE);
        }
       
        measure.setDisplayName(fieldInstance.getFieldLabel());
       
    return measure;
}

}
