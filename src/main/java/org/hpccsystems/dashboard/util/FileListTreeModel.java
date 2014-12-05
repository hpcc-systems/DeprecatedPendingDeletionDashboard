package org.hpccsystems.dashboard.util;

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.FileMeta;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.impl.HPCCServiceImpl;
import org.zkoss.zul.AbstractTreeModel;

public class FileListTreeModel extends AbstractTreeModel<FileMeta> {

    private static final long serialVersionUID = 1L;
    private static final  Log LOG = LogFactory.getLog(FileListTreeModel.class); 
    
    ChartData chartData;
    private HPCCService hpccService = new HPCCServiceImpl();
    
    public FileListTreeModel(FileMeta fileMeta, ChartData chartData) {
        super(fileMeta);
        super.setMultiple(true);
        this.chartData = chartData;
    }


    public FileMeta getChild(FileMeta parent, int index) {
        if(index < parent.getChildlist().size()) {
            return parent.getChildlist().get(index);
        } else{
            return null;
        }    
    }

    public int getChildCount(FileMeta parent){
        if(parent.getChildlist() == null) {
            try {
                parent.setChildlist(
                        hpccService.getFileList(parent.getScope(), chartData.getHpccConnection())
                            );
            } catch (Exception e) {                
                    LOG.error(Constants.EXCEPTION, e);
                    return 0;
            }
            
        }
        return parent.getChildlist().size();
    }

    public boolean isLeaf(FileMeta node) {
        return !node.isDirectory();
    }
    
}
