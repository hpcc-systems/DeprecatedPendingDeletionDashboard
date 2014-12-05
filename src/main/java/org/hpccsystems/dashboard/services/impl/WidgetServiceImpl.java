package org.hpccsystems.dashboard.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.cluster.ClusterData;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.HpccConnection;
import org.hpccsystems.dashboard.chart.entity.ScoredSearchData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.TextData;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.gauge.GaugeChartData;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.chart.utils.XMLConverter;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.dao.WidgetDao;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.hpccsystems.dashboard.util.EncryptDecrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Service class to define Widget related services
 *
 */
@Service("widgetService") 
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WidgetServiceImpl implements WidgetService {
    private static final  Log LOG = LogFactory.getLog(WidgetServiceImpl.class); 
    WidgetDao widgetDao;
    ChartService chartService;
    
    @Autowired
    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }

    @Autowired
    public void setWidgetDao(WidgetDao widgetDao) {
        this.widgetDao = widgetDao;
    }
    
    public void addWidgetDetails(Integer dashboardId,List<Portlet> portlets) throws DataAccessException{
        try
        {
        widgetDao.addWidgetDetails(dashboardId,portlets);
        }catch(DataAccessException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
        
    }
    
    public List<Portlet> retriveWidgetDetails(Integer dashboardId) throws DataAccessException {
        try {
            //Making Objects from XML
            List<Portlet> portlets = widgetDao.retriveWidgetDetails(dashboardId);
            for (Portlet portlet : portlets) {
                if(portlet.getWidgetState().equals(Constants.STATE_LIVE_CHART)) {
                    if(Constants.CATEGORY_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                        portlet.setChartData(
                                XMLConverter.makeTableDataObject(portlet.getChartDataXML()));
                        
                    } else if(Constants.CATEGORY_HIERARCHY == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                        portlet.setChartData(
                                XMLConverter.makeTreeDataObject(portlet.getChartDataXML()));
                    } else if(Constants.CATEGORY_GAUGE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                        portlet.setChartData(
                                XMLConverter.makeGaugeChartDataObject(portlet.getChartDataXML()));
                    } else if(Constants.CATEGORY_TEXT_EDITOR == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    	ChartData chartData = new TextData();
                    	((TextData)chartData).setHtmlText(portlet.getChartDataXML());
                    	portlet.setChartData(chartData);
                    }else if(Constants.CATEGORY_CLUSTER == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    	portlet.setChartData(
                                XMLConverter.makeClusterDataObject(portlet.getChartDataXML()));
                    }else if(Constants.CATEGORY_ADVANCED_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    	portlet.setChartData(
                    			XMLConverter.makeScoredSearchDataObject(portlet.getChartDataXML()));
                    }else {
                        portlet.setChartData(
                                XMLConverter.makeXYChartDataObject(portlet.getChartDataXML()));
                    }
                }
            }
            
            return portlets;
        } catch(DataAccessException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION, e);
            // Temporarily thrown Data Excess Exception
            throw new DataAccessException("XML Parsing failed") {
                private static final long serialVersionUID = 1L;
            };
        }
    }

    @Override
    public void deleteWidget(Integer portletId) throws DataAccessException {
        try    {
            widgetDao.deleteWidget(portletId);
        }catch(DataAccessException ex)    {
            LOG.error("DataAccessException in deleteWidgets() in WidgetServiceImpl", ex);
            throw ex;
        }
    }
    
    @Override
    public void updateWidgetSequence(Dashboard dashboard) throws DataAccessException {
        try {
            widgetDao.updateWidgetSequence(dashboard.getDashboardId(),dashboard.getPortletList());
        }catch(DataAccessException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
        
    }

    @Override
    public void updateWidget(Portlet portlet) throws DataAccessException,
            JAXBException, EncryptDecryptException {
        
        try {
            // Converting Java Objects to XML
            if(portlet.getWidgetState().equals(Constants.STATE_LIVE_CHART)) {
                if(Constants.CATEGORY_TABLE== chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                    portlet.setChartDataXML(
                            XMLConverter.makeTableDataXML(
                                    (TableData) portlet.getChartData())
                            );                
                } else if(Constants.CATEGORY_HIERARCHY == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                    portlet.setChartDataXML(
                            XMLConverter.makeTreeDataXML(
                                    (TreeData) portlet.getChartData())
                            );
                } else if(Constants.CATEGORY_TEXT_EDITOR == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    portlet.setChartDataXML(
                                    ((TextData) portlet.getChartData()).getHtmlText());
                } else if(Constants.CATEGORY_GAUGE == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                    portlet.setChartDataXML(
                            XMLConverter.makeGaugeChartDataXML(
                                    (GaugeChartData) portlet.getChartData())  );
                }else if(Constants.CATEGORY_CLUSTER == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                	 portlet.setChartDataXML(
                             XMLConverter.makeClusterChartDataXML(
                                     (ClusterData) portlet.getChartData()));
                } else if(Constants.CATEGORY_ADVANCED_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                	portlet.setChartDataXML(
                			XMLConverter.makeScoredSearchDataXML((ScoredSearchData) portlet.getChartData()));
                }else {
                    //For Pie/Line/Bar charts
                    portlet.setChartDataXML(
                            XMLConverter.makeXYChartDataXML(
                                    (XYChartData) portlet.getChartData())
                            );
                }
            }
        } catch (DataAccessException e) {
            LOG.error("DataAccessException while updating widget"+e);
            throw e;
        }catch(JAXBException ex){
            LOG.error("JAXBException while updating widget"+ex);
            throw ex;
        }
        
        try    {
            widgetDao.updateWidget(portlet);
        } catch(DataAccessException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
        
    }

    @Override
    public void updateWidgetTitle(Portlet portlet)throws DataAccessException {
        try    {
            widgetDao.updateWidgetTitle(portlet);
        }catch(DataAccessException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }        
    }

    @Override
    public Integer addWidget(Integer dashboardId, Portlet portlet,
            Integer sequence) throws JAXBException, DataAccessException, EncryptDecryptException {
        try {
            // Converting Java Objects to XML
            if (portlet.getWidgetState().equals(Constants.STATE_LIVE_CHART)) {
                if (Constants.CATEGORY_TABLE== chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                    portlet.setChartDataXML(XMLConverter
                            .makeTableDataXML((TableData) portlet.getChartData()));
                } else if (Constants.CATEGORY_HIERARCHY == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                    portlet.setChartDataXML(XMLConverter
                            .makeTreeDataXML((TreeData) portlet.getChartData()));
                }else if(Constants.CATEGORY_TEXT_EDITOR == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                	portlet.setChartDataXML(((TextData) portlet.getChartData()).getHtmlText());
                } else if(Constants.CATEGORY_ADVANCED_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                	portlet.setChartDataXML(
                			XMLConverter.makeScoredSearchDataXML((ScoredSearchData) portlet.getChartData()));
                }else {
                    // For Pie/Line/Bar charts
                    portlet.setChartDataXML(XMLConverter
                            .makeXYChartDataXML((XYChartData) portlet
                                    .getChartData()));
                }
            }
        } catch(JAXBException ex){
            throw ex;
        }
        try {
            return widgetDao.addWidget(dashboardId, portlet, sequence);
        } catch (DataAccessException ex) {
            LOG.error(Constants.EXCEPTION,ex);
            throw ex;
        }
    }

    @Override
    public int updateHpccPassword(List<Dashboard> dashboards,
            HpccConnection hpccConnection, String password) throws EncryptDecryptException {
        EncryptDecrypt encryptor = new EncryptDecrypt("");
        
        List<Integer> dashboardIds = new ArrayList<Integer>();
        for (Dashboard dashboard : dashboards) {
            if(dashboard.getRole().equals(Constants.ROLE_ADMIN) || 
                    dashboard.getRole().equals(Constants.ROLE_CONTRIBUTOR)) {
                dashboardIds.add(dashboard.getDashboardId());
            }
        }
        
        //TODO: Consider maximum IN clause limit for mysql, default is 1048576
        return 
        widgetDao.updateHpccPassword(dashboardIds, hpccConnection.getHostIp(), hpccConnection.getUsername(), encryptor.encrypt(password));
    }

}
