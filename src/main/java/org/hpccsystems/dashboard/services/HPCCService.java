package org.hpccsystems.dashboard.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.xpath.XPathExpressionException;

import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.HpccConnection;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.TitleColumn;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.entity.XYModel;
import org.hpccsystems.dashboard.chart.tree.entity.Level;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.chart.tree.entity.TreeFilter;
import org.hpccsystems.dashboard.entity.FileMeta;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.xml.sax.SAXException;

public interface HPCCService {
     
    List<String> getClusters(HpccConnection hpccConnection) throws HpccConnectionException;
    
    /**
     * Fetches Column data type and column name of specified Logical file
     *  
     * @param fileName
     * @param hpccConnection
     * @return
     *     Map with Column name as Key and Data type as result
     * @throws SAXException 
     * @throws IOException 
     * @throws Exception
     */
    Set<Field> getColumns(final String fileName,
            final HpccConnection hpccConnection)
            throws HpccConnectionException,ParserConfigurationException, SAXException, IOException;
    
     /**
      * getChartData() is used the retrieve the ChartData details and render the D3 charts.
     * @param titleColumns 
     * @param chartParamsMap
     * @return List<BarChart>
     * @throws XPathExpressionException TODO
     * @throws Exception 
     * 
     */
    List<XYModel> getChartData(XYChartData data, List<TitleColumn> titleColumns)
            throws HpccConnectionException, ParserConfigurationException,
            SAXException, IOException, ServiceException, XPathExpressionException;
    
    /**
     * Function to fetch distinct values of a specified String field
     * All filter Conditions in ChartData Object are applied while retrieving distinct values  
     * @param fieldName
     *     Must be a STRING Field
     * @param chartData
     * @param applyFilter
     *     Decides whether to apply filters present in XYChartData object
     * @return
     *     List of distinct values as a list
     * @throws Exception
     */
    List<String> getDistinctValues(String fieldName, String dataSetName,
            ChartData chartData, Boolean applyFilter)
            throws HpccConnectionException,RemoteException;
    
    /**
     * Function to fetch Minimum and Maximum values of specified Numeric field
     * All filter Conditions in ChartData Object are applied while retrieving Minimum and Maximum values
     * @param fieldName
     *     Must be a NUMERIC field
     * @param chartData
     * @param applyFilter
     *     Decides whether to apply existing filters in XYChartdata object while Querying 
     * @return
     *     Minimum and Maximum values as values of the Map
     * @throws Exception
     */
    Map<Integer, BigDecimal> getMinMax(String fieldName, String dataSetName,
            ChartData chartData, Boolean applyFilter)
                    throws HpccConnectionException,RemoteException;
    
    /**
     * Retrieves data for Drawing table
     * 
     * @param data
     * @return
     * @throws Exception
     */
    Map<String, List<Attribute>> fetchTableData(TableData data,List<TitleColumn> titleColumns)
            throws HpccConnectionException, RemoteException;
    
    /**
     * Retrieves the files & directories in the specified scope
     * @param scope
     * @param hpccConnection
     * @return
     * @throws Exception
     */
    List<FileMeta> getFileList(String scope, HpccConnection hpccConnection) throws ServiceException, RemoteException ;

    /**
     * Retrieves values for columns in specified level of TreeData
     * 
     * @param treeData
     * @param level
     * @param filters
     *     Can be passed null if no filters are to be applied
     * @return
     *     List of 'Values of each column as a List'
     * @throws Exception 
     */
    List<List<String>> getRootValues(TreeData treeData, Level level, List<TreeFilter> filters)  throws HpccConnectionException, RemoteException;
    
    /**  Retrieves the files from roxie
     * @param hpccConnection
     * @return List<FileMeta>
     * @throws Exception
     */
    @Deprecated
    List<FileMeta> getRoxieFileList(HpccConnection hpccConnection) throws HpccConnectionException;
    
    /**  Retrieves published queries
     * @param hpccConnection
     * @return List<FileMeta>
     * @throws Exception
     */
    List<FileMeta> getQueries(HpccConnection hpccConnection,int category) throws HpccConnectionException;

}
