package org.hpccsystems.dashboard.api.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.api.entity.ApiChartConfiguration;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.entity.Measure;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.LDAPAuthenticationService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zkoss.util.resource.Labels;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Controller to interact with Circuit application
 * 
 */
@Controller
@RequestMapping("*.do")
public class DashboardApiController {
	private static final Log LOG = LogFactory
			.getLog(DashboardApiController.class);

	DashboardService dashboardService;
	WidgetService widgetService;
	AuthenticationService authenticationService;
	LDAPAuthenticationService LDAPService;
	ChartService chartService;

	@Autowired
	public void setChartService(ChartService chartService) {
		this.chartService = chartService;
	}

	@Autowired
	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Autowired
	public void setDashboardService(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@Autowired
	public void setWidgetService(WidgetService widgetService) {
		this.widgetService = widgetService;
	}

	@Autowired
	public void setLDAPService(LDAPAuthenticationService lDAPService) {
		LDAPService = lDAPService;
	}

	/**
	 * Method to process delete dashboard request from circuit
	 * 
	 * No authentication set for the process
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = Constants.CIRCUIT_DELETE_REQ)
	public void deleteDashboard(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		JsonObject jsObj = new JsonObject();
		int rowsDeleted = 0;
		try {
			// Authenticating the user
			boolean isLoginSuccessful = true;
			response.setContentType(Constants.RES_TEXT_TYPE_JSON);
			response.setCharacterEncoding(Constants.CHAR_CODE);

			if (isLoginSuccessful) {
				String dashboardId = null;
				dashboardId = request.getParameter(Constants.DB_DASHBOARD_ID);
				rowsDeleted = dashboardService.deleteDashboard(
						Integer.valueOf(dashboardId), null);
				if (rowsDeleted > 0) {
					jsObj.addProperty(Constants.STATUS,
							Constants.STATUS_SUCCESS);
				} else {
					jsObj.addProperty(Constants.STATUS, Constants.STATUS_FAIL);
					jsObj.addProperty(Constants.STATUS_MESSAGE,
							Constants.DASHBOARD_NOT_EXIST);
				}

			} else {
				jsObj.addProperty(Constants.INVALID_USER,
						Labels.getLabel("invalidUser"));
			}

			response.getWriter().write(jsObj.toString());
		} catch (DataAccessException ex) {
			LOG.error(Constants.EXCEPTION, ex);
			jsObj.addProperty(Constants.STATUS, Constants.STATUS_FAIL);
			jsObj.addProperty(Constants.STATUS_MESSAGE, ex.getMessage());
			response.getWriter().write(jsObj.toString());
		}

	}

	@RequestMapping(value = Constants.CIRCUIT_CHARTLIST_REQ)
	public void getChartList(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// Authenticating the user
		boolean isLoginSuccessful = true;
		JsonObject jsonObject;
		response.setContentType(Constants.RES_TEXT_TYPE_JSON);
		response.setCharacterEncoding(Constants.CHAR_CODE);
		if (isLoginSuccessful) {
			String paramValue = request
					.getParameter(Constants.CHARTLIST_FORMAT);
			if (paramValue != null && Constants.JSON.equals(paramValue)) {

				JsonArray jsonArray = new JsonArray();
				Map<Integer, ChartDetails> chartdetailsMap = chartService
						.getCharts();
				for (Map.Entry<Integer, ChartDetails> entry : chartdetailsMap
						.entrySet()) {
					// Supports only Table,XY,Pie charts  					
					if (Constants.CATEGORY_PIE == chartdetailsMap.get(entry.getKey()).getCategory() 
							|| Constants.CATEGORY_XY_CHART == chartdetailsMap.get(entry.getKey()).getCategory() 
							|| Constants.CATEGORY_TABLE == chartdetailsMap.get(entry.getKey()).getCategory()) {
						jsonObject = new JsonObject();
						jsonObject.addProperty(Constants.VALUE, entry.getValue()
								.getId());
						jsonObject.addProperty(Constants.LABEL, entry.getValue()
								.getName());
						jsonObject.addProperty(Constants.DESCRIPTION, entry
								.getValue().getDescription());
						jsonArray.add(jsonObject);
					}
					
				}
				JsonObject result = new JsonObject();
				result.add(Constants.CHART_LIST, jsonArray);
				response.getWriter().write(result.toString());
			}
		} else {
			jsonObject = new JsonObject();
			jsonObject.addProperty(Constants.INVALID_USER,
					Labels.getLabel("invalidUser"));
			response.getWriter().write(jsonObject.toString());
		}
	}

	@RequestMapping(value = Constants.CIRCUIT_SEARCH_REQ)
	public void searchDashboard(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		JsonObject jsonResposeObj = new JsonObject();
		try {
			// Authenticating the user
			boolean isLoginSuccessful = true;// authendicate(request,Constants.CIRCUIT_APPLICATION_ID);
			response.setContentType(Constants.RES_TEXT_TYPE_JSON);
			response.setCharacterEncoding(Constants.CHAR_CODE);
			if (isLoginSuccessful) {
				List<Dashboard> dashboardList = null;
				Dashboard dashBoard = null;
				JsonObject jsonObject = null;
				JsonArray jsonArray = new JsonArray();
				dashboardList = dashboardService.retrieveDashboardMenuPages(
						request.getParameter(Constants.SOURCE), "user"
						/* request.getParameter(Constants.USERNAME) */, null,
						request.getParameter(Constants.SOURCE_ID));
				if (dashboardList != null) {
					for (final Iterator<Dashboard> iter = dashboardList
							.iterator(); iter.hasNext();) {
						dashBoard = (Dashboard) iter.next();
						jsonObject = new JsonObject();
						jsonObject.addProperty(Constants.NAME_SMALL,
								dashBoard.getName());
						jsonObject.addProperty(Constants.DB_DASHBOARD_ID,
								dashBoard.getDashboardId());
						jsonObject.addProperty(Constants.LAST_UPDATED_DATE,
								dashBoard.getLastupdatedDate().toString());
						jsonArray.add(jsonObject);
					}
					if (jsonArray.size() > 0) {
						jsonResposeObj.add(Constants.DASHBOARDS, jsonArray);
					} else {
						jsonResposeObj.addProperty(Constants.DASHBOARDS,
								Labels.getLabel("noDashboard"));
					}
				}
				response.getWriter().write(jsonResposeObj.toString());
			} else {
				jsonResposeObj.addProperty(Constants.INVALID_USER,
						Labels.getLabel("invalidUser"));
				response.getWriter().write(jsonResposeObj.toString());
			}
		} catch (DataAccessException ex) {
			LOG.error(Constants.EXCEPTION, ex);
			jsonResposeObj.addProperty(Constants.STATUS_FAIL, ex.getMessage());
			response.getWriter().write(jsonResposeObj.toString());
		}

	}

	/**
	 * validateDashboard() is responsible for validate that the field list sent
	 * in contains all fields needed to render a dashboard.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = Constants.CIRCUIT_VALIDATE_REQ, method = RequestMethod.POST)
	public void validateDashboard(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		JsonObject jsonObject = new JsonObject();
		try {
			// Authenticating the user
			boolean isLoginSuccessful = true;// authendicate(request,Constants.CIRCUIT_APPLICATION_ID);
			response.setContentType(Constants.RES_TEXT_TYPE_JSON);
			response.setCharacterEncoding(Constants.CHAR_CODE);
			if (isLoginSuccessful) {
				XYChartData chartData = null;
				Attribute xColumn = null;
				List<Measure> yColumnList = null;
				String filterColumn = null;
				Integer filterDataType = 0;
				ApiChartConfiguration chartConfiguration = null;
				List<String> failedValColumnList = new ArrayList<String>();
				String circuitFields = request
						.getParameter(Constants.CIRCUIT_CONFIG);
				String dashboardId = request
						.getParameter(Constants.CIRCUIT_DASHBOARD_ID);
				chartConfiguration = new GsonBuilder().create().fromJson(
						circuitFields, ApiChartConfiguration.class);
				List<Portlet> portletList = widgetService
						.retriveWidgetDetails(Integer.valueOf(dashboardId));
				Map<Integer, ChartDetails> chartdetailsMap = chartService
						.getCharts();
				if (portletList != null && !portletList.isEmpty()) {
					for (Portlet portlet : portletList) {
						chartData = (XYChartData) portlet.getChartData();
						if (chartData != null
								&&
								// Adding NOT TABLE WIDGET check to restrict to
								// pie, bar & line charts only
								Constants.CATEGORY_TABLE != chartdetailsMap
										.get(portlet.getChartType())
										.getCategory()) {
							xColumn = chartData.getAttribute();
							yColumnList = chartData.getMeasures();
							// For XAxis & YAxis Validation
							xColumnValidation(failedValColumnList, chartData,
									chartConfiguration);
							yColumnValidation(failedValColumnList, yColumnList,
									chartConfiguration);
							// Filter Column Validation
							if (chartData.getIsFiltered()) {
								for (Filter filter : chartData.getFilters()) {
									filterColumn = filter.getColumn();
									filterDataType = filter.getType();
									filterColumnValidation(failedValColumnList,
											filterColumn, filterDataType,
											chartConfiguration);
								}
							}
						}
					}
					if (failedValColumnList.isEmpty()) {
						jsonObject.addProperty(Constants.STATUS,
								Constants.STATUS_SUCCESS);
					} else {
						jsonObject.addProperty(Constants.STATUS,
								Constants.STATUS_FAIL);
						StringBuilder failedStr = new StringBuilder();
						int index = 0;
						for (String failedColumn : failedValColumnList) {
							if (index != failedValColumnList.size() - 1) {
								failedStr.append(failedColumn).append(",");
							} else if (index == failedValColumnList.size() - 1) {
								failedStr.append(failedColumn);
							}
							index++;
						}
						if (failedValColumnList.size() == 1) {
							failedStr.append(Constants.FIELD_NOT_EXIST);
						} else if (failedValColumnList.size() > 1) {
							failedStr.append(Constants.FIELDS_NOT_EXIST);
						}
						jsonObject.addProperty(Constants.STATUS_MESSAGE,
								failedStr.toString());
					}

				} else {
					jsonObject.addProperty(Constants.STATUS,
							Constants.STATUS_FAIL);
					jsonObject.addProperty(Constants.STATUS_MESSAGE,
							Constants.DASHBOARD_NOT_EXISTS);
				}
				response.getWriter().write(jsonObject.toString());
			} else {
				jsonObject.addProperty(Constants.INVALID_USER,
						Labels.getLabel("invalidUser"));
				response.getWriter().write(jsonObject.toString());
			}
		} catch (DataAccessException ex) {
			LOG.error(Constants.EXCEPTION, ex);
			jsonObject.addProperty(Constants.STATUS, Constants.STATUS_FAIL);
			jsonObject.addProperty(Constants.STATUS_MESSAGE, ex.getMessage());
			response.getWriter().write(jsonObject.toString());
		}
	}

	/**
	 * xColumnValidation() is responsible for Validate the xColumn.
	 * 
	 * @param failedColumnList
	 * @param xColumnList
	 * @param configuration
	 * @return
	 */
	private void xColumnValidation(List<String> failedColumnList,
			XYChartData chartData, ApiChartConfiguration configuration) {
		
		Attribute xColumn = chartData.getAttribute();
		//validating Xcolumn
		validateAttribute(xColumn,configuration,failedColumnList);
		//validating grouped column
		if(chartData.isGrouped()){
			validateAttribute(chartData.getGroupAttribute(),configuration,failedColumnList);
		}
	}

	private void validateAttribute(Attribute xColumn,
			ApiChartConfiguration configuration, List<String> failedColumnList) {
		Boolean xAxisValStatus = false;
		if (xColumn != null) {
			for (Field entry : configuration.getFields()) {
				if (xColumn.getColumn().equals(entry.getColumnName().trim())) {
					xAxisValStatus = true;
					break;
				}
			}
			if (!xAxisValStatus && !failedColumnList.contains(xColumn)) {
				failedColumnList.add(xColumn.getColumn());
			}
		}
	}

	/**
	 * yColumnValidation() is responsible for Validate the yColumn.
	 * 
	 * @param failedColumnList
	 * @param yColumnList
	 * @param configuration
	 * @return
	 */
	private void yColumnValidation(List<String> failedColumnList,
			List<Measure> yColumnList, ApiChartConfiguration configuration) {
		Boolean yAxisValStatus = false;
		if (yColumnList != null) {
			for (Measure measure : yColumnList) {
				for (Field entry : configuration.getFields()) {
					if (measure.equals(entry.getColumnName().trim())
							&& DashboardUtil.checkNumeric(entry.getDataType()
									.trim())) {
						yAxisValStatus = true;
						break;
					}
				}
				if (!yAxisValStatus
						&& !failedColumnList.contains(measure.getColumn()
								.trim())) {
					failedColumnList.add(measure.getColumn().trim());
				}
				yAxisValStatus = false;
			}
		}
	}

	/**
	 * filterColumnValidation() is responsible for Validate the filterColumn.
	 * 
	 * @param failedColumnList
	 * @param filterColumn
	 * @param filterDataType
	 * @param configuration
	 * @return
	 */
	private void filterColumnValidation(List<String> failedColumnList,
			String filterColumn, Integer filterDataType,
			ApiChartConfiguration configuration) {
		Boolean filterColumnValStatus = false;
		for (Field entry : configuration.getFields()) {
			if (filterColumn.equals(entry.getColumnName().trim())
					&& DashboardUtil.checkNumeric(entry.getDataType().trim())) {
				filterColumnValStatus = true;
				break;
			}
		}
		if (!filterColumnValStatus
				&& !failedColumnList.contains(filterColumn.trim())) {
			failedColumnList.add(filterColumn.trim());
		}
	}

	/**
	 * Authenticates the API user
	 * 
	 * @param request
	 * @return boolean
	 */
	/*
	 * private boolean authendicate(HttpServletRequest request, String appId)
	 * throws DataAccessException, RemoteException,
	 * ServiceException,RuntimeException { String userName =
	 * request.getParameter(Constants.USERNAME); String pwd =
	 * request.getParameter(Constants.CREDENTIAL); boolean isLoginSuccessful =
	 * false; User user = null;
	 * if("true".equals(Labels.getLabel("enableLDAP"))){ user =
	 * LDAPService.authenticate(userName, pwd); }else{ user =
	 * authenticationService.authenticate(userName, pwd); } isLoginSuccessful =
	 * authenticationService.login(user, appId);
	 * LOG.debug("User authenticated .." + isLoginSuccessful); return
	 * isLoginSuccessful; }
	 */

}
