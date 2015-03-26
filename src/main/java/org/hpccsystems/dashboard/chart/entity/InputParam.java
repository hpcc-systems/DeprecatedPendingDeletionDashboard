package org.hpccsystems.dashboard.chart.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InputParam {
    
    private boolean isCommonInput = false;
    private String name;
    private String value;
    
    public InputParam() {
    }
    
    public InputParam(String inputName) {
        name = inputName;
    }
    
    public InputParam(String inputName,String inputValue) {
        name = inputName;
        value = inputValue;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public boolean getIsCommonInput() {
        return isCommonInput;
    }
    
    public void setIsCommonInput(boolean isCommonInput) {
        this.isCommonInput = isCommonInput;
    }
    
    @XmlElement
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
            builder.append("isCommonInput=").append(isCommonInput).append("name=").append(name)
            .append("value=").append(value).append( "]");
        return  builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InputParam other = (InputParam) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
