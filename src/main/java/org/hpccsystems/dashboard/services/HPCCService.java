package org.hpccsystems.dashboard.services;

import java.util.List;
import java.util.Map;

import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.XYModel;

public interface HPCCService {
	 /**
	  * getColumnSchema() is used to retrieve the ColumnSchema details from HPCC systems 
	  * to pass column data details to Edit Chart page to generate the D3 charts. 
	 * @param sql
	 * @param userName
	 * @param password
	 * @param url
	 * @return Map<String,String>
	 */
	Map<String,String> getColumnSchema(String sql,String userName,String password,String url);
	
	 /**
	  * getChartData() is used the retrieve the ChartData details and render the D3 charts.
	 * @param chartParamsMap
	 * @return List<BarChart>
	 * 
	 */
	List<XYModel> getChartData(XYChartData data);
	
	 
	 /**
	 * @param filterColumn
	 * @return List<String>
	 */
	List<String> fetchFilterData(XYChartData data) throws Exception;
}
