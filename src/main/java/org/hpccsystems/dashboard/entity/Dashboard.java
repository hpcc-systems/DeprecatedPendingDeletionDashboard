package org.hpccsystems.dashboard.entity;

import org.apache.commons.lang.StringUtils;
import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.CompositionService;
import org.hpccsystems.dashboard.util.HipieSingleton;
import org.zkoss.zkplus.spring.SpringUtil;


public class Dashboard {
    private int id;
    private String name;
    private String applicationId;    
    private int visiblity;   
    private String hpccId;
    private String compositionName;
    private String layout;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCompositionName() {
        return compositionName;
    }
    public void setCompositionName(String compositionName) {
        this.compositionName = compositionName;
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
    public String getLayout() {
        return layout;
    }
    public void setLayout(String layout) {
        this.layout = layout;
    }
    
    /**
     * @return HPCCConnection corresponding to this.hpccId
     */
    public HPCCConnection getHpccConnection() {
        if(getHpccId() != null){
            HPCCConnection connection = HipieSingleton.getHipie()
                    .getHpccManager().getConnection(getHpccId());
            return connection;
        }
        return null;
    }
    
    /**
     * It generates the chart visualization URL to create the chart
     * @return String
     * @throws Exception
     */
    public String generateVisualizationURL() throws Exception {
        CompositionService compositionService = (CompositionService) SpringUtil.getBean("compositionService");
        AuthenticationService authenticationService = (AuthenticationService) SpringUtil.getBean("authenticationService");
        String user=authenticationService.getUserCredential().getId();
        String workunitId = compositionService.getWorkunitId(this,user);
        Composition composition = HipieSingleton.getHipie().getComposition(
                authenticationService.getUserCredential().getId(), this.getCompositionName());
        if (workunitId != null) {
            String resultName = composition
                    .getVisualizationDDLs(
                            authenticationService.getUserCredential().getId(),
                            false).values().iterator().next().keySet().iterator().next();
            resultName = !StringUtils.substringBeforeLast(resultName, "admin").isEmpty()?
                    "admin" + StringUtils.substringAfterLast(resultName, "admin"):
                        resultName;
            StringBuilder url = new StringBuilder(getHpccConnection()
                    .getESPUrl()).append("WsWorkunits/WUResult.json?")
                    .append("Wuid=")
                    .append(compositionService.getWorkunitId(this,user))
                    .append("&ResultName=").append(resultName)
                    .append("&SuppressXmlSchema=true");
            return url.toString();

        } else {
            return null;
        }

    }    
}
