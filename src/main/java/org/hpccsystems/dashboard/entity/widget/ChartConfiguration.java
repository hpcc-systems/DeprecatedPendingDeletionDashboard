package org.hpccsystems.dashboard.entity.widget;

public class ChartConfiguration {
    private String name;
    private String staticImage;
    private String editLayout;
    
    public ChartConfiguration(String name, String image, String layout) {
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
}
