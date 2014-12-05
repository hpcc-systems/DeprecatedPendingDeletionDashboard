package org.hpccsystems.dashboard.chart.entity;


public class TextData extends ChartData {
	
	private String htmlText;

	public String getHtmlText() {
		return htmlText;
	}

	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}

	@Override
	public String toString() {
		return "TextData [htmlText=" + htmlText + "]";
	}
	

}
