package org.hpccsystems.dashboard.entity.widget;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Attribute extends Field {
    private String displayName;
    
    public Attribute() {
    }
    
    public Attribute(Field field) {
        super(field);
        this.displayName = field.getColumn();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
