package org.hpccsystems.dashboard.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.HpccConnection;
import org.hpccsystems.dashboard.chart.entity.ScoredSearchData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.entity.XYModel;
import org.hpccsystems.dashboard.chart.tree.entity.Level;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.chart.tree.entity.TreeFilter;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.xml.sax.SAXException;

public interface HPCCQueryService {
	
	Set<Field> getColumns(String queryName, HpccConnection hpccConnection,
			boolean isGenericQuery, String inputParamQuery) throws IOException, ParserConfigurationException, SAXException, URISyntaxException, HpccConnectionException ;
	
	QuerySchema getQuerySchema(String queryName, HpccConnection hpccConnection,boolean isGenericQuery,String inputParamQuery) throws Exception;

    List<XYModel> getChartData(XYChartData data) throws HpccConnectionException, NumberFormatException, XPathExpressionException;

    Set<String> getInputParameters(final String queryName, final HpccConnection hpccConnection,boolean isGenericQuery,String inputParamQuery) throws Exception;

    Map<String, List<Attribute>> fetchTableData(TableData data) throws HpccConnectionException, RemoteException;
    
	Map<String, Set<String>> getInputParamDistinctValues(String queryName,
			Set<String> inputParams, HpccConnection hpccConnection,
			boolean isGenericQuery, String inputParamQuery) throws Exception;

	List<List<String>> getRootValues(TreeData treeData, Level level, List<TreeFilter> treeFilters) throws HpccConnectionException, RemoteException;
	
	HashMap<String, HashMap<String, List<Attribute>>> fetchScoredSearchData(ScoredSearchData searchData) throws HpccConnectionException,RemoteException;
}
