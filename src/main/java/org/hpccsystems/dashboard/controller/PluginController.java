package org.hpccsystems.dashboard.controller;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.services.ChartService;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

/**
 * PluginController is responsible for handle the adding new widget plugins and
 * controller class for plugin.zul.
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class PluginController extends SelectorComposer<Window> {
    
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(PluginController.class);
    
    @Wire
    private Include addHolder;
    @Wire
    private Include helpHolder;
    @Wire
    private Rows pluginRows;
    
    @WireVariable
    ChartService chartService;
    
    @Override
    public void doAfterCompose(Window window) throws Exception {
        super.doAfterCompose(window);
        try {
            List<ChartDetails> plugins = chartService.getPlugins();
            for (ChartDetails chartDetails : plugins) {
                createPluginRows(chartDetails);
            }
        } catch (Exception ex) {
            LOG.error(Constants.EXCEPTION, ex);
        }
        // An event listener to update the plugin list when new plugin is added
        this.getSelf().addEventListener("onPluginAdded",
                new EventListener<Event>() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        ChartDetails chartDetails = (ChartDetails) event.getData();
                        try {
                            chartService.getCharts().put(chartDetails.getId(), chartDetails);
                        } catch (Exception ex) {
                            LOG.error(Constants.EXCEPTION, ex);
                        }
                        createPluginRows(chartDetails);
                    }
                });
    }
    
    private void createPluginRows(ChartDetails plugin) {
        Label name;
        Label description;
        Button editBtn;
        Button deleteBtn;
        Row row;
        row = new Row();
        name = new Label();
        name.setValue(plugin.getName());
        name.setZclass("pluginName");
        description = new Label();
        editBtn = new Button();
        deleteBtn = new Button();
        description.setValue(plugin.getDescription());
        editBtn.setLabel("Edit");
        editBtn.setZclass("btn btn-xs btn-primary");
        deleteBtn.setLabel("Delete");
        deleteBtn.setZclass("btn btn-xs btn-danger");
        row.appendChild(name);
        row.appendChild(description);
        row.appendChild(editBtn);
        row.appendChild(deleteBtn);
        row.setSclass("pluginList");
        row.setParent(pluginRows);
        
        //Hiding Buttons untill functionality is implemented
        editBtn.setVisible(false);
        deleteBtn.setVisible(false);
    }

    @Listen("onClick = #help")
    public void onAddPlugin(final Event event) {
        helpHolder.setSrc("plugin_howto.zul");
    }
    
    @Listen("onClick = #addPlugin")
    public void onPluginHelp(final Event event) {
        addHolder.setSrc("plugin_config.zul");
    }
}
