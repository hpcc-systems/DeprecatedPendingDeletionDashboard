package org.hpccsystems.dashboard;

public enum ChartTypes {

    PIE("PIE"), DONUT("DONUT"), LINE("LINE"), BAR("BAR"), COLUMN("COLUMN"), US_MAP(
            "CHORO"), TABLE("TABLE"), STEP("STEP"), SCATTER("SCATTER"), AREA(
            "AREA");

    private String chartCode;

    public String getChartCode() {
        return chartCode;
    }

    ChartTypes(String chartCode) {
        this.chartCode = chartCode;
    }
}
