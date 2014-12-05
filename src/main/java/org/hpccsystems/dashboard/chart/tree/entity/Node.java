package org.hpccsystems.dashboard.chart.tree.entity;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;


public class Node { 
    private String name;
    private List<Node> children;
    private String imageSrc;
    private Integer level;
    
    private List<List<String>> filters;
    
    @SerializedName("_children")
    private List<String> dummyChildren;
    
    /**
     * Initializes an empty ArrayList. This is used in D3 tree to indicate existence of next level
     */
    public void setDummyChildren() {
        dummyChildren = new ArrayList<String>();
    }
    
    public Node() {
    }

    public Node(String name) {
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    public List<List<String>> getFilters() {
        return filters;
    }

    public void setFilters(List<List<String>> filters) {
        this.filters = filters;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node [name=").append(name).append(", children=").append(children)
			.append(", imageSrc=").append(imageSrc).append(", level=").append(level)
			.append(", filters=" ).append(filters).append(", dummyChildren=" ).append(dummyChildren)
			.append("]");
		return builder.toString() ;
	}
    
    
}
