package org.hpccsystems.dashboard.entity.widget;


public class Field {

	private String column;
	private String dataType;
	private String displayName;
	
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public boolean isNumeric() {
	    //TODO implement
	    return false;
	}

}
