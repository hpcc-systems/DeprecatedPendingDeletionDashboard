package org.hpccsystems.dashboard.entity.widget;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Attribute extends Field {
	
	private String file;
	  
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	
}