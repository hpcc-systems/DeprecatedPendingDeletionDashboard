package org.hpccsystems.dashboard.util;

import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.HIPIEService;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.entity.UserCredential;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.service.impl.hipie.HipieSingleton;

public class HIPIEUtil {

	private static HIPIEService hipieService = HipieSingleton.getHipie();
	private static final String CHART_2D = "2DCHART";

	public static ContractInstance createPlugin(UserCredential credential, String compName,Composition composition,Widget widget) throws Exception {	
		//TODO: Add logic	
		return  null;

	}
	

	public static void updateRawDataset(Composition composition,String filename,HPCCConnection hpccConnection) throws Exception {
	  //TODO: Add logic	
	}
}
