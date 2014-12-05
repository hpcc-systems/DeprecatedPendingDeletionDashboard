package org.hpccsystems.dashboard.entity;

import java.sql.Timestamp;

public class Group {
    
    private String code;
    private String name;
    private String role;
    private Timestamp lastUpdatedDate;
    
    
    public String getRole() {
        return role;
    }
    public void setRole(final String role) {
        this.role = role;
    }
    public Timestamp getLastUpdatedDate() {
        return lastUpdatedDate;
    }
    public void setLastUpdatedDate(final Timestamp lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    @Override
    public String toString() {
        return "Group [code=" + code + ", name=" + name + ", role=" + role
                + ", lastUpdatedDate=" + lastUpdatedDate + "]";
    }
    
    

}
