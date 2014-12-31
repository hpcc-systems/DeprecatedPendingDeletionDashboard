package org.hpccsystems.dashboard.manage.widget;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.LogicalFile;
import org.hpccsystems.dashboard.service.HPCCFileService;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.AbstractTreeModel;

public class LogicalFileTreeModel extends AbstractTreeModel<LogicalFile> {

    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(LogicalFileTreeModel.class);

    private HPCCConnection hpccConnection;
    private HPCCFileService hpccFileService = (HPCCFileService) SpringUtil .getBean("hpccFileService");

    public LogicalFileTreeModel(LogicalFile logicalFile, HPCCConnection hpccConnection) {
        super(logicalFile);
        super.setMultiple(true);
        this.hpccConnection = hpccConnection;
    }

    @Override
    public LogicalFile getChild(LogicalFile parent, int index) {
        if (index < parent.getChildlist().size()) {
            return parent.getChildlist().get(index);
        } else {
            return null;
        }
    }

    @Override
    public int getChildCount(LogicalFile parent) {
        if (parent.getChildlist() == null) {
            try {
                parent.setChildlist(hpccFileService.getFiles(parent.getScope(),
                        hpccConnection));
            } catch (Exception e) {
                LOG.error(Constants.EXCEPTION, e);
                return 0;
            }

        }
        return parent.getChildlist().size();
    }

    @Override
    public boolean isLeaf(LogicalFile node) {
         return !node.isDirectory();
    }

   

}
