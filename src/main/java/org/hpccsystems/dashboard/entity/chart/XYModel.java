package org.hpccsystems.dashboard.entity.chart;

import java.util.List;

public class XYModel {
	private List<Object> xAxisValues;
	private List<Object> yAxisValues;
	
	public XYModel() {
	}
	
	public final List<Object> getyAxisValues() {
		return yAxisValues;
	}
	public final void setyAxisValues(final List<Object> yAxisValues) {
		this.yAxisValues = yAxisValues;
	}

	public List<Object> getxAxisValues() {
		return xAxisValues;
	}

	public void setxAxisValues(List<Object> xAxisValues) {
		this.xAxisValues = xAxisValues;
	}

	@Override
	public String toString() {
		return "XYModel [xAxisValues=" + xAxisValues + ", yAxisValues="
				+ yAxisValues + "]";
	}
	
}
