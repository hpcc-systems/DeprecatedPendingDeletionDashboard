package org.hpccsystems.dashboard.chart.entity;

public class Measure extends Field {
	
	 private String aggregateFunction;
	 private String file;
	 
	 public String getAggregateFunction() {
		return aggregateFunction;
	}
	public void setAggregateFunction(String aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	

}
