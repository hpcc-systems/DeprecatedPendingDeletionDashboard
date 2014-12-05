package org.hpccsystems.dashboard.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.TextData;
import org.hpccsystems.dashboard.common.Constants;
import org.zkforge.ckez.CKeditor;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;

public class EditTextController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(EditTextController.class);
    
    @Wire
    private CKeditor editor;

    private TextData textData;
    private Button doneButton;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        textData = (TextData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
        doneButton = (Button) Executions.getCurrent().getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
        doneButton.setDisabled(false);
        if (textData.getHtmlText() != null) {
            editor.setValue(textData.getHtmlText());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Created edit screen");
        }
    }

    @Listen("onChange = #editor;")
    public void onChangeContent(Event event) {
        textData.setHtmlText(((CKeditor) event.getTarget()).getValue());
    }

}
