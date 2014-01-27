package org.hpccsystems.dashboard.api.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Controller to interact with Circuit application
 *
 */
@Controller
@RequestMapping("*.do")
public class DashboardCircuitController {	
	private static final  Log LOG = LogFactory.getLog(DashboardCircuitController.class); 
	DashboardService dashboardService;
	@Autowired
	public void setDashboardService(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}
/**
 * Method to process delete dashboard request from circuit
 * @param request
 * @param response
 */
@RequestMapping(value = Constants.CIRCUIT_DELETE_REQ , method = RequestMethod.GET)
public void deleteDashboard(HttpServletRequest request, HttpServletResponse response)throws Exception
{	
	JSONObject jsObj = new JSONObject();
	int rowsDeleted = 0;
	try { 
			String dashboardId = null;
			dashboardId = request.getParameter(Constants.DB_DASHBOARD_ID);	
			rowsDeleted = dashboardService.deleteDashboard(Integer.valueOf(dashboardId),null);	
			if(rowsDeleted > 0)	{
				jsObj.put(Constants.STATUS, Constants.STATUS_SUCCESS);
			}else{
				jsObj.put(Constants.STATUS, Constants.STATUS_FAIL);
				jsObj.put(Constants.STATUS_MESSAGE, Constants.DASHBOARD_NOT_EXIST);
			}
		}
	catch(Exception ex)
	{
		LOG.error("Exception while processing 'Delete Dashboard' request from Circuit",ex);
		jsObj.put(Constants.STATUS, Constants.STATUS_FAIL);
		jsObj.put(Constants.STATUS_MESSAGE, ex.getMessage());
	}
	response.setContentType(Constants.RES_TEXT_TYPE_JSON);
	response.setCharacterEncoding(Constants.CHAR_CODE);
	try
	{
	response.getWriter().write(jsObj.toJSONString());
	}catch(Exception ex)
	{
		LOG.error("Exception while writing JSON response to Circuit",ex);
		throw new Exception("Unable to process Delete Request");
	}
	
}

@RequestMapping(value = Constants.CIRCUIT_CHARTLIST_REQ , method = RequestMethod.GET)
public void getChartList(HttpServletRequest request, HttpServletResponse response)throws Exception
 {
		try {
			PrintWriter out = response.getWriter();
			String paramValue = request.getParameter(Constants.CHARTLIST_FORMAT);			
			if (paramValue != null &&Constants.JSON .equals(paramValue)) {
				JsonObject json;
				JsonArray array = new JsonArray();
				Map<Integer, ChartDetails> chartdetailsMap = Constants.CHART_MAP;
				for (Map.Entry<Integer, ChartDetails> entry : chartdetailsMap.entrySet()) {
					json = new JsonObject();
					json.addProperty(Constants.VALUE, entry.getValue().getChartId());
					json.addProperty(Constants.LABEL, entry.getValue().getChartName());
					json.addProperty(Constants.DESCRIPTION, entry.getValue()
							.getChartDesc());

					array.add(json);
				}
				JsonObject result = new JsonObject();
				result.add(Constants.CHART_LIST, array);
				response.setContentType(Constants.RES_TEXT_TYPE_JSON);
				out.print(result.toString());
				out.flush();
			} 
		} catch (Exception e) {
			LOG.error("Exception while processing ChartLsit Request from Circuit", e);
			throw new Exception("Unable to process ChartLsit Request");
		}

	}

@RequestMapping(value = Constants.CIRCUIT_SEARCH_REQ , method = RequestMethod.GET)
public void searchDashboard(HttpServletRequest request, HttpServletResponse response)throws Exception
 {
		try {
			final Application application = new Application();
			PrintWriter out = response.getWriter();
			String sourceName = request.getParameter(Constants.SOURCE);
			String sourceId = request.getParameter(Constants.SOURCE_ID);
			application.setAppId(sourceId);			
			application.setAppName(sourceName);

			List<Dashboard> dashboardList = null;
			Dashboard dashBoard = null;

			JSONObject obj = null;
			JSONObject jsonResposeObj = new JSONObject();
			JSONArray jsonObjList = new JSONArray();
			try{
			dashboardList = new ArrayList<Dashboard>(
					dashboardService.retrieveDashboardMenuPages(application,null));
			}catch(Exception ex){
				LOG.error("Exception while fetching dahhboards from DB",ex);
				jsonResposeObj.put(Constants.STATUS_FAIL,ex.getMessage());
			}
			if (dashboardList != null) {
				for (final Iterator<Dashboard> iter = dashboardList.iterator(); iter
						.hasNext();) {
					dashBoard = (Dashboard) iter.next();
					obj = new JSONObject();
					obj.put(Constants.NAME_SMALL, dashBoard.getName());
					obj.put(Constants.DB_DASHBOARD_ID, dashBoard.getDashboardId());
					jsonObjList.add(obj);
				}
				if(jsonObjList.size() > 0){
					jsonResposeObj.put(Constants.DASHBOARDS, jsonObjList);
				}else{
					jsonResposeObj.put(Constants.DASHBOARDS,"No Dashboard Exists for the given Source" );
				}
					
			}			
			response.setContentType(Constants.RES_TEXT_TYPE_JSON);
			out.print(jsonResposeObj);
			out.flush();
		} catch (Exception e) {
			LOG.error(
					"Exception while processing Search dahhboard request from Circuit",
					e);
			throw new Exception("Unable to process Search Request");
		}
	}


}

