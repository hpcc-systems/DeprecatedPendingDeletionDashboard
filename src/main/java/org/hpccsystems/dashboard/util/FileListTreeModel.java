package org.hpccsystems.dashboard.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.entity.FileMeta;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.impl.HPCCServiceImpl;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.AbstractTreeModel;

public class FileListTreeModel extends AbstractTreeModel<FileMeta> {

	private static final long serialVersionUID = 1L;
	private static final  Log LOG = LogFactory.getLog(FileListTreeModel.class); 
	
	XYChartData chartData;
	private HPCCService hpccService = new HPCCServiceImpl();
	
	public FileListTreeModel(FileMeta fileMeta, XYChartData chartData) {
		super(fileMeta);
		this.chartData = chartData;
	}


	public FileMeta getChild(FileMeta parent, int index) {
		if(index < parent.getChildlist().size()) {
			FileMeta fileMeta = parent.getChildlist().get(index);
			return fileMeta;
		}
		else
			return null;
	}

	public int getChildCount(FileMeta parent){
		if(parent.getChildlist() == null) {
			try {
				parent.setChildlist(
						hpccService.getFileList(parent.getScope(), chartData.getHpccConnection())
							);
			} catch (Exception e) {				
					LOG.error("Exception while browsing files for selcted Scope", e);
					return 0;
			}
			
		}
		return parent.getChildlist().size();
	}

	public boolean isLeaf(FileMeta node) {
		return !node.isDirectory();
	}
	
}
