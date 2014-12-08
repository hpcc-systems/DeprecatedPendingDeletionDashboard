package org.hpccsystems.dashboard.impl.hipie;

import java.util.HashMap;
import java.util.Map;

import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.CompositionInstance;
import org.hpcc.HIPIE.Contract;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.HIPIEService;
import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.OutputElement;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.HpccConnection;
import org.hpccsystems.dashboard.init.HipieSingleton;
import org.hpccsystems.dashboard.service.hipie.CompositionService;

public class CompositionServiceImpl implements CompositionService {

	private HIPIEService hipieService = HipieSingleton.getHipie();
	
	@Override
	public Composition createComposition(String userId, String compName,HpccConnection hpccConnection)
			throws Exception {
		Composition composition = hipieService.getCompositionTemplate(userId, "BasicTemplate");
		composition.setLabel(compName);
		compName = compName.replaceAll("[^a-zA-Z0-9]+", "");
		composition.setName(compName);
		updateRawDataset(composition);
		createPlugin(userId,compName,composition);		
		return composition;
	}

	private void updateRawDataset(Composition composition) throws Exception {
		ContractInstance rawDatasetContract = composition.getContractInstanceByName(Constants.HIPIE_RAW_DATASET);
		rawDatasetContract.setFileName("");
		Map<String,String[]> paramMap = new HashMap<String, String[]>();
		paramMap.put("LogicalFilename", new String[]{"LogicalFilename"});
		paramMap.put("Method", new String[]{"THOR"});
		paramMap.put("Structure", new String[]{""});
	}

	private void createPlugin(String userId, String compName,Composition composition) throws Exception {
		
		
		Contract contract = new Contract();
		contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());		
		contract.setLabel(compName);
		compName = compName.replaceAll("[^a-zA-Z0-9]+", "");
		contract.setName(compName);
		contract.setAuthor(userId);
		contract.setDescription(compName);
		
		InputElement input =new InputElement();
		input.setName("dsInput");
		//TODO:need to change for roxie query
		input.setType(InputElement.TYPE_DATASET);
		//TODO:set measure and attribute
		
		input.addOption(new ElementOption(Element.MAPBYNAME));
		contract.getInputElements().add(input);
		
		OutputElement output=new OutputElement();
		output.setName("dsOutput");
		output.setType(OutputElement.TYPE_DATASET);
		output.setBase("dsInput");
		output.addOption(new ElementOption("WUID"));
		contract.getOutputElements().add(output);

		
		

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
