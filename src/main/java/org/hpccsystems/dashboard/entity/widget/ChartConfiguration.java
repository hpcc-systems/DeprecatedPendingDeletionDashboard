package org.hpccsystems.dashboard.entity.widget;

import org.hpccsystems.dashboard.Constants.CHART_TYPES;

public class ChartConfiguration {
    private CHART_TYPES type;
    private String name;
    private String staticImage;
    private String editLayout;
    
    public ChartConfiguration(CHART_TYPES type, String name, String image, String layout) {
        this.setType(type);
        this.name = name;
        this.staticImage = image;
        this.editLayout = layout;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStaticImage() {
        return staticImage;
    }
    public void setStaticImage(String staticImage) {
        this.staticImage = staticImage;
    }
    public String getEditLayout() {
        return editLayout;
    }
    public void setEditLayout(String editLayout) {
        this.editLayout = editLayout;
    }

    public CHART_TYPES getType() {
        return type;
    }

    public void setType(CHART_TYPES type) {
        this.type = type;
    }
}
