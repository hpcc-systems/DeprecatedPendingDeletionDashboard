package org.hpccsystems.dashboard.entity;

import java.time.LocalDateTime;


public class Dashboard {


    private int id;
    private String name;
    private String applicationId;    
    private LocalDateTime lastUpDateTime;
    private int visiblity;      
   
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }    
    public LocalDateTime getLastUpDateTime() {
        return lastUpDateTime;
    }
    public void setLastUpDateTime(LocalDateTime lastUpDateTime) {
        this.lastUpDateTime = lastUpDateTime;
    }    
    public int getVisiblity() {
        return visiblity;
    }
    public void setVisiblity(int visiblity) {
        this.visiblity = visiblity;
    }
}
