package org.hpccsystems.dashboard.service;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;

public interface CompositionService {
    
    void createComposition(Dashboard dashboard, Widget widget);
    
    void runComposition(Dashboard dashboard);

}
