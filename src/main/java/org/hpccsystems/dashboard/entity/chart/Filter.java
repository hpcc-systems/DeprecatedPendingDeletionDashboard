package org.hpccsystems.dashboard.entity.chart;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Filter {
	private Integer type;
	private String column;
	
	@Override
	public String toString() {
		return "Filter [type=" + type + ", column=" + column + ", values="
				+ values + ", StartValue=" + StartValue + ", EndValue="
				+ EndValue + "]";
	}

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
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Filter){
			Filter filter= (Filter) obj;
			if(this.column.equals(filter.getColumn())){
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.column.hashCode();
	}
}
