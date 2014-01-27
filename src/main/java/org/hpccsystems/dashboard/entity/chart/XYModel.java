package org.hpccsystems.dashboard.entity.chart;

import java.util.List;


/**
 * This class is model for BarChart Type.
 *
 */
public class XYModel {
	private Object xAxisVal;
	private List<Object> yAxisValues;
	
	public XYModel() {
	}
	
	/**
	 * @return the xAxisVal
	 */
	public final Object getxAxisVal() {
		return xAxisVal;
	}

	/**
	 * @param xAxisVal the xAxisVal to set
	 */
	public final void setxAxisVal(final Object xAxisVal) {
		this.xAxisVal = xAxisVal;
	}

	/**
	 * @return the yAxisVal
	 */
	public final List<Object> getyAxisValues() {
		return yAxisValues;
	}

	/**
	 * @param yAxisVal the yAxisVal to set
	 */
	public final void setyAxisValues(final List<Object> yAxisValues) {
		this.yAxisValues = yAxisValues;
	}

	@Override
	public String toString() {
		final StringBuilder obj =new StringBuilder();
		obj.append("XYModel [xAxisVal=")
			.append(xAxisVal )
			.append(", yAxisValues=")
			.append(yAxisValues)
			.append("]");
		return obj.toString();
	}
}
