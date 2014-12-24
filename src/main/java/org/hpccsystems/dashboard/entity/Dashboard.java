package org.hpccsystems.dashboard.entity;

import java.time.LocalDateTime;

public class Dashboard {
    private String id;
    private String name;
    private LocalDateTime lastUpDateTime;
    private boolean visiblity;
    
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
    public LocalDateTime getLastUpDateTime() {
        return lastUpDateTime;
    }
    public void setLastUpDateTime(LocalDateTime lastUpDateTime) {
        this.lastUpDateTime = lastUpDateTime;
    }
    public boolean isVisiblity() {
        return visiblity;
    }
    public void setVisiblity(boolean visiblity) {
        this.visiblity = visiblity;
    }
}
