package org.hpccsystems.dashboard.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Textbox;

public class GroupManageController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final  Log LOG = LogFactory.getLog(GroupManageController.class);
    
    
    @Wire
    private Textbox oldPwd;
    @Wire
    private Textbox newPwd;
    @Wire
    private Textbox confirmPwd;
    @Wire
    private Panel pwdPanel;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
    }

    @Listen("onClick = #changePwdBtn")
    public void onChangePassword(){
        boolean validPwd = validatePassword();
    }

    private boolean validatePassword() {
        
        //validates old password
        
        
         if(!newPwd.getText().equals(confirmPwd.getText())){
            Clients.showNotification(
                    Labels.getLabel("passwordNotMatched"),
                    Clients.NOTIFICATION_TYPE_ERROR, pwdPanel, Constants.POSITION_CENTER, 3000, true);
            return false;
        }
        return false;
    }
}
