package org.hpccsystems.dashboard.entity;

import java.util.List;

public class FileMeta {
	
	private String fileName;
	private String scope;
	private Boolean isDirectory;
	private List<FileMeta> childlist;
	
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public Boolean isDirectory() {
		return isDirectory;
	}
	public void setIsDirectory(Boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	
	public List<FileMeta> getChildlist() {
		return childlist;
	}
	public void setChildlist(List<FileMeta> childlist) {
		this.childlist = childlist;
	}
}
