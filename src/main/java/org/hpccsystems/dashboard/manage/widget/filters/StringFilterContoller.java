package org.hpccsystems.dashboard.manage.widget.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.StringFilter;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Popup;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class StringFilterContoller extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(StringFilterContoller.class);
    
    private StringFilter filter;
    private Button doneButton;
    private ListModelList<String> listOfvalues;

    @WireVariable
    AuthenticationService authenticationService;

    @WireVariable
    WSSQLService wssqlService;

    @Wire
    Listbox filterListBox;

    @Wire
    Button filtersSelectedBtn;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        WidgetConfiguration widgetconfig = (WidgetConfiguration) Executions.getCurrent().getAttribute(Constants.WIDGET_CONFIG);
        filter = (StringFilter) Executions.getCurrent().getAttribute(Constants.FILTER);
        
        List<String> valueList = null;
        
        valueList = wssqlService.getDistinctValues(filter, 
                widgetconfig.getDashboard().getHpccConnection(), 
                widgetconfig.getWidget().getLogicalFile(), 
                widgetconfig.getWidget().getFilters());
        
        if (!valueList.isEmpty()) {
            listOfvalues = new ListModelList<String>(valueList);
            filterListBox.setModel(listOfvalues);
        }
    }

    @Listen("onClick = #filtersSelectedBtn")
    public void onfiltersSelected() {
        Set<String> selectedValues = listOfvalues.getSelection();
        
        // Check for no values selected
        if (selectedValues.isEmpty()) {
            Clients.showNotification(Labels.getLabel("noFilterareSelected"), "error", doneButton.getParent().getParent()
                    .getParent(), "middle_center", 3000, true);
            return;
        }
        filter.setValues(new ArrayList<String>(selectedValues));

        // Detaching the filter's popup window
        Popup popup = (Popup) this.getSelf().getParent().getParent();
        popup.close();

    }

}
