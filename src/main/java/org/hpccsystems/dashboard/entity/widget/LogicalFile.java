package org.hpccsystems.dashboard.entity.widget;

import java.util.List;

public class LogicalFile {
    private String fileName;
    private String scope;
    private boolean directory;    
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
    public boolean isDirectory() {
        return directory;
    }
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }
    public List<LogicalFile> getChildlist() {
        return childlist;
    }
    public void setChildlist(List<LogicalFile> childlist) {
        this.childlist = childlist;
    }
}
