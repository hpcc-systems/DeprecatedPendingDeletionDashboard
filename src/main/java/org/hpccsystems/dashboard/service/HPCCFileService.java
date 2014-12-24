package org.hpccsystems.dashboard.service;

import java.util.List;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.LogicalFile;

public interface HPCCFileService {
    
    List<LogicalFile> getFiles(String scope, HPCCConnection hpccConnection);
    
    List<Field> getFields(String logicalFile, HPCCConnection hpccConnection);
}
