package org.hpccsystems.dashboard.chart.tree.entity;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Level {
    private List<LevelElement> elements;
    private String imgSrc;

    @XmlElement
    public List<LevelElement> getElements() {
        return elements;
    }

    public void setElements(List<LevelElement> elements) {
        this.elements = elements;
    }

    @XmlElement
    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }
}
