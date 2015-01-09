package org.hpccsystems.dashboard.manage.widget.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.StringFilter;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Popup;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class StringFilterContoller extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(StringFilterContoller.class);
    
    private StringFilter filter;

    @WireVariable
    AuthenticationService authenticationService;

    @WireVariable
    WSSQLService wssqlService;

    @Wire
    Listbox filterListBox;

    @Wire
    Button filtersSelectedBtn;

    private WidgetConfiguration widgetConfiguration;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        widgetConfiguration = (WidgetConfiguration) Executions.getCurrent().getAttribute(Constants.WIDGET_CONFIG);
        filter = (StringFilter) Executions.getCurrent().getAttribute(Constants.FILTER);
        
        List<String> valueList = null;
        
        valueList = wssqlService.getDistinctValues(filter, 
                widgetConfiguration.getDashboard().getHpccConnection(), 
                widgetConfiguration.getWidget().getLogicalFile(), 
                widgetConfiguration.getWidget().getFilters());
        
        ListModelList<String> values =  new ListModelList<String>(valueList);
        values.setMultiple(true);
        filterListBox.setModel(values);
     
    }

    @Listen("onClick = #filtersSelectedBtn")
    public void onfiltersSelected() {
        Set<Listitem> selectedValues = filterListBox.getSelectedItems();

        List<String> listOfLabels = new ArrayList<String>();
        
        selectedValues.forEach(value->{
        	String label = value.getLabel();
        	listOfLabels.add(label);
        });
       
       
       filter.setValues(new ArrayList<String>(listOfLabels));

        // Detaching the filter's popup window
        Popup popup = (Popup) this.getSelf().getParent().getParent();
        popup.close();

        widgetConfiguration.getComposer().drawChart();
    }

}
