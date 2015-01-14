package org.hpccsystems.dashboard.entity;

public class Application {
	private String id;
	private String name;
	
	public Application(String id, String name) {
		setId(id);
		setName(name);
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
