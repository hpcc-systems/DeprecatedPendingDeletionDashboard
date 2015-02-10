package org.hpccsystems.dashboard.entity;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ChartDetails implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 6598478929739766447L;
	private boolean isPlugin;
    private int category;
    private Integer id;
    private String name;
    private String description;
    private ChartConfiguration configuration;
    

    public ChartDetails() {

    }

    @XmlElement
    public boolean getIsPlugin() {
        return isPlugin;
    }

    public void setIsPlugin(boolean isPlugin) {
        this.isPlugin = isPlugin;
    }

    @XmlElement
    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    @XmlElement
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    public ChartConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ChartConfiguration configuration) {
        this.configuration = configuration;
    }
}
