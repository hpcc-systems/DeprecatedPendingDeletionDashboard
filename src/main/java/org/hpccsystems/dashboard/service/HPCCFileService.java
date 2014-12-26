package org.hpccsystems.dashboard.service;

import java.util.List;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.LogicalFile;

public interface HPCCFileService {
    
    List<LogicalFile> getFiles(String scope, HPCCConnection hpccConnection);
    
    List<Field> getFields(String logicalFile, HPCCConnection hpccConnection);
}
