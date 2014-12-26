package org.hpccsystems.dashboard.entity.widget;

import java.util.List;

public class LogicalFile {
    private String fileName;
    private String scope;
    private Boolean isDirectory;
    private List<LogicalFile> childlist;
    
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getScope() {
        return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }
    public Boolean getIsDirectory() {
        return isDirectory;
    }
    public void setIsDirectory(Boolean isDirectory) {
        this.isDirectory = isDirectory;
    }
    public List<LogicalFile> getChildlist() {
        return childlist;
    }
    public void setChildlist(List<LogicalFile> childlist) {
        this.childlist = childlist;
    }
}
