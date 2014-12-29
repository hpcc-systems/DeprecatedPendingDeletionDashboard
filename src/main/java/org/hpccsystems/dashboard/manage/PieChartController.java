package org.hpccsystems.dashboard.manage;

import java.util.HashMap;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Tabbox;

/**
 * EditChartController class is used to handle the edit page of the Dashboard
 * project and controller class for edit_portlet.zul file.
 * 
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class PieChartController extends SelectorComposer<Component> {
    private static final long serialVersionUID = 1L;
    
    @Wire
    private Tabbox measureTabbox;
        
    final Map<String, Object> parameters = new HashMap<String, Object>();
    
    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        return super.doBeforeCompose(page, parent, compInfo);
    }
    
    @Override
    public void doBeforeComposeChildren(Component comp) throws Exception {
        super.doBeforeComposeChildren(comp);
    }
    
    public boolean getShowGroupPanel() {
        return false;
    }
    
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        
    }    
    
}

