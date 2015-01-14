package org.hpccsystems.dashboard.entity.widget;


public class ChartConfiguration {
    private String type;
    private String name;
    private String staticImage;
    private String editLayout;
    private String hipieChartName;
    
    public ChartConfiguration(String type, String name, String image,
            String layout, String hipieChartName) {
        this.setType(type);
        this.name = name;
        this.staticImage = image;
        this.editLayout = layout;
        this.hipieChartName = hipieChartName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHipieChartName() {
        return hipieChartName;
    }

    public void setHipieChartName(String hipieChartName) {
        this.hipieChartName = hipieChartName;
    }
}
