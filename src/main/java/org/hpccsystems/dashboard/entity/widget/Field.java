package org.hpccsystems.dashboard.entity.widget;


public class Field {

	private String column;
	private String dataType;
	private String file;
	
	public Field(String column, String dataType) {
	    this.column = column;
	    this.dataType = dataType;
    }
	
	public Field() {
        super();
    }



    public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
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
	public String getFile() {
        return file;
    }
    public void setFile(String file) {
        this.file = file;
    }

}
