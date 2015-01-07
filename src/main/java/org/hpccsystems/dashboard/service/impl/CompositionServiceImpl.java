package org.hpccsystems.dashboard.service.impl;

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
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.CompositionService;
import org.hpccsystems.dashboard.util.HipieSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.zkoss.zkplus.spring.SpringUtil;

@Service("compositionService") 
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CompositionServiceImpl implements CompositionService{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositionServiceImpl.class);
    private static final String BASIC_TEMPLATE = "BasicTemplate";
    private static final String HIPIE_RAW_DATASET  = "RawDataset";
    
    @Override
    public void createComposition(Dashboard dashboard, Widget widget,String user) throws Exception {
        HIPIEService hipieService = HipieSingleton.getHipie();
        Composition composition;
        try {
            composition = hipieService.getCompositionTemplate(user,BASIC_TEMPLATE);
            composition.setLabel(dashboard.getName());
            String compName = dashboard.getName().replaceAll("[^a-zA-Z0-9]+", "");
            composition.setName(compName);
            
            ContractInstance datasource = composition.getContractInstanceByName(HIPIE_RAW_DATASET);
            updateRawDataset(datasource, "~" + widget.getLogicalFile(),dashboard.getHpccConnection());
            
            Contract visualContract = createVisualContract(compName,widget);    
            visualContract = hipieService.saveContractAs(user, visualContract, visualContract.getName());
                        
            ContractInstance visualContractInstance = visualContract.createContractInstance();
            widget.getInstanceProperties().forEach((propertyName,propertyValue)->
                visualContractInstance.setProperty(propertyName,propertyValue)
            );
            
            visualContractInstance.addPrecursor(datasource);
            
            composition = HipieSingleton.getHipie().saveCompositionAs(user, composition, compName + ".cmp");
            dashboard.setCompositionName(composition.getCanonicalName());           
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
            throw e;
        }
    }
    
    @Override
    public void updateComposition(Dashboard dashboard, Widget widget,String user) {
        HIPIEService hipieService = HipieSingleton.getHipie();
        Composition composition;
        try {
            composition = hipieService.getComposition(user, dashboard.getCompositionName());
            appendOnPlugin(composition,widget,user);   
            //addInputOutput(composition,"~" + widget.getLogicalFile(),widget,dashboard.getHpccConnection(),user);
            HipieSingleton.getHipie().saveComposition(user, composition);
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
        }
    }
    
    /**
     * @param composition
     * @param fileName
     * @param widget
     * @param hpcc
     * @param user
     * @throws Exception
     * Adds additional input,output and rawdataset to the plugin
     */
    private void addInputOutput(Composition composition, String fileName,Widget widget,HPCCConnection hpcc,String user) throws Exception {
        
        Contract contract = composition.getContractInstanceByName(composition.getName()).getContract();
        HIPIEService hipieService=HipieSingleton.getHipie();
        
        //Adding additional input
        InputElement input2=new InputElement();
        input2.setName("dsInput2");
        input2.setType(InputElement.TYPE_DATASET);
        input2.addOption(new ElementOption(Element.MAPBYNAME));
        contract.getInputElements().add(input2);        
        
        widget.generateInputElement().stream().forEach(inputElement->
            input2.addChildElement(inputElement)
        );

        //Adding additional output
        OutputElement dsoutput2=new OutputElement();
        dsoutput2.setName("dsOutput2");
        dsoutput2.setType(OutputElement.TYPE_DATASET);
        dsoutput2.setBase("dsInput2");
        dsoutput2.addOption(new ElementOption("WUID"));
        contract.getOutputElements().add(dsoutput2);
        
        VisualElement visualElement = widget.generateVisualElement();
        visualElement.setBasis(dsoutput2);

        VisualElement visualization=contract.getVisualElements().iterator().next();
        visualization.addChildElement(visualElement);
        
        contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());
        hipieService.saveContract(user, contract);
        
        hipieService.refreshData();
        
        
        ContractInstance pluginInstance = composition.getContractInstanceByName(composition.getName());
        
        widget.getInstanceProperties().forEach((propertyName,propertyValue)->
        pluginInstance.setProperty(propertyName,propertyValue)
        );
        //Adding additional Rawdataset
        ContractInstance datasource1=composition.getContractInstanceByName(HIPIE_RAW_DATASET);
        ContractInstance datasource2 = new ContractInstance(datasource1.getContract());
        datasource2.setFileName("");
        Map<String,String[]> paramMap = new HashMap<String, String[]>();
        paramMap.put("LogicalFilename", new String[]{fileName});
        paramMap.put("Method", new String[]{"THOR"});
        String fieldseparator=null;
        RecordInstance recordInstance;
        recordInstance = hpcc.getDatasetFields(fileName, fieldseparator);
        datasource2.setProperty("Structure", recordInstance);        
        datasource2.setAllProperties(paramMap);
        
        pluginInstance.addPrecursor(datasource2,"dsOutput","dsInput2"); 
    }


    @Override
    public CompositionInstance runComposition(Dashboard dashboard,String user) throws Exception {
        HIPIEService hipieService=HipieSingleton.getHipie();
        CompositionInstance compositionInstance = null;
        try {
            Composition comp = hipieService.getComposition(user,dashboard.getCompositionName());
            compositionInstance = hipieService.runComposition(comp, dashboard.getHpccConnection(), user);
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
            throw e;
        }
        return compositionInstance;
    }
    
    private void updateRawDataset(ContractInstance rawDatasetIns, String filename, HPCCConnection hpccConnection) throws Exception {
        Map<String,String[]> paramMap = new HashMap<String, String[]>();
        paramMap.put("LogicalFilename", new String[]{filename});
        paramMap.put("Method", new String[]{"THOR"});
        String fieldseparator=null;
        
        RecordInstance recordInstance;
        recordInstance = hpccConnection.getDatasetFields(filename, fieldseparator);
        
        rawDatasetIns.setProperty("Structure", recordInstance);        
        rawDatasetIns.setAllProperties(paramMap);
        //TODO:Need to set FieldSeparator & other info for NON-THOR files
    }
    
    /**
     * @param composition
     * @param widget
     * @param user
     * @throws Exception
     * Adds additional visual element to the plugin file,with single(existing) input/output
     */
    private void appendOnPlugin(Composition composition,Widget widget,String user) throws Exception {
        
        HIPIEService hipieService=HipieSingleton.getHipie();
        Contract contract = composition.getContractInstanceByName(composition.getName()).getContract();
       // Contract contract =hipieService.getContract(user, composition.getName());
        Element input=contract.getInputElements().iterator().next();
        widget.generateInputElement().stream().forEach(inputElement->
            input.addChildElement(inputElement)
        );
        
        VisualElement visualization=contract.getVisualElements().iterator().next();
        VisualElement visualElement = widget.generateVisualElement();
        //Sets basis for visual element
        Element output = contract.getOutputElements().iterator().next();
        visualElement.setBasis(output);
        visualization.addChildElement(visualElement);
        LOGGER.debug("visual 2 -->"+(VisualElement)visualization.getChildElements().iterator().next());
        
        contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());
        hipieService.saveContract(user, contract);
        //contract.save();
        
        hipieService.refreshData();
        
        ContractInstance pluginContract = composition.getContractInstanceByName(composition.getName());
        
        
        widget.getInstanceProperties().forEach((propertyName,propertyValue)->
            pluginContract.setProperty(propertyName,propertyValue)
        );
        LOGGER.debug("pluginContract.getProps() -->"+pluginContract.getProps());
      
    }
    
    private Contract createVisualContract(String compName,Widget widget) throws Exception {   
        
        Contract contract = new Contract();
        HIPIEService hipieService = HipieSingleton.getHipie();
        contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());     
        contract.setLabel(compName);
        contract.setName(compName);
        AuthenticationService authenticationService =(AuthenticationService) SpringUtil.getBean("authenticationService");
        contract.setAuthor(authenticationService.getUserCredential().getId());      
        contract.setDescription("Dashboard charts integrated with Hipie/Marshaller");
        contract.setProp(Contract.CATEGORY, "VISUALIZE");
        contract.setProp(Contract.VERSION, "0.1");
        
        InputElement input = new InputElement();
        input.setName("dsInput");       
        //TODO:need to change for roxie query
        input.setType(InputElement.TYPE_DATASET);   
        input.addOption(new ElementOption(Element.MAPBYNAME));
        
        widget.generateInputElement().stream().forEach(inputElement->
            input.addChildElement(inputElement)
        );
        
        contract.getInputElements().add(input);

        OutputElement output = new OutputElement();
        output.setName("dsOutput");
        output.setType(OutputElement.TYPE_DATASET);
        output.setBase("dsInput");
        output.addOption(new ElementOption("WUID"));
        contract.getOutputElements().add(output);

        VisualElement visualization = new VisualElement();
        visualization.setName(compName);
        visualization.setType(VisualElement.VISUALIZE);
        
        //TODO:set title for visualization
        VisualElement ve = widget.generateVisualElement();
        ve.setBasis(output);
        visualization.addChildElement(ve);
        
        contract.getVisualElements().add(visualization);
        
        return  contract;
    }

    /* 
     * Gets the composition's latest workuntit ID
     */
    @Override
    public String getWorkunitId(Dashboard dashboard,String user) throws Exception {
        Composition composition = null;
        CompositionInstance latestInstance = null;
        composition =  HipieSingleton.getHipie().getComposition(
                user,
                dashboard.getCompositionName());
        
        if(composition != null) {
            latestInstance = composition.getMostRecentInstance(
                    user, true);
            if(latestInstance == null){
                latestInstance = runComposition(dashboard,user);
            } 
            
            if(latestInstance.getWorkunitStatus().contains("failed")) {
               return null;
            }
        }
        
        return latestInstance.getWorkunitId();
    }
}