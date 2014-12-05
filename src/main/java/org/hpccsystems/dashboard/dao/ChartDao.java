package org.hpccsystems.dashboard.dao;

import java.util.List;

import org.hpccsystems.dashboard.entity.ChartDetails;
import org.springframework.dao.DataAccessException;

public interface ChartDao {

    int addPlugin(String name, String description, String configData,
            String userId, int category, boolean isPlugin) throws DataAccessException;

    List<ChartDetails> getCharts() throws DataAccessException;

    void deletePlugin(int pluginId) throws DataAccessException;

    List<ChartDetails> getPlugins() throws DataAccessException;

}
