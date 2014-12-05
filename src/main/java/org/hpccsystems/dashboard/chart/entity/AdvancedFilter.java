package org.hpccsystems.dashboard.chart.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class AdvancedFilter {

	String columnName;
	String operator;
	String opeartorValue;
	String modifier;
	String modifierValue;
	
	
	public AdvancedFilter() {
		super();
	}

	public AdvancedFilter(String columnName) {
		super();
		this.columnName = columnName;
	}
	
	@XmlElement
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	@XmlElement
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	@XmlElement
	public String getOpeartorValue() {
		return opeartorValue;
	}
	public void setOpeartorValue(String opeartorValue) {
		this.opeartorValue = opeartorValue;
	}
	@XmlElement
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	@XmlElement
	public String getModifierValue() {
		return modifierValue;
	}
	public void setModifierValue(String modifierValue) {
		this.modifierValue = modifierValue;
	}

	@Override
	public String toString() {
		return "AdvancedFilter [columnName=" + columnName + ", operator="
				+ operator + ", opeartorValue=" + opeartorValue + ", modifier="
				+ modifier + ", modifierValue=" + modifierValue + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((columnName == null) ? 0 : columnName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdvancedFilter other = (AdvancedFilter) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		return true;
	}
	
	
	
	
}
