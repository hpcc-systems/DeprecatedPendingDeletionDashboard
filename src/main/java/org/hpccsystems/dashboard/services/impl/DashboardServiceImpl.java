package org.hpccsystems.dashboard.services.impl; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.InputParam;
import org.hpccsystems.dashboard.chart.utils.XMLConverter;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * DashboardServiceImpl is implementaiton class for DashboardService.
 *
 */
@Service("dashboardService") 
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DashboardServiceImpl implements DashboardService {
    
    private static final  Log LOG = LogFactory.getLog(DashboardServiceImpl.class); 
    
    private DashboardDao dashboardDao;

    
    /**
     * Retrieving DashboardMenuPages details from dashboard_details table.
     * @param viewModel
     * @return List<SidebarPage>
     */

    public List<Dashboard> retrieveDashboardMenuPages(final String applicationId,String userId,List<String> dashboardIdList, String sourceId)throws DataAccessException {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling 'retrieveDashboardMenuPages' in DashboardServiceImpl");
        }
        
        final HashMap<String,Dashboard> pageMap = new LinkedHashMap<String,Dashboard>();
        List<Dashboard> menuList = null;
        try {
            menuList = dashboardDao.fetchDashboardDetails(applicationId,userId,dashboardIdList, sourceId);
        }
        catch(final DataAccessException ex) {
            throw ex;
        }
        
        final String fnName = "fn";
        if(menuList != null) {
        for (int i = 0; i < menuList.size(); i++) {
            final Dashboard entry = (Dashboard)menuList.get(i);
            final int j = i + 1;                                    
            final String fnNameStr = fnName + j;
            if (LOG.isDebugEnabled()) {
                LOG.debug("dasboardname:"+entry.getName());
            }
            pageMap.put(fnNameStr, entry);
        }
        }
        return new ArrayList<Dashboard>(pageMap.values());
    }

    public DashboardDao getDashboardDao() {
        return dashboardDao;
    }

    public void setDashboardDao(final DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public void addDashboardDetails(final Dashboard dashboard,
            final String applicationId, final String sourceId, final String userId)
            throws DataAccessException {
        try {
            dashboardDao.addDashboardDetails(dashboard,applicationId, sourceId,userId);
        }
        catch(DataAccessException ex) {
            LOG.error(Constants.EXCEPTION,ex);
            throw ex;
        }
    }
    
    /* 
     * service to delete a dashboard
     *
     */
    public int deleteDashboard(Integer dashboardId, UserCredential user) throws DataAccessException{
        try {
            return dashboardDao.deleteDashboard(dashboardId,user);
        } catch (DataAccessException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        }        
    }    
    
    public void updateSidebarDetails(List<Integer> dashboardIdList)throws DataAccessException{
        try{
            dashboardDao.updateSidebarDetails(dashboardIdList);
        }catch(DataAccessException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        }
        
    }
    
    @Override
    public void updateDashboard(Dashboard dashboard) throws DataAccessException {
        try {
            dashboardDao.updateDashboard(dashboard);
        } catch (DataAccessException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        }
        
    }

    @Override
    public List<Dashboard> retrieveDashboards(String applicationId,
            String userId, List<String> groupIds) {
        return dashboardDao.retrieveDashboards(applicationId,userId,groupIds);
    }

    @Override
    public List<Dashboard> getAllDashboard(String applnId) {
        return dashboardDao.getAllDashboard(applnId);
    }

    @Override
    public Dashboard getDashboard(Integer dashboardId) {
        return dashboardDao.getDashboard(dashboardId);
    }

    @Override
    public List<String> getDashboardName(String userId, String applicationId) {
        return dashboardDao.getDashboardName(userId, applicationId);
    }

    @Override
    public void saveFilterOrder(Integer dashboardId, String order) {
        dashboardDao.saveFilterOrder(dashboardId, order);
    }

    @Override
    public List<String> getFilterOrder(Integer dashboardId) {
        String order = dashboardDao.getFilterOrder(dashboardId);
        return order != null ? Arrays.asList(StringUtils.split(order, ",")) : null;
    }

    @Override
    public void addOrUpdateCommonInput(Integer dashboardId, String userId,
            List<InputParam> commonInputs) throws JAXBException {
        
        try {     
            String commonInputText = null;
            if (commonInputs != null) {
                commonInputText = XMLConverter.makeCommonInputXML(commonInputs);
            }
            dashboardDao.addOrUpdateCommonInput(dashboardId, commonInputText,userId);
        } catch (DataAccessException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
    
        
    }

}
