package org.hpccsystems.dashboard.chart.entity;

import org.hpccsystems.dashboard.service.hipie.HipieSingleton;

public class HPCCConnection {

	 /**
     * Unique identifier for HPCCConnection stored by HIPIE
     */
    private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public org.hpcc.HIPIE.utils.HPCCConnection getHipieHPCCConnection(){
         org.hpcc.HIPIE.utils.HPCCConnection connection = HipieSingleton.getHipie().getHpccManager().getConnection(getId());
        // TODO:Need to set the cluster types
        // connection.setThorCluster();
        // connection.setRoxieCluster();
         return connection;
	}
}
