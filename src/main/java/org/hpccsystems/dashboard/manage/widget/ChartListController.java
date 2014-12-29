package org.hpccsystems.dashboard.manage.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.CHART_TYPES;
import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.entity.widget.charts.Pie;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Image;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Vlayout;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ChartListController extends SelectorComposer<Grid>{
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChartListController.class);
    
    private WidgetConfiguration widgetConfiguration;
    
    @Override
    public void doAfterCompose(Grid grid) throws Exception {
        super.doAfterCompose(grid);
        
        widgetConfiguration = (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        
        
        List<List<ChartConfiguration>> configurationList = new ArrayList<List<ChartConfiguration>>();
        
        Iterator<ChartConfiguration> iterator = Constants.CHART_CONFIGURATIONS.values().iterator();
        while (iterator.hasNext()) {
            List<ChartConfiguration> list = new ArrayList<ChartConfiguration>();
            for (int i = 0; i < 4; i++) {
                if(iterator.hasNext()) {
                    list.add(iterator.next());
                }
            }
        }
        
        
        ListModelList<List<ChartConfiguration>> configurations = new ListModelList<List<ChartConfiguration>>(configurationList);
        grid.setModel(configurations);
        
        grid.setRowRenderer( new RowRenderer<List<ChartConfiguration>>() {

            @Override
            public void render(Row row, List<ChartConfiguration> configs, int i) throws Exception {
                populateRow(row, configs);
            }


        });
    }
    
    private void populateRow(Row row, List<ChartConfiguration> configs) {
        configs.forEach(configuration -> {
            Vlayout vlayout = new Vlayout();
            Image image = new Image(configuration.getStaticImage());            
            row.appendChild(vlayout);
        });
    }
    
    @Listen("onClick = #add")
    public void addChart() {
        widgetConfiguration.setChartConfiguration(Constants.CHART_CONFIGURATIONS.get(CHART_TYPES.PIE));
        Events.postEvent(WidgetConfiguration.ON_CHART_TYPE_SELECT, widgetConfiguration.getHolder(), null);
    }
}
