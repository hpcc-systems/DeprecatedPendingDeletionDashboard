package org.hpccsystems.dashboard.chart.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

@XmlRootElement
public class Attribute implements Cloneable {

    private String columnName;
    private String displayName;
    private String fileName;
    
    private String dateFormat;
    private String aggregateFunction;    

	private List<Attribute> children;
    
    public Attribute() {
    }
    
    public Attribute(Attribute attribute) {
        setColumn(attribute.getColumn());
        setDisplayName(attribute.getDisplayName());
        setFileName(attribute.getFileName());
        setDateFormat(attribute.getDateFormat());
        setAggregateFunction(attribute.getAggregateFunction());
        setChildren(attribute.getChildren());
    }
    
    @Override
	public String toString() {
		return "Attribute [columnName=" + columnName + ", displayName="
				+ displayName + ", fileName=" + fileName + ", dateFormat="
				+ dateFormat + ", aggregateFunction=" + aggregateFunction
				+ ", children=" + children + "]";
	}

	/**
     * @param columnName
     *            Creates and Attribute and Sets display name as column name
     */
    public Attribute(String columnName) {
        this.columnName = columnName;
        this.displayName = columnName;
    }

    @XmlAttribute
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @XmlAttribute
    public String getColumn() {
        return columnName;
    }

    public void setColumn(String columnName) {
        this.columnName = columnName;
    }

    @XmlAttribute
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((columnName == null) ? 0 : columnName.hashCode());
        result = prime * result
                + ((fileName == null) ? 0 : fileName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Attribute) {
            Attribute arg = (Attribute) obj;
            return this.columnName.equals(arg.getColumn())
                    && this.fileName.equals(arg.getFileName());
        } else if (obj instanceof Filter) {
            Filter arg = (Filter) obj;
            return this.columnName.equals(arg.getColumn())
                    && this.fileName.equals(arg.getFileName());
        } else {
            return this.columnName.equals(obj);
        }
    }



    @XmlAttribute
    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @XmlAttribute
    public String getAggregateFunction() {
		return aggregateFunction;
	}

	public void setAggregateFunction(String aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}
	
    public String getD3DateFormat() {
        if(this.dateFormat != null && !this.dateFormat.isEmpty()) {
            
            //Replacing Year
            String result = processYear();
            //Replacing Month
            result = processMonth(result);
            //Replaces day
            result = processDay(result);
            result = result
                    .replace("HH", "%H")
                    .replace("hh", "%I")
                    .replace("mm", "%M")
                    .replace("ss", "%S")
                    .replace("SS", "%L");
            
            return result;            
        }        
        return null;
    }

    
    /**
     * Converts Date format from Simple date to D3
     * @param result
     * @return String
     */
    private String processDay(String result) {

        StringBuilder charToReplace = null;
        String replacedFormat = result;
        if(StringUtils.contains(dateFormat, "d")){
            charToReplace = new StringBuilder();
            for (int i=0 ; i<dateFormat.length() ; i++) {
                if("d".equalsIgnoreCase(String.valueOf(dateFormat.charAt(i)))){
                    charToReplace.append(dateFormat.charAt(i));
                }else if(charToReplace.length() > 0){
                    break;
                }                
            }
            if(charToReplace.length() == 1){
                replacedFormat = StringUtils.replace(result, charToReplace.toString(), "%e");
            }else if( charToReplace.length() == 2){
                replacedFormat = StringUtils.replace(result, charToReplace.toString(), "%d");
            }else if(charToReplace.length() > 2){                
                replacedFormat = StringUtils.replace(result, charToReplace.toString(), "%_d");                
            }
        }
        return replacedFormat;
    
    
    }

    /**
     * Converts Month format from Simple date to D3
     * @param result
     * @return String
     */
    private String processMonth(String result) {
        StringBuilder charToReplace = null;
        String replacedFormat = result;
        if(StringUtils.contains(dateFormat, "M")){
            charToReplace = new StringBuilder();
            for (int i=0 ; i<dateFormat.length() ; i++) {
                if("M".equals(String.valueOf(dateFormat.charAt(i)))){
                    charToReplace.append(dateFormat.charAt(i));
                }else if(charToReplace.length() > 0){
                    break;
                }                
            }
            if(charToReplace.length() == 1){
                replacedFormat = StringUtils.replace(result, charToReplace.toString(), "%_m");
            }else if( charToReplace.length() == 2){
                replacedFormat = StringUtils.replace(result, charToReplace.toString(), "%m");
            }else if(charToReplace.length() == 3){
                replacedFormat = StringUtils.replace(result, charToReplace.toString(), "%b");
            }else if(charToReplace.length() > 3){                
                replacedFormat = StringUtils.replace(result, charToReplace.toString(), "%B");                
            }
        }
        return replacedFormat;    
    }

    /**Converts Year format from Simple date to D3
     * @return String
     */
    private String processYear() {
        StringBuilder charToReplace = null;
        String replacedFormat = dateFormat;
        if(StringUtils.containsIgnoreCase(dateFormat, "y")){
            charToReplace = new StringBuilder();
            for (int i=0 ; i<dateFormat.length() ; i++) {
                if("y".equalsIgnoreCase(String.valueOf(dateFormat.charAt(i)))){
                    charToReplace.append(dateFormat.charAt(i));
                }else if(charToReplace.length() > 0){
                    break;
                }                
            }
            if(charToReplace.length() == 2){
                replacedFormat = StringUtils.replace(dateFormat, charToReplace.toString(), "%y");
            }else if(charToReplace.length() == 1 || (charToReplace.length() > 2 && charToReplace.length() < 5)){
                replacedFormat = StringUtils.replace(dateFormat, charToReplace.toString(), "%Y");
            }else if(charToReplace.length() > 4){                
                replacedFormat = StringUtils.replace(dateFormat, charToReplace.toString(), "%_Y");                
            }
        }
        
        return replacedFormat;
    }

    public List<Attribute> getChildren() {
        return children;
    }

    public void setChildren(List<Attribute> children) {
        this.children = children;
    }
    
    public boolean isNested() {
        return (children != null);
    }
}
