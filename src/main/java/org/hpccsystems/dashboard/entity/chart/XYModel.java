package org.hpccsystems.dashboard.entity.chart;


/**
 * This class is model for BarChart Type.
 *
 */
public class XYModel {
	private Object xAxisVal;
	private Object yAxisVal;
	
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
	public final Object getyAxisVal() {
		return yAxisVal;
	}

	/**
	 * @param yAxisVal the yAxisVal to set
	 */
	public final void setyAxisVal(final Object yAxisVal) {
		this.yAxisVal = yAxisVal;
	}

	@Override
	public String toString() {
		final StringBuilder objState=new StringBuilder();
		objState.append("xAxisVal:").append(xAxisVal).append(":yAxisVal:").append(yAxisVal);
		return objState.toString();
	}
	
}
