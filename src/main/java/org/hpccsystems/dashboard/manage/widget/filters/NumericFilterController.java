package org.hpccsystems.dashboard.manage.widget.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

public class NumericFilterController extends SelectorComposer<Component> {
	 private static final long serialVersionUID = 1L;
	 private static final  Log LOG = LogFactory.getLog(NumericFilterController.class);
	 
	 @Wire
	    Label minimumLabel;
	    
	    @Wire
	    Label maximumLabel;
	    
	    @Wire
	    Div sliderDiv;
	    
	    @Wire
	    Button filtersSelectedBtn;
	    @Override
	    public void doAfterCompose(Component comp) throws Exception {
	        super.doAfterCompose(comp);
	        
	    
	    }

}
