package org.hpccsystems.dashboard.service.impl.hipie;

import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.CompositionInstance;
import org.hpcc.HIPIE.ContractInstance;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.chart.entity.HPCCConnection;
import org.hpccsystems.dashboard.entity.Widget;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.hipie.CompositionService;
import org.hpccsystems.dashboard.service.hipie.HipieSingleton;
import org.hpccsystems.dashboard.util.HIPIEUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service("compositionService") 
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CompositionServiceImpl implements CompositionService {
	
	private AuthenticationService authenticationService;
	
	@Autowired
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	public Composition createComposition(String compName,HPCCConnection hpccConnection,Widget widget)
			throws Exception {
		String label = compName;
		Composition composition = HipieSingleton.getHipie().getCompositionTemplate(
				authenticationService.getUserCredential().getId(),"BasicTemplate");
		composition.setLabel(label);
		compName = label.replaceAll("[^a-zA-Z0-9]+", "");
		composition.setName(compName);
		//TODO:Need to iterate widget list create rawdataset
		HIPIEUtil.updateRawDataset(composition,widget.getChartConfig().getFile(),hpccConnection);
		ContractInstance pluginContract = HIPIEUtil.createPlugin(authenticationService.getUserCredential(), label,composition,widget);		
		
		ContractInstance datasource=composition.getContractInstanceByName(Constants.HIPIE_RAW_DATASET);
		pluginContract.addPrecursor(datasource);	
		
		composition = HipieSingleton.getHipie().saveCompositionAs(authenticationService.getUserCredential().getId(), composition,
				 compName + ".cmp");
		return composition;
	}
	
	@Override
	public Composition updateComposition(String userId, String compName) {
		return null;
	}

	@Override
	public CompositionInstance runComposition(String userId, String compName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteComposition(String userId, String compName) {
		// TODO Auto-generated method stub
		return false;
	}

}
