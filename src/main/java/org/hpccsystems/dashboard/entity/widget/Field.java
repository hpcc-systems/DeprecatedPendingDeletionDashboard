package org.hpccsystems.dashboard.entity.widget;

import java.util.Optional;


public class Field {

	private String column;
	private String dataType;
	private String file;
	
	public Field() {
	}
	
	public Field(String column, String dataType) {
	    this.column = column;
	    this.dataType = dataType;
    }
	
    public Field(Field field) {
        this.column = field.column;
        this.dataType = field.dataType;
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
	    Optional<String> optionalDatatype = Optional.ofNullable(dataType);
	    if(optionalDatatype.isPresent()) {
	        String lowerCaseType = optionalDatatype.get().trim().toLowerCase();
	        if(lowerCaseType.contains("integer") || lowerCaseType.contains("real") 
	                || lowerCaseType.contains("decimal") || lowerCaseType.contains("unsigned")) {
	            return true;
	        }
	    }
	    return false;
	}
	public String getFile() {
        return file;
    }
    public void setFile(String file) {
        this.file = file;
    }
    
    @Override
    public boolean equals(Object o){
        final Field thisField=(Field) o;
        if(this.column==thisField.column&&this.dataType==thisField.dataType)
            return true;
        else
            return false;
    }
    
    @Override    
    public int hashCode(){
        int hash = 3;
        hash = 53 * hash + (this.column != null ? this.column.hashCode() : 0);
        return hash;
    }

}
