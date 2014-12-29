package org.hpccsystems.dashboard.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.hpcc.HIPIE.Composition;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.zkoss.zkplus.spring.SpringUtil;

@Service("compositionService") 
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CompositionServiceImpl implements CompositionService{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositionServiceImpl.class);
    private AuthenticationService authenticationService;
    private static final String HIPIE_RAW_DATASET  = "RawDataset";
    
    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    @Override
    public void createComposition(Dashboard dashboard, Widget widget) {
        String label = dashboard.getName();
        HIPIEService hipieService = HipieSingleton.getHipie();
        Composition composition;
        try {
            composition = hipieService.getCompositionTemplate(
                    authenticationService.getUserCredential().getId(),"BasicTemplate");
            composition.setLabel(label);
            String compName = label.replaceAll("[^a-zA-Z0-9]+", "");
            composition.setName(compName);
            updateRawDataset(composition, "~" + widget.getLogicalFile(),dashboard.getHpccConnection());
            ContractInstance pluginContract = createPlugin(label,composition,widget);       
            //refreshes the plugins
            hipieService.refreshData();
            ContractInstance datasource=composition.getContractInstanceByName(HIPIE_RAW_DATASET);
            pluginContract.addPrecursor(datasource);    
            composition = HipieSingleton.getHipie().saveCompositionAs(authenticationService.getUserCredential().getId(), composition,
                     compName + ".cmp");
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
        }
    }

    @Override
    public void runComposition(Dashboard dashboard) {
        HIPIEService hipieService=HipieSingleton.getHipie();
        String userId=authenticationService.getUserCredential().getId();
        Composition comp;
        try {
            comp = hipieService.getComposition(userId,dashboard.getCompositionName());
            hipieService.runComposition(comp, dashboard.getHpccConnection(), userId);
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
        }
    }
    
    private void updateRawDataset(Composition composition,String filename,HPCCConnection hpccConnection) throws Exception {
        ContractInstance rawDatasetContract = composition.getContractInstanceByName(HIPIE_RAW_DATASET);
        rawDatasetContract.setFileName("");
        Map<String,String[]> paramMap = new HashMap<String, String[]>();
        paramMap.put("LogicalFilename", new String[]{filename});
        paramMap.put("Method", new String[]{"THOR"});
        String fieldseparator=null;
        RecordInstance recordInstance;
        recordInstance = hpccConnection.getDatasetFields(filename, fieldseparator);
        rawDatasetContract.setProperty("Structure", recordInstance);        
        rawDatasetContract.setAllProperties(paramMap);
        //TODO:Need to set FieldSeparator & other info for NON-THOR files
    }
    
    private ContractInstance createPlugin(String compName,Composition composition,Widget widget) throws Exception {   
        
        Contract contract = new Contract();
        HIPIEService hipieService = HipieSingleton.getHipie();
        contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());     
        contract.setLabel(compName);
        compName = compName.replaceAll("[^a-zA-Z0-9]+", "");
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
        
        visualization.addChildElement(widget.generateVisualElement());
        
        contract.getVisualElements().add(visualization);
        
        contract = hipieService.saveContractAs(authenticationService.getUserCredential().getId(), contract,contract.getName());
        
        ContractInstance pluginInstance = contract.createContractInstance();
        
        widget.getInstanceProperties().forEach((propertyName,propertyValue)->
            pluginInstance.setProperty(propertyName,propertyValue)
        );
      
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Visuslisation plugin - " + pluginInstance.toCompositionString());
        }
        return  pluginInstance;
    }
}
