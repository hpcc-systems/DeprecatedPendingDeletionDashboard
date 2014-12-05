package org.hpccsystems.dashboard.controller;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Include;
import org.zkoss.zul.Panel;

public class PluginHowtoController extends SelectorComposer<Panel>{

    private static final long serialVersionUID = 1L;
    
    @Override
    public void doAfterCompose(Panel comp) throws Exception {
        super.doAfterCompose(comp);
        
        // On Close Event
        this.getSelf().addEventListener(Events.ON_CLOSE,
                new EventListener<Event>() {
                    @Override
                    public void onEvent(Event arg0) throws Exception {
                        Include include = (Include) PluginHowtoController.this.getSelf().getParent();
                        include.setSrc(null);
                    }
                }
        );
    }

}
