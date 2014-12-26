package org.hpccsystems.dashboard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpcc.HIPIE.utils.HPCCLogicalFile;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.LogicalFile;
import org.hpccsystems.dashboard.service.HPCCFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HPCCFileServiceImpl implements HPCCFileService{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HPCCFileServiceImpl.class);
    
    private LogicalFile settingScopeBasedOnLength(String scope, LogicalFile logicalFile, HPCCLogicalFile hpccLogicalFile) {
        if (scope.length() > 0) {
            logicalFile.setScope(scope + "::" + hpccLogicalFile.getFileName());
        } else {
            logicalFile.setScope("~" + hpccLogicalFile.getFileName());
        }
        return logicalFile;
    }

    @Override
    public List<LogicalFile> getFiles(String scope, HPCCConnection hpccConnection) {
        List<LogicalFile> results = new ArrayList<LogicalFile>();

        List<HPCCLogicalFile> resultsArray;
        try {
            resultsArray = hpccConnection.getFilenames(scope);
            LogicalFile logicalFile;
            for (HPCCLogicalFile hpccLogicalFile : resultsArray) {
                LOGGER.debug("File - {}", hpccLogicalFile);

                logicalFile = new LogicalFile();
                if (hpccLogicalFile.isDirectory()) {
                    logicalFile.setIsDirectory(true);
                    logicalFile.setFileName(hpccLogicalFile.getFileName());
                    logicalFile = settingScopeBasedOnLength(scope, logicalFile, hpccLogicalFile);
                } else {
                    logicalFile.setIsDirectory(false);
                    logicalFile.setFileName(hpccLogicalFile.getFileName());
                }
                results.add(logicalFile);
            }
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
        }

        return results;
    }

    @Override
    public List<Field> getFields(String logicalFile, HPCCConnection hpccConnection) {

        RecordInstance recordInstance;
        List<Field> fieldList=new ArrayList<Field>();
        try {
            recordInstance = hpccConnection.getDatasetFields(logicalFile, null);
            recordInstance.stream().forEach(fieldInstance -> 
                fieldList.add(new Field(fieldInstance.getName(), fieldInstance.getType()))
            );
            
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
            // TODO:thow exception
        }
        return fieldList;
    }

}
