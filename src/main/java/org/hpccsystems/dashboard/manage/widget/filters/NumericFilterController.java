package org.hpccsystems.dashboard.manage.widget.filters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.NumericFilter;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class NumericFilterController extends SelectorComposer<Component> {
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(NumericFilterController.class);
    private NumericFilter filter;

    @WireVariable
    AuthenticationService authenticationService;

    @WireVariable
    WSSQLService wssqlService;

    @Wire
    Label minimumLabel;

    @Wire
    Label maximumLabel;

    @Wire
    Div sliderDiv;

    @Wire
    Button filtersSelectedBtn;

    BigDecimal min;
    BigDecimal max;
    BigDecimal rangeFactor;
    
    private WidgetConfiguration widgetconfig;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        Map<String, BigDecimal> map = null;

        widgetconfig = (WidgetConfiguration) Executions.getCurrent().getAttribute(
                org.hpccsystems.dashboard.Constants.WIDGET_CONFIG);
        filter = (NumericFilter) Executions.getCurrent().getAttribute(Constants.FILTER);

        map = wssqlService.getMinMax(filter, 
                widgetconfig.getDashboard().getHpccConnection(), widgetconfig.getWidget().
                getLogicalFile().toString(), 
                widgetconfig.getWidget().getFilters());

        min = map.get("min");
        max = map.get("max");

        minimumLabel.setValue(min.toString());
        maximumLabel.setValue(max.toString());

        // Initializing Slider positions
        Integer sliderStart = 0;
        Integer sliderEnd = 100;

        // Translating min & max to a scale of 0 to 100 using Linear equation
        // ((actualVal - actualMin)/(actualMax- actualMin)) = ((sliderVal -sliderMin)/(sliderMax- sliderMin))
        // Range Factor = (actualMax- actualMin)/(sliderMax- sliderMin)

        rangeFactor = max.subtract(min).divide(new BigDecimal(100));

        if (filter.getMinValue() != null && filter.getMaxValue() != null) {
            // Updating slider positions for already applied filters
            sliderStart = filter.getMinValue().subtract(min).divide(rangeFactor, RoundingMode.DOWN).intValue();
            sliderEnd = filter.getMaxValue().subtract(min).divide(rangeFactor, RoundingMode.CEILING).intValue();
        } else {
            filter.setMinValue(min);
            filter.setMaxValue(max);
        }

        StringBuilder html = new StringBuilder();
        html.append("<div id=\"");
        html.append(filter.getColumn());
        html.append("_sdiv\" style=\"margin: 8px;\" class=\"slider-grey\"></div>");

        html.append("<script type=\"text/javascript\">");
        html.append("$('#").append(filter.getColumn()).append("_sdiv').slider({").append("range: true,").append("values: [")
                .append(sliderStart).append(", ").append(sliderEnd).append("]").append("});");

        html.append("$('#").append(filter.getColumn()).append("_sdiv').on( \"slide\", function( event, ui ) {")
                .append("payload = \"").append(filter.getColumn()).append("_hbox,\" + ui.values;")
                .append("zAu.send(new zk.Event(zk.Widget.$('$").append("numericFilterPopup")
                .append("'), 'onSlide', payload, {toServer:true}));").append("});");

        html.append("$('#").append(filter.getColumn()).append("_sdiv').on( \"slidestop\", function( event, ui ) {")
                .append("payload = \"").append(filter.getColumn()).append("_hbox,\" + ui.values;")
                .append("zAu.send(new zk.Event(zk.Widget.$('$").append("numericFilterPopup")
                .append("'), 'onSlideStop', payload, {toServer:true}));").append("});");
        html.append("</script>");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated HTML " + html.toString());
        }

        sliderDiv.appendChild(new Html(html.toString()));

    }

    @Listen("onSlide = #numericFilterPopup")
    public void onSlide(Event event) {
        String[] data = ((String) event.getData()).split(",");

        Integer startPosition = Integer.valueOf(data[1]);
        Integer endPosition = Integer.valueOf(data[2]);

        // Converting position into value
        // value = pos . rangeFactor + min
        minimumLabel.setValue(String.valueOf(rangeFactor.multiply(new BigDecimal(startPosition)).add(min).intValue()));
        maximumLabel.setValue(String.valueOf(rangeFactor.multiply(new BigDecimal(endPosition)).add(min).intValue()));
    }

    @Listen("onSlideStop = #numericFilterPopup")
    public void onSlideStop(Event event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("On Slide Stop Event - Data -- " + event.getData());
        }

        String[] data = ((String) event.getData()).split(",");

        Integer startPosition = Integer.valueOf(data[1]);
        Integer endPosition = Integer.valueOf(data[2]);

        // Updating Change to filter object
        // value = pos . rangeFactor + min
        filter.setMinValue(rangeFactor.multiply(new BigDecimal(startPosition)).add(min));
        filter.setMaxValue(rangeFactor.multiply(new BigDecimal(endPosition)).add(min));

    }

    @Listen("onClick = button#filtersSelectedBtn")
    public void onfiltersSelected() {
        // Hiding the filter's popup window
        Popup popup = (Popup) this.getSelf().getParent().getParent();
        popup.close();

        widgetconfig.getComposer().drawChart();
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Drawn filtered chart with Numeric filter");
        }
    }

}
