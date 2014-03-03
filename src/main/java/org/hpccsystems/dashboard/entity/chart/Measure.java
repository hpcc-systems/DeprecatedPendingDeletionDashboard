package org.hpccsystems.dashboard.entity.chart;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Measure {
	private String column;
	private String aggregateFunction;
	
	public Measure() {}
	
	
	/**
	 * Creates aggregated Measure
	 * @param columnName
	 * @param aggregateFunction
	 */
	public Measure( String columnName, String aggregateFunction) {
		this.column = columnName;
		this.aggregateFunction = aggregateFunction;
	}
	
	@XmlAttribute
	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	@XmlAttribute
	public String getAggregateFunction() {
		return aggregateFunction;
	}
	
	public void setAggregateFunction(String aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result
				+ ((aggregateFunction == null) ? 0 : aggregateFunction.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Measure) {
			Measure arg = (Measure) obj;
			return this.column.equals(arg.getColumn())
					&& this.aggregateFunction.equals(arg.getAggregateFunction());
		} else if (obj instanceof Filter) {
			Filter arg = (Filter) obj;
			return this.column.equals(arg.getColumn());
		} else {
			return this.column.equals(obj);
		}
	}

}
