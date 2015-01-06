package org.hpccsystems.dashboard.entity;

import java.util.Map;
import java.util.Set;

import org.hpccsystems.dashboard.chart.entity.Field;

public class QuerySchema {
	private Set<Field> fields;
	private  Map<String,Set<String>> inputParams;
	
	public Set<Field> getFields() {
		return fields;
	}
	public void setFields(Set<Field> fields) {
		this.fields = fields;
	}
	public Map<String, Set<String>> getInputParams() {
		return inputParams;
	}
	public void setInputParams(Map<String, Set<String>> inputParams) {
		this.inputParams = inputParams;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("QuerySchema [fields=").append(fields)
				.append(", inputParams=").append(inputParams).append("]");
		return builder.toString();
	}

	
}
