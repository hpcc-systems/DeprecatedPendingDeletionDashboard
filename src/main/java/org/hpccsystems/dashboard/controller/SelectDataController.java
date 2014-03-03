package org.hpccsystems.dashboard.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.FileMeta;
import org.hpccsystems.dashboard.entity.chart.HpccConnection;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.util.FileListTreeModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Window;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SelectDataController extends SelectorComposer<Component>{

	private static final long serialVersionUID = 1L;
	private static final  Log LOG = LogFactory.getLog(SelectDataController.class);
	@WireVariable
	HPCCService hpccService;

	@Wire
	Textbox username;
	@Wire
	Textbox password;
	@Wire
	Textbox URL;
	@Wire
	Checkbox sslCheckbox;
	
	@Wire
	Tree tree;
	@Wire
	Label selectedFileName;
	
	private XYChartData chartData;
	private Window parentWindow;
	
	private static final String KEY_isSelected = "isFileSelected";
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		chartData = (XYChartData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
		 
		parentWindow = (Window) Executions.getCurrent().getAttribute(Constants.PARENT);
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Checkbox" + sslCheckbox);
		}
	}
	
	@Listen("onClick = #submitBtn")
	public void onFormSubmit(Event event) {
		HpccConnection hpccConnection = new HpccConnection();
		hpccConnection.setUsername(username.getValue());
		hpccConnection.setPassword(password.getValue());
		hpccConnection.setHostIp(URL.getValue());
		hpccConnection.setIsSSL(sslCheckbox.isChecked());
		chartData.setHpccConnection(hpccConnection);
		
		FileMeta fileMeta = new FileMeta();
		fileMeta.setScope("");
		fileMeta.setFileName("ROOT");
		fileMeta.setIsDirectory(true);
		try {
			fileMeta.setChildlist(
					hpccService.getFileList(fileMeta.getScope(), chartData.getHpccConnection())
				);
		} catch (Exception e) {
			Clients.showNotification("Please check provided HPCC Credentials", "error", username.getParent().getParent(), "after_center", 3000, true);
			LOG.error("Exception while browsing files for selcted Scope", e);
			return;
		}
		FileListTreeModel fileListTreeModel = new FileListTreeModel(fileMeta, chartData);
		tree.setModel(fileListTreeModel);
		tree.setVisible(true);
		
		tree.addEventListener(Events.ON_SELECT, new EventListener<Event>() {

			public void onEvent(Event event) throws Exception {
				Tree tree = (Tree) event.getTarget();
				Treeitem treeitem = tree.getSelectedItem();
				if(treeitem.getLastChild() instanceof Treerow){
					Treerow treerow = (Treerow) treeitem.getLastChild();
					Treecell treecell = (Treecell) treerow.getLastChild();
					Label label = (Label) treecell.getLastChild();
					selectedFileName.setValue(label.getValue());
					selectedFileName.setZclass("h4");
					selectedFileName.setAttribute(KEY_isSelected, true);
				}
			}
		});
		
		Button btn = (Button) event.getTarget();
		btn.setDisabled(true);
	}
	
	@Listen("onClick = #visualizeBtn")
	public void onVisualizeButtonClick(Event event) {
		if(selectedFileName.getAttribute(KEY_isSelected) != null && 
				(Boolean) selectedFileName.getAttribute(KEY_isSelected)) {
			chartData.setFileName(selectedFileName.getValue());
			Events.sendEvent("onIncludeDetach", parentWindow, Constants.EDIT_WINDOW_TYPE_DATA_SELECTION);
		} else {
			Clients.showNotification("Please choose a file", "warning", tree, "middle_center", 2000, false);
		}
	}
}
