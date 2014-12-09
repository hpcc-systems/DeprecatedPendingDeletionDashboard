package org.hpccsystems.dashboard.chart.entity;


public class ChartConfig {
	
	private Attribute attribute;
	private Measure measure;
	private String file;
	
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public Attribute getAttribute() {
		return attribute;
	}
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	public Measure getMeasure() {
		return measure;
	}
	public void setMeasure(Measure measure) {
		this.measure = measure;
	}	

}
