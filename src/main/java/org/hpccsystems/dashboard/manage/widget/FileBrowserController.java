package org.hpccsystems.dashboard.manage.widget;

import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.authentication.LoginController;
import org.hpccsystems.dashboard.entity.widget.LogicalFile;
import org.hpccsystems.dashboard.model.widget.LogicalFileTreeModel;
import org.hpccsystems.dashboard.service.HPCCFileService;
import org.hpccsystems.dashboard.util.HipieSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class FileBrowserController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBrowserController.class);
    
    @WireVariable
    private HPCCFileService hpccFileService;
    @Wire
    private Tree fileTree;
    @Wire
    private Textbox selectedFile;
    
    private HPCCConnection hpccConnection;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        hpccConnection = (HPCCConnection)Executions.getCurrent().getArg().get(Constants.HPCC_CONNECTION);
        hpccConnection = HipieSingleton.getHipie()
                .getHpccManager().getConnection("dev-dashboard");
        constructFileBrowser();
    }

    /**
     * Constructs file browser for the selected Hpcc cluster
     * @return boolean
     */
    private void constructFileBrowser() {
        LogicalFile logicalFile = new LogicalFile();
        logicalFile.setScope("");
        logicalFile.setFileName("ROOT");
        logicalFile.setDirectory(true);
        logicalFile.setChildlist(hpccFileService.getFiles(logicalFile.getScope(),
                hpccConnection));

        LogicalFileTreeModel fileTreeModel = new LogicalFileTreeModel(logicalFile,
                hpccConnection);

        fileTree.setModel(fileTreeModel);

        fileTree.addEventListener(Events.ON_SELECT, new EventListener<Event>() {

            @Override
            public void onEvent(Event event) {
                Tree targetTree = (Tree) event.getTarget();
                selectedFile.setText("");
                addSelectedFile(targetTree);
            }
        });
    }
    
    /**
     * Adds the selected file to UI
     * @param targetTree
     */
    private void addSelectedFile(Tree targetTree) {
        for (Treeitem treeitem : targetTree.getSelectedItems()) {
            if (treeitem.getLastChild() instanceof Treerow) {
                Treerow treerow = (Treerow) treeitem.getLastChild();
                Treecell treecell = (Treecell) treerow.getLastChild();
                Label label = (Label) treecell.getLastChild();
                String logicalFileName = "~" + label.getValue();
                selectedFile.setText(logicalFileName);
                fetchFields(logicalFileName);
            } else {
                if (treeitem.isOpen()) {
                    treeitem.setOpen(false);
                } else {
                    treeitem.setOpen(true);
                }
                treeitem.setSelected(false);
            }
        }
    }
    
    /**
     * Fetches fields for the selected logical file
     * @param logicalFileName
     */
    private void fetchFields(String logicalFileName) {

        String fieldseparator = null;
        RecordInstance recordInstance;
        try {
            recordInstance = hpccConnection.getDatasetFields(logicalFileName, fieldseparator);
            //TODO:Need to set the fields into composition's plugin file
            /*plugin.getContractInstance().setProperty("Structure",
                    recordInstance);*/
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
        }
    }
}
