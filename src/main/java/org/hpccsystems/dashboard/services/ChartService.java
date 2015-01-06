package org.hpccsystems.dashboard.services;

import java.util.List;
import java.util.Map;

import org.hpccsystems.dashboard.entity.ChartDetails;
import org.springframework.dao.DataAccessException;

/**
 * Service to process with Plugin
 *
 */
public interface ChartService {
    
    /**
     * Adds new plugin data into DB
     * @param name
     * @param imageUrl
     * @param configData
     * @param userId
     */
    int addPlugin(String name,String description,String configData,String userId,int category,boolean isPlugin) throws DataAccessException;
    /**
     * Get available charts from DB for an user
     * @param userId
     * @return List<ChartDetails>
     */
    Map<Integer, ChartDetails> getCharts() throws DataAccessException;
    
    /**
     * Service to delete plugin
     * @param pluginId
     */
    void deletePlugin(int pluginId) throws DataAccessException;
    
    /**Returns plugin list
     * @return List<ChartDetails>
     */
    List<ChartDetails> getPlugins() throws DataAccessException;
    
    

}
