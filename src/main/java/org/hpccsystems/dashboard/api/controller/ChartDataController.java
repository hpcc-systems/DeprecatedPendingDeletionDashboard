package org.hpccsystems.dashboard.api.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.tree.entity.Node;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.common.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Controller for rendering the data for the charts.
 * 
 */
@Controller
@RequestMapping("*.do")
public class ChartDataController {

	private static final Log logger = LogFactory.getLog(ChartDataController.class);

	@RequestMapping(value = Constants.Hierarchical_Chart_Data)
	public void constructHierarchicalChartData(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		JsonObject jsObj = new JsonObject();
		Gson gson = new Gson();
		
		Node _curNode = null;
		ChartRenderer _chartRenderer = new ChartRenderer();
		try {
			response.setContentType(Constants.RES_TEXT_TYPE_JSON);
			response.setCharacterEncoding(Constants.CHAR_CODE);
			String chart_unique_Id = request.getParameter("chart_id");
			String _nodeFilters = request.getParameter("node");
			if(logger.isDebugEnabled()){
				logger.debug("Request chart_unique_Id -->"+chart_unique_Id);
				logger.debug("Request _nodeFilters -->"+_nodeFilters);
			}
			
			TreeData treeData = (TreeData) request.getSession().getAttribute(chart_unique_Id);
			_curNode = gson.fromJson(_nodeFilters, Node.class);
			
			if(logger.isDebugEnabled()){
				logger.debug("_curNode --->"+_curNode);
			}
			
			response.setContentType(Constants.RES_TEXT_TYPE_JSON);
			response.setCharacterEncoding(Constants.CHAR_CODE);
			response.getWriter().write(_chartRenderer.constructChildren(treeData, _curNode).toString());
			
		} catch (Exception ex) {
			logger.error(Constants.EXCEPTION, ex);
			jsObj.addProperty(Constants.STATUS_FAIL, ex.getMessage());
			response.getWriter().write(jsObj.toString());
		}

	}
}
