package org.hpccsystems.dashboard.entity.chart;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XYChartData {
	
	private Integer sourceType;
	private String URL;
	private String userName;
	private String password;
	private String fileName;
	
	private String xColumnName;
	private String yColumnName;
	
	private Boolean isFiltered = false;
	private Filter filter;

	@XmlElement
	public Integer getSourceType() {
		return sourceType;
	}
	
	public void setSourceType(Integer sourceType) {
		this.sourceType = sourceType;
	}

	@XmlElement
	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	@XmlElement
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@XmlElement
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@XmlElement
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@XmlElement
	public String getXColumnName() {
		return xColumnName;
	}

	public void setXColumnName(String xColumnName) {
		this.xColumnName = xColumnName;
	}

	@XmlElement
	public String getYColumnName() {
		return yColumnName;
	}

	public void setYColumnName(String yColumnName) {
		this.yColumnName = yColumnName;
	}

	@XmlElement
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	@XmlElement
	public Boolean isFiltered() {
		return isFiltered;
	}

	public void setIsFiltered(Boolean isFiltered) {
		this.isFiltered = isFiltered;
	}
}
