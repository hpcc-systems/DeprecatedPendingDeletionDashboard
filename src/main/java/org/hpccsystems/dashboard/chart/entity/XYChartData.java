package org.hpccsystems.dashboard.chart.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XYChartData extends ChartData {

    private Attribute attribute;
    private Attribute groupAttribute;
    private List<Measure> measures;
    private Boolean isGrouped = false;
    private XYGroup group;
    private BigDecimal yAxisMinVal; 
	private BigDecimal yAxisMaxVal;
	private BigDecimal y2AxisMinVal; 
	private BigDecimal y2AxisMaxVal;
	private boolean isAxisrotated;  
	private Double yThresholdValMin;
    private Double yThresholdValMax;
	private Double y2ThresholdValMin;
	private Double y2ThresholdValMax;

	

	public XYChartData() {
    }
    
    public XYChartData(ChartData chartData) {
        this.setFields(chartData.getFields());
        this.setFiles(chartData.getFiles());
        this.setFilters(chartData.getFilters());
        this.setHpccConnection(chartData.getHpccConnection());
        this.setInputParams(chartData.getInputParams());
        this.setIsFiltered(chartData.getIsFiltered());
        this.setIsQuery(chartData.getIsQuery());
        this.setJoins(chartData.getJoins());
    }
    
    public boolean hasMultipleMeasures() {
        return getMeasures().size() > 1;
    }
    
    @XmlElement
    public List<Measure> getMeasures() {
        if (measures == null) {
            measures = new ArrayList<Measure>();
        }
        return measures;
    }

    public void setMeasures(List<Measure> yColumnNames) {
        this.measures = yColumnNames;
    }

    public XYGroup getGroup() {
        return group;
    }

    public void setGroup(XYGroup group) {
        this.group = group;
    }

    @XmlElement
    public Boolean getIsGrouped() {
        return isGrouped;
    }

    public void setIsGrouped(Boolean isGrouped) {
        this.isGrouped = isGrouped;
    }
    
    public boolean isGrouped() {
        return getIsGrouped();
    }

    @XmlElement
    public BigDecimal getyAxisMinVal() {
		return yAxisMinVal;
	}

	public void setyAxisMinVal(BigDecimal yAxisMinVal) {
		this.yAxisMinVal = yAxisMinVal;
	}

	@XmlElement
	public BigDecimal getyAxisMaxVal() {
		return yAxisMaxVal;
	}

	public void setyAxisMaxVal(BigDecimal yAxisMaxVal) {
		this.yAxisMaxVal = yAxisMaxVal;
	}
	   @XmlElement
	   public Double getyThresholdValMin() {
	        return yThresholdValMin;
	    }

	    public void setyThresholdValMin(Double yThresholdValMin) {
	        this.yThresholdValMin = yThresholdValMin;
	    }
	    @XmlElement
	    public Double getyThresholdValMax() {
	        return yThresholdValMax;
	    }

	    public void setyThresholdValMax(Double yThresholdValMax) {
	        this.yThresholdValMax = yThresholdValMax;
	    }
	    @XmlElement
	    public Double getY2ThresholdValMin() {
	        return y2ThresholdValMin;
	    }

	    public void setY2ThresholdValMin(Double y2ThresholdValMin) {
	        this.y2ThresholdValMin = y2ThresholdValMin;
	    }
	    @XmlElement
	    public Double getY2ThresholdVaMaxl() {
	        return y2ThresholdValMax;
	    }

	    public void setY2ThresholdVaMaxl(Double y2ThresholdVaMaxl) {
	        this.y2ThresholdValMax = y2ThresholdVaMaxl;
	    }
	

    @XmlElement
    public boolean getIsAxisrotated() {
        return isAxisrotated;
    }

    public void setIsAxisrotated(boolean isAxisrotated) {
        this.isAxisrotated = isAxisrotated;
    }
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("XYChartData [attribute=").append(getAttribute())
                .append(", measures=").append(measures)
                .append(", isGrouped=").append(isGrouped).append(", group=")
                .append(group).append(", yAxisMinVal=").append(yAxisMinVal)
                .append(", isAxisrotated=").append(isAxisrotated)
                .append(", yAxisMaxVal=").append(yAxisMaxVal)
                 .append(", yThresholdValMin=").append(yThresholdValMin)
                  .append(", yThresholdValMax=").append(yThresholdValMax)
                   .append(", y2ThresholdValMin=").append(y2ThresholdValMin)
                  .append(", y2ThresholdValMax=").append(yThresholdValMax).append(", inputParams=").append(this.getInputParams()).append("]");
        return buffer.toString();
    }

    @XmlElement
    public BigDecimal getY2AxisMinVal() {
        return y2AxisMinVal;
    }

    public void setY2AxisMinVal(BigDecimal y2AxisMinVal) {
        this.y2AxisMinVal = y2AxisMinVal;
    }

    @XmlElement
    public BigDecimal getY2AxisMaxVal() {
        return y2AxisMaxVal;
    }

    public void setY2AxisMaxVal(BigDecimal y2AxisMaxVal) {
        this.y2AxisMaxVal = y2AxisMaxVal;
    }

    @XmlElement
    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    @XmlElement
    public Attribute getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(Attribute groupAttribute) {
        this.groupAttribute = groupAttribute;
    }
    
    public boolean hasAttribute(Attribute attribute) {
        if(attribute == null) {
            return false;
        } else {
            return attribute.equals(getAttribute()) || attribute.equals(getGroupAttribute());
        }
    }

    public void removeAttribute() {
        setAttribute(null);
    }

    public void removeGroupAttribute() {
        setGroupAttribute(null);
    }
    
    public boolean isDrawable() {
        return !getMeasures().isEmpty() && (getAttribute() != null);
    }
    
    public String getxAxisLabel() {
        return getAttribute().getDisplayName() != null ? getAttribute().getDisplayName() : getAttribute().getColumn();
    }
    
    /**
     * @return
     *  Labels for Y Axis as Array
     *  Primary label as first element & secondary labels a second element
     */
    public String[] getyAxisLabels() {
        String[] labels = new String[2];
        final String AND = " & ";
        
        StringBuilder label = new StringBuilder();
        StringBuilder secondaryLabel = new StringBuilder();
        ListIterator<Measure> iterator = getMeasures().listIterator();
        Measure nextMeasure = null;
        Measure measure  = null;
        while (iterator.hasNext()) {
        	measure = iterator.next();
            if(measure.isSecondary()) {
                secondaryLabel.append(measure.getLabel());
            } else {
                label.append(measure.getLabel());
            }
            
            if(iterator.hasNext()) {
                nextMeasure = iterator.next();
                if(nextMeasure.isSecondary() && measure.isSecondary() ) {
                    secondaryLabel.append(AND);
                } else if(!nextMeasure.isSecondary() && !measure.isSecondary()) {
                    label.append(AND);
                }
                iterator.previous();
            }
        }
        
        labels[0] = label.toString();
        labels[1] = secondaryLabel.toString();
        return labels;
    }
}
