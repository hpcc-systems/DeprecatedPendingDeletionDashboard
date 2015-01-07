package org.hpccsystems.dashboard.service;

import org.hpcc.HIPIE.CompositionInstance;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;

public interface CompositionService {
    
    void createComposition(Dashboard dashboard, Widget widget,String user) throws Exception;
    
    CompositionInstance runComposition(Dashboard dashboard,String user) throws Exception;
    
    String getWorkunitId(Dashboard dashboard,String user) throws Exception ;

    void updateComposition(Dashboard dashboard, Widget widget,String user);
}
