package org.hpccsystems.dashboard.service;

import org.hpcc.HIPIE.CompositionInstance;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;

public interface CompositionService {
    
    void createComposition(Dashboard dashboard, Widget widget);
    
    CompositionInstance runComposition(Dashboard dashboard);
    
    String getWorkunitId(Dashboard dashboard) throws Exception ;
}
