package org.hpccsystems.dashboard.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hpccsystems.dashboard.entity.FileMeta;
import org.hpccsystems.dashboard.entity.chart.HpccConnection;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.XYModel;

public interface HPCCService {
	 
	/**
	 * Fetches Column data type and column name of specified Logical file
	 *  
	 * @param fileName
	 * @param hpccConnection
	 * @return
	 * 	Map with Column name as Key and Data type as result
	 * @throws Exception
	 */
	Map<String,String> getColumnSchema(final String fileName, final HpccConnection hpccConnection) throws Exception;
	
	 /**
	  * getChartData() is used the retrieve the ChartData details and render the D3 charts.
	 * @param chartParamsMap
	 * @return List<BarChart>
	 * @throws Exception 
	 * 
	 */
	List<XYModel> getChartData(XYChartData data) throws Exception;
	
	 
	 /**
	  * Fetches list of distinct values from specified column
	 * @param filterColumn
	 * @return List<String>
	 */
	List<String> fetchFilterData(XYChartData data) throws Exception ;
	
	/**
	 * Fetches maximum and minimum values of the Numeric filter
	 * @param chartData
	 * @return
	 * 	a map with minimum and maximum values
	 */
	Map<Integer,Integer> fetchFilterMinMax(XYChartData chartData) throws Exception ;
	
	/**
	 * fetchTableData() is used to retrieve the Column values from HPCC systems 
	 * to construct Table Widget.
	 * @param data
	 * @return
	 * @throws Exception
	 */
	LinkedHashMap<String, List<String>> fetchTableData(XYChartData data) throws Exception;
	
	
	/**
	 * Retrieves the files & directories in the specified scope
	 * @param scope
	 * @param hpccConnection
	 * @return
	 * @throws Exception
	 */
	List<FileMeta> getFileList(String scope, HpccConnection hpccConnection) throws Exception;
}
