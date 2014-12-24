package org.hpccsystems.dashboard.util;

import java.util.HashMap;
import java.util.Map;

import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.Contract;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.HIPIEService;
import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.OutputElement;
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.chart.entity.HPCCConnection;
import org.hpccsystems.dashboard.entity.UserCredential;
import org.hpccsystems.dashboard.entity.Widget;
import org.hpccsystems.dashboard.service.hipie.HipieSingleton;

public class HIPIEUtil {

	private static HIPIEService hipieService = HipieSingleton.getHipie();
	private static final String CHART_2D = "2DCHART";
		
	/**
	 * @param compName
	 * @param composition
	 * @param widget
	 * @throws Exception
	 */
	public static ContractInstance createPlugin(UserCredential credential, String compName,Composition composition,Widget widget) throws Exception {	
		
		Contract contract = new Contract();
		contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());		
		contract.setLabel(compName);
		compName = compName.replaceAll("[^a-zA-Z0-9]+", "");
		contract.setName(compName);
		contract.setAuthor(credential.getId());
		contract.setDescription(widget.getChartName());
		
		InputElement input = new InputElement();
		input.setName("dsInput");
		//TODO:need to change for roxie query
		input.setType(InputElement.TYPE_DATASET);
		//TODO:set measure and attribute
		
		input.addOption(new ElementOption(Element.MAPBYNAME));
		contract.getInputElements().add(input);
		
		OutputElement output = new OutputElement();
		output.setName("dsOutput");
		output.setType(OutputElement.TYPE_DATASET);
		output.setBase("dsInput");
		output.addOption(new ElementOption("WUID"));
		contract.getOutputElements().add(output);

		
        VisualElement visualization = new VisualElement();
        visualization.setName(widget.getChartName());
        visualization.setType(VisualElement.VISUALIZE);
        
        VisualElement visualElement = new VisualElement();
        visualElement.setType(CHART_2D);
        visualElement.setName(widget.getChartName());
        visualElement.setBasis(output);
        
        RecordInstance ri=new RecordInstance();
        ri.add(new FieldInstance(null,widget.getChartConfig().getAttribute().getColumn()));
        //TODO:Check how to set aggregate function
        ri.add(new FieldInstance("SUM",widget.getChartConfig().getMeasure().getColumn()));        
        visualElement.setBasisQualifier(ri);
        
		visualElement.addOption(new ElementOption("LABEL", new FieldInstance(
				null, widget.getChartConfig().getAttribute().getColumn())));
		visualElement.addOption(new ElementOption("WEIGHT", new FieldInstance(
				null, widget.getChartConfig().getMeasure().getColumn())));
        visualElement.addCustomOption(new ElementOption("_chartType",new FieldInstance(null,widget.getChartType())));
        visualization.addChildElement(visualElement);
        
        contract = hipieService.saveContractAs(credential.getId(), contract,contract.getName());
		 
		return  contract.createContractInstance();

	}
	
	/**
	 * @param composition
	 * @param filename
	 * @param hpccConnection
	 * @throws Exception
	 */
	public static void updateRawDataset(Composition composition,String filename,HPCCConnection hpccConnection) throws Exception {
		ContractInstance rawDatasetContract = composition.getContractInstanceByName(Constants.HIPIE_RAW_DATASET);
		rawDatasetContract.setFileName("");
		Map<String,String[]> paramMap = new HashMap<String, String[]>();
		paramMap.put("LogicalFilename", new String[]{filename});
		paramMap.put("Method", new String[]{"THOR"});
		
		String fieldseparator=null;
		RecordInstance recordInstance;
		recordInstance = hpccConnection.getHipieHPCCConnection().getDatasetFields(filename, fieldseparator);
		rawDatasetContract.setProperty("Structure", recordInstance);		
		rawDatasetContract.setAllProperties(paramMap);
		//TODO:Need to set FieldSeparator & other info for NON-THOR files
			
	}
}
