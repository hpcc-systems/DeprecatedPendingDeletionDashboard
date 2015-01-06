package org.hpccsystems.dashboard.controller.component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Image;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

public class ImageGridPopup extends Popup {
    
    private static final long serialVersionUID = 1L;

    private static final String ICONS_PATH = Executions.getCurrent()
            .getDesktop().getWebApp().getRealPath("/demo/chart/icons/");
    
    private static final String ICONS_RELATIVE_PATH = "/demo/chart/icons/";
    private static final String COLOR_SUFFIX = "_c";
    private static final String IMAGE_SIZE = "32px";
    private static final String SELECTED_IMAGE_STYLE = "selected-image";
    
    private Image selectedImage;
    
    private EventListener<Event> clickListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Image image = (Image) event.getTarget();
            image.setSclass(SELECTED_IMAGE_STYLE);
            
            if(selectedImage != null) {
                selectedImage.setSclass(null);
            }
            selectedImage = image;
            
            //Send event to parent to inform icon change
            //Data sent is name of the icon currently chosen
            Events.postEvent(new Event("onIconChange", ImageGridPopup.this.getParent(), selectedImage.getAttribute("iconName")));
            
            //Closing the popup on selecting an image
            ImageGridPopup.this.close();
        }
    }; 
    
    public ImageGridPopup() {
        constructGrid(null);
    }
    
    public ImageGridPopup(String selectedIcon) {
        constructGrid(selectedIcon);
    }
    
    private void constructGrid(String selectedIcon) {
    	this.setZclass("popup");
    	
        Grid grid = new Grid();
        grid.setOddRowSclass(null);
        grid.setSclass("white-grid");
        grid.setWidth("164px");
        
        Columns columns = new Columns();
        columns.appendChild(new Column(null,null,IMAGE_SIZE));
        columns.appendChild(new Column(null,null,IMAGE_SIZE));
        columns.appendChild(new Column(null,null,IMAGE_SIZE));
        columns.appendChild(new Column(null,null,IMAGE_SIZE));
        columns.appendChild(new Column(null,null,IMAGE_SIZE));
        grid.appendChild(columns);
        
        File iconsDirectory = new File(ICONS_PATH);
        List<File> files = new ArrayList<File>(Arrays.asList(iconsDirectory.listFiles()));
        List<String> icons = new ArrayList<String>();
        String iconName;
        for (File file : files) {
           iconName = FilenameUtils.removeExtension(file.getName());
           //Excluding black icons
           if(!StringUtils.endsWith(iconName, COLOR_SUFFIX)) {        	  
               icons.add(iconName);
           }
        }
        
        Rows rows = new Rows();
        Row row = new Row();
        row.setSclass("row-nohover");
        int count = 1;
        Image image;
        for (Iterator<String> iterator = icons.iterator(); iterator.hasNext();) {
            iconName = iterator.next();
            image = new Image(
                    new StringBuilder(ICONS_RELATIVE_PATH)
                    .append(iconName)
                    .append(".png")
                    .toString()
                    );
            
            image.addEventListener(Events.ON_CLICK, clickListener);
            image.setAttribute("iconName", iconName);
            
            if(selectedIcon != null && selectedIcon.equals(iconName)) {
                image.setSclass(SELECTED_IMAGE_STYLE);
                selectedImage = image;
            }
            
            row.appendChild(image);
            
            //Adding a new row for every five icons
            if(count % 5 == 0 || !iterator.hasNext()) {
                rows.appendChild(row);
                
                if(iterator.hasNext()) {
                    row = new Row();
                    row.setSclass("row-nohover");
                }
            }
            count++;
        }
        grid.appendChild(rows);
        
        this.appendChild(grid);
    }
}
