package org.hpccsystems.dashboard.entity;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.util.HipieSingleton;


public class Dashboard {
    private int id;
    private String name;
    private String applicationId;    
    private int visiblity;   
    private String hpccId;   
    
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
    public int getVisiblity() {
        return visiblity;
    }
    public void setVisiblity(int visiblity) {
        this.visiblity = visiblity;
    }
    public String getHpccId() {
        return hpccId;
    }
    public void setHpccId(String hpccId) {
        this.hpccId = hpccId;
    }
    
    /**
     * @return Returns HPCCConnection from HIPIE based on the Hpcc server type
     * selected from UI 
     */
    public HPCCConnection getHpccConnection() {
        if(getHpccId() != null){
            HPCCConnection connection = HipieSingleton.getHipie()
                    .getHpccManager().getConnection(getHpccId());
            return connection;
        }
        return null;
    }
}
