package org.hpccsystems.dashboard.entity;

import java.time.LocalDateTime;


public class Dashboard {


    private int id;
    private String name;
    private int applicationId;
    private LocalDateTime lastUpDateTime;
    private int sequence;
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
    public int getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }
    public LocalDateTime getLastUpDateTime() {
        return lastUpDateTime;
    }
    public void setLastUpDateTime(LocalDateTime lastUpDateTime) {
        this.lastUpDateTime = lastUpDateTime;
    }
    public int getSequence() {
        return sequence;
    }
    public void setSequence(int sequence) {
        this.sequence = sequence;
    } 
    public int getVisiblity() {
        return visiblity;
    }
    public void setVisiblity(int visiblity) {
        this.visiblity = visiblity;
    }
}
