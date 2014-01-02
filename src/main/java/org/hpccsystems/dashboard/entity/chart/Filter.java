package org.hpccsystems.dashboard.entity.chart;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Filter {
	private Integer type;
	private String column;
	
	/**
	 *  Present only got String Filter
	 */
	private List<String> values;
	
	/**
	 * Present only for Numeric Filter
	 */
	private Double StartValue;
	private Double EndValue;
	
	@XmlAttribute
	public Integer getType() {
		return type;
	}
	
	public void setType(Integer filterType) {
		this.type = filterType;
	}
	
	@XmlElement
	public List<String> getValues() {
		return values;
	}
	
	public void setValues(List<String> list) {
		this.values = list;
	}
	
	@XmlElement
	public Double getStartValue() {
		return StartValue;
	}
	
	public void setStartValue(Double startValue) {
		StartValue = startValue;
	}
	
	@XmlElement
	public Double getEndValue() {
		return EndValue;
	}
	
	public void setEndValue(Double endValue) {
		EndValue = endValue;
	}
	
	@XmlElement
	public String getColumn() {
		return column;
	}
	
	public void setColumn(String column) {
		this.column = column;
	}
}
