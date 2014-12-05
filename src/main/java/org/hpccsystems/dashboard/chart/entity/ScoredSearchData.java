package org.hpccsystems.dashboard.chart.entity;

import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class ScoredSearchData extends ChartData {
	
	private List<String> groupbyColumns;
	private String aggregateFunction;	
	private List<AdvancedFilter> advancedFilters;
	private List<String> inputParamNames;
	private HashMap<String, HashMap<String, List<Attribute>>> hpccTableData;
	
	@XmlTransient
	public List<String> getInputParamNames() {
		return inputParamNames;
	}
	public void setInputParamNames(List<String> inputParamNames) {
		this.inputParamNames = inputParamNames;
	}
	@XmlElement
	public List<AdvancedFilter> getAdvancedFilters() {
		return advancedFilters;
	}
	public void setAdvancedFilters(List<AdvancedFilter> advancedFilters) {
		this.advancedFilters = advancedFilters;
	}
	@XmlElement
	public List<String> getGroupbyColumns() {
		return groupbyColumns;
	}
	public void setGroupbyColumns(List<String> groupbyColumns) {
		this.groupbyColumns = groupbyColumns;
	}
	@XmlElement
	public String getAggregateFunction() {
		return aggregateFunction;
	}
	public void setAggregateFunction(String aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}
	@XmlTransient
	public HashMap<String, HashMap<String, List<Attribute>>> getHpccTableData() {
		return hpccTableData;
	}
	public void setHpccTableData(
			HashMap<String, HashMap<String, List<Attribute>>> hpccTableData) {
		this.hpccTableData = hpccTableData;
	}
	@Override
	public String toString() {
		return "ScoredSearchData [groupbyColumns=" + groupbyColumns
				+ ", aggregateFunction=" + aggregateFunction
				+ ", advancedFilters=" + advancedFilters + ", inputParamNames="
				+ inputParamNames + ", hpccTableData=" + hpccTableData + "]";
	}
	

}
