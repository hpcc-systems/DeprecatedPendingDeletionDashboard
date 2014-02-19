package org.hpccsystems.dashboard.entity.chart;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XYChartData {
	
	private HpccConnection hpccConnection;
	
	private String fileName;
	
	private List<String> xColumnNames;
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(", hpccConnection=").append(hpccConnection).append(", fileName=")
				.append(fileName).append(", xColumnNames=").append(xColumnNames)
				.append(", yColumnNames=" ).append(yColumnNames).append(", tableColumns=")
				.append(tableColumns).append(", isFiltered=")
				.append(isFiltered).append(", filter=").append(filter).append("]");
		return  builder.toString();
	}

	private List<String> yColumnNames;
	private List<String> tableColumns;
	
	
	private Boolean isFiltered = false;
	private Filter filter;
	
	private Boolean isGrouped = false;
	
	private Group group;
	
	@XmlElement
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@XmlElement
	public List<String> getXColumnNames() {
		if(xColumnNames == null) {
			xColumnNames = new ArrayList<String>();
		}
		return xColumnNames;
	}

	public void setXColumnNames(List<String> xColumnNames) {
		this.xColumnNames = xColumnNames;
	}

	@XmlElement
	public List<String> getYColumnNames() {
		if(yColumnNames == null) {
			yColumnNames = new ArrayList<String>();
		} 
		return yColumnNames;
	}

	public void setYColumnNames(List<String> yColumnNames) {
		this.yColumnNames = yColumnNames;
	}

	@XmlElement
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	@XmlElement
	public Boolean getIsFiltered() {
		return isFiltered;
	}

	public void setIsFiltered(Boolean isFiltered) {
		this.isFiltered = isFiltered;
	}

	@XmlElement
	public final List<String> getTableColumns() {
		if(tableColumns == null) {
			tableColumns = new ArrayList<String>();
		}
		return tableColumns;
	}

	public final void setTableColumns(List<String> tableColumnName) {
		this.tableColumns = tableColumnName;
	}

	@XmlElement
	public HpccConnection getHpccConnection() {
		if(this.hpccConnection == null)
			this.hpccConnection = new HpccConnection();
		return hpccConnection;
	}

	public void setHpccConnection(HpccConnection hpccConnection) {
		this.hpccConnection = hpccConnection;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@XmlElement
	public Boolean getIsGrouped() {
		return isGrouped;
	}

	public void setIsGrouped(Boolean isGrouped) {
		this.isGrouped = isGrouped;
	}

}
