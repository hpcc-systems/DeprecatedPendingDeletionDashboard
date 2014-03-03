package org.hpccsystems.dashboard.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Include;
import org.zkoss.zul.Menuitem;

/**
 * LogoutController is responsible for logout from the Dashboard project.
 * This includes profile link.
 * 
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class LogoutController extends SelectorComposer<Component> {

	private static final Log LOG = LogFactory.getLog(LogoutController.class);
	private static final long serialVersionUID = 1L;

	@WireVariable
	AuthenticationService authenticationService;

	@Wire
	Menuitem logout;

	@Listen("onClick=#logout")
	public void doLogout() throws Exception {
		authenticationService.logout(Sessions.getCurrent().getAttribute("user"));
		Executions.sendRedirect("/demo/");
		if (LOG.isInfoEnabled()) {
			LOG.debug("Successfully Logged out");
		}
	}

	@Listen("onClick=#profile-link")
	public void onEvent(final Event arg0) throws Exception {
		// use iterable to find the first include only
		final Include include = (Include) Selectors
				.iterable(this.getPage(), "#mainInclude").iterator().next();
		include.setSrc("/demo/profile-mvc.zul");
	}
}
