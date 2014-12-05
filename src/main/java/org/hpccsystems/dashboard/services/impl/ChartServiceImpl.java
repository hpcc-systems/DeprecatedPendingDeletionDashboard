package org.hpccsystems.dashboard.services.impl; 

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.dao.ChartDao;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.services.ChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

public class ChartServiceImpl implements ChartService {
    private static final Log LOG = LogFactory.getLog(GroupServiceImpl.class);
    private ChartDao chartDao;

    @Autowired
    public void setChartDao(ChartDao chartDao) {
        this.chartDao = chartDao;
    }

    @Override
    public int addPlugin(String name, String description, String configData,
            String userId, int category, boolean isPlugin) throws DataAccessException    {
        try {
            return chartDao.addPlugin(name, description, configData, userId,
                    category, isPlugin);
        } catch (DataAccessException ex) {
            LOG.error(Constants.EXCEPTION,    ex);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Integer, ChartDetails> getCharts() throws DataAccessException {
        Session session = Sessions.getCurrent();
        if (session == null || session.getAttribute(Constants.CHARTS) == null) {
            try {
                List<ChartDetails> list = chartDao.getCharts();
                Map<Integer, ChartDetails> charts = new LinkedHashMap<Integer, ChartDetails>();
                for (ChartDetails chartDetails : list) {
                    charts.put(chartDetails.getId(), chartDetails);
                }
                if (session != null) {
                    session.setAttribute(Constants.CHARTS, charts);
                } else {
                    return charts;
                }
            } catch (DataAccessException ex) {
                LOG.error(Constants.EXCEPTION, ex);
                throw ex;
            }
        }
        return (Map<Integer, ChartDetails>) session
                .getAttribute(Constants.CHARTS);
    }

    @Override
    public void deletePlugin(int pluginId) throws DataAccessException {
        try {
            chartDao.deletePlugin(pluginId);
        } catch (DataAccessException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
    }

    @Override
    public List<ChartDetails> getPlugins() throws DataAccessException {
        try {
            return chartDao.getPlugins();
        } catch (DataAccessException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
    }
}
