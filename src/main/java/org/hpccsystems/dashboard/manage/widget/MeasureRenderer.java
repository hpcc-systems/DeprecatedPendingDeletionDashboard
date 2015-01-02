package org.hpccsystems.dashboard.manage.widget;

import java.util.ArrayList;
import java.util.List;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Popup;

public class MeasureRenderer implements ListitemRenderer<Measure> {

	@Override
	public void render(Listitem listitem, Measure measure, int index)
			throws Exception {
		listitem.setValue(measure);
		Listcell listcell = new Listcell();
		listitem.setLabel(measure.getColumn());
		listitem.setDraggable(Constants.TRUE);
		final Popup popup = new Popup();
		popup.setWidth("100px");
		popup.setZclass(Constants.STYLE_POPUP);
		final Button button = new Button("sum");
		measure.setAggregation(AGGREGATION.SUM);
		button.setZclass("btn btn-xs");
		button.setStyle("font-size: 10px; float: right;");
		button.setPopup(popup);

		Listbox listbox = new Listbox();
		listbox.setMultiple(false);

		List<AGGREGATION> list = new ArrayList<AGGREGATION>() {
			/**
				 * 
				 */
			private static final long serialVersionUID = 1L;

			{
				add(AGGREGATION.AVG);
				add(AGGREGATION.COUNT);
				add(AGGREGATION.MAX);
				add(AGGREGATION.MIN);
				add(AGGREGATION.NONE);
				add(AGGREGATION.SUM);
			}
		};
		listbox.setModel(new ListModelList<>(list));
		ListitemRenderer<AGGREGATION> renderer = (item, agg, ind) -> {
			item.setLabel(agg.toString());
		};
		listbox.setItemRenderer(renderer);

		listbox.addEventListener(Constants.ON_SELECT,
				new EventListener<SelectEvent<Component, Object>>() {
					@Override
					public void onEvent(SelectEvent event) throws Exception {
						AGGREGATION selectedItem = (AGGREGATION) event
								.getSelectedObjects().iterator().next();
						measure.setAggregation(selectedItem);
						button.setLabel(selectedItem.toString());
						popup.close();
					}
				});

		popup.appendChild(listbox);
		popup.setParent(listcell);
		listcell.setParent(listitem);
		listcell.appendChild(button);

	}

}
