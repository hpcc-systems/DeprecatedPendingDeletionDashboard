package org.hpccsystems.dashboard.controller;
 
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.RequestParams;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.GroupService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.springframework.dao.DataAccessException;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.Button;
import org.zkoss.zul.Center;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 * SidebarController is used to handle the sidebar logic for Dashboard project
 *  and controller class for sidebar.zul.
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SidebarController extends GenericForwardComposer<Component>{

    private static final long serialVersionUID = 1L;
    
    private static final  Log LOG = LogFactory.getLog(SidebarController.class);
    
    //wire components
    @Wire
    Div sidebarContainer;
    @Wire
    Navbar navBar;
    @Wire
    Button addDash;
    
    @WireVariable
    private DashboardService dashboardService;
    
    @WireVariable
    AuthenticationService  authenticationService;
    
    @WireVariable
    WidgetService  widgetService;
    
    @WireVariable
    GroupService groupService;
    
    @Override
    public void doAfterCompose(final Component comp) throws Exception{
        super.doAfterCompose(comp);
        if(LOG.isDebugEnabled()) {
            LOG.debug("Initiating sidebar page");
        }
       
        
        // Wire Spring Bean
        Selectors.wireVariables(navBar, this, Selectors.newVariableResolvers(getClass(), null));
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("Request path - " + Executions.getCurrent().getParameterMap());
        }
        
        
        List<Dashboard> sideBarPageList = null;
        try {
            //Circuit/External Source Flow   
            if(authenticationService.getUserCredential().hasRole(Constants.ROLE_API_VIEW_DASHBOARD)){
                RequestParams requestParams = (RequestParams) Sessions.getCurrent().getAttribute(Constants.REQUEST_PRAMS);
                String sharedDashboardId = requestParams.getDashbaordId();
                
                Dashboard shareDashboard = new Dashboard();
                shareDashboard.setDashboardId(Integer.valueOf(sharedDashboardId));
                List<String> groupIds = getGroupIds(); 
                //TODO:need to check the filtering dashbords by application id
                List<Dashboard> shareDashboardList = dashboardService.retrieveDashboards(
                        authenticationService.getUserCredential().getApplicationId(),
                        authenticationService.getUserCredential().getUserId(), 
                        groupIds); 
                if(shareDashboardList.contains(shareDashboard)){
                    sideBarPageList = shareDashboardList
                            .stream()
                            .filter(searchDashboard -> (searchDashboard
                                    .getDashboardId().equals(shareDashboard
                                    .getDashboardId())))
                            .collect(Collectors.toList());
                }else{
                    Clients.showNotification("Not authorized to view this Dashboard", true);
                    return;
                }
                
            }else if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_EDIT_DASHBOARD)){
                String[] dashboardIdArray = (String[])Executions.getCurrent().getParameterValues(Constants.DB_DASHBOARD_ID);
                List<String> dashboardIdList =Arrays.asList(dashboardIdArray);
                sideBarPageList = getApiViewDashboardList(
                        authenticationService.getUserCredential().getUserId(),
                        authenticationService.getUserCredential().getApplicationId(),
                        dashboardIdList
                    );                
            } else {
                //Dashboard Flow
                //Add dashboard    
                if(authenticationService.getUserCredential().hasRole(Constants.DASHBOARD_CREATE_ROLE)){
                    addDash.addEventListener(Events.ON_CLICK, addDashboardBtnLisnr);    
                }
                List<String> groupIds = getGroupIds();                
                
                //for Super user
                if(authenticationService.getUserCredential().isSuperUser()){
                    sideBarPageList =dashboardService.getAllDashboard(authenticationService.getUserCredential().getApplicationId());
                }else{    //for others                        
                    sideBarPageList =dashboardService.retrieveDashboards(
                            authenticationService.getUserCredential().getApplicationId(),
                            authenticationService.getUserCredential().getUserId(), 
                            groupIds);    
                }

            }
        } catch(DataAccessException ex) {
            Clients.showNotification(Labels.getLabel("unableToRetrieveDashboards"), true);
            LOG.error(Constants.EXCEPTION, ex);
        }
        
        Navitem firstNavitem = null; 
        Boolean firstSet = false;
        Dashboard entry=null;
        Navitem navitem=null;
        
        for (final Iterator<Dashboard> iter = sideBarPageList.iterator(); iter.hasNext();) {
            entry = (Dashboard) iter.next();
            navitem  = constructNavItem(entry);
            navBar.appendChild(navitem);

            // Retriving first NavItem, to set as default
            if(!firstSet){
                firstNavitem = navitem;
                firstSet = !firstSet;
            }
        }
        
        // Displaying first menu item as default page
        if(firstSet) {
            //Setting current/First dashboard in session will load it when page loads
            Sessions.getCurrent().setAttribute(Constants.ACTIVE_DASHBOARD_ID, firstNavitem.getAttribute(Constants.DASHBOARD_ID));
            Sessions.getCurrent().setAttribute(Constants.ACTIVE_DASHBOARD_ROLE, firstNavitem.getAttribute(Constants.DASHBOARD_ROLE));
            
            firstNavitem.setSelected(true);
        }else {
            Clients.evalJavaScript("showPopUp()");
        }
        
        //Setting to session for logout controller
        Sessions.getCurrent().setAttribute(Constants.NAVBAR, navBar);
    }

    private List<String> getGroupIds() {
        List<String> groupIds = null;
        try {
            groupIds = groupService.getGroupCodes(authenticationService.getUserCredential().getUserId());
        } catch(DataAccessException | RemoteException | ServiceException e) {
            LOG.error(Constants.EXCEPTION, e);
            groupIds = new ArrayList<String>();
        }        
        return groupIds;
    }

    private Navitem constructNavItem(final Dashboard dashboard) {
        
        final Navitem navitem = new Navitem();
        navitem.setLabel(dashboard.getName());            
        
        //Setting dashboard id to be retrived onClick
        navitem.setAttribute(Constants.DASHBOARD_ID, dashboard.getDashboardId());
        navitem.setAttribute(Constants.DASHBOARD_ROLE, dashboard.getRole());
        
        if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_EDIT_DASHBOARD)){
            navitem.addEventListener(Events.ON_CLICK, apiNavItemSelectLisnr);
        }else{
            navitem.addEventListener(Events.ON_CLICK, navItemSelectLisnr);
        }
        navitem.setIconSclass("glyphicon glyphicon-stats");
        navitem.setZclass("list");
        
        navitem.setDraggable("true");
        navitem.setDroppable("true");
        navitem.addEventListener(Events.ON_DROP, onDropEvent);
        
        return navitem;
    }
    
    /**
     * Listener for onClick in dashboard menus
     */
    EventListener<Event> navItemSelectLisnr = new SerializableEventListener<Event>() {

        private static final long serialVersionUID = 1L;

        public void onEvent(final Event event) {
            // use iterable to find the first include only
            final Include include = (Include) Selectors.iterable(sidebarContainer.getPage(), "#mainInclude")
                    .iterator().next();
            //Setting currently active Dashboard session
            if(LOG.isDebugEnabled()){
                LOG.debug("Setting active dashboard to session" + event.getTarget().getAttribute(Constants.DASHBOARD_ID));
            }
            //Detaching the include and Including the page again to trigger reload
            includeDashboard(event,include);
            
        }
        
    };
    /**
     * Detaching the include and Including the page again to trigger reload
     * @param event
     * @param include
     */
    private void includeDashboard(Event event, Include include) {
        final Component component = include.getParent();
        include.detach();
        final Include newInclude = new Include("/demo/layout/dashboard.zul");
        newInclude.setId("mainInclude");
        newInclude.setDynamicProperty(Constants.ACTIVE_DASHBOARD_ID, event.getTarget().getAttribute(Constants.DASHBOARD_ID));
        newInclude.setDynamicProperty(Constants.ACTIVE_DASHBOARD_ROLE, event.getTarget().getAttribute(Constants.DASHBOARD_ROLE));
        component.appendChild(newInclude);        
    }
    
    /**
     * Listener for onClick of a dashboard when request triggered from Circuit/external Source 
     */
    EventListener<Event> apiNavItemSelectLisnr = new SerializableEventListener<Event>() {

        private static final long serialVersionUID = 1L;

        public void onEvent(final Event event) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Setting active dashboard to session in Api flow" + event.getTarget().getAttribute(Constants.DASHBOARD_ID));
            }            
            Iterator<Component> iterator = sidebarContainer.getParent().getParent().getFellows().iterator();
            Component centerComp =null;
            while(iterator.hasNext()) {
                Component comp = iterator.next();
                if(comp instanceof Center){
                    centerComp = comp;
                    break;
                }
            }    
            includeApiDashboard(centerComp,event);            
        }
    };
    
    /**
     * Listener for onClick in profile
     */
    EventListener<Event> profileSelectLisnr = new EventListener<Event>() {

        @Override
        public void onEvent(final Event arg0) {
            // use iterable to find the first include only
            final Include include = (Include) Selectors.iterable(sidebarContainer.getPage(), "#mainInclude")
                    .iterator().next();
            
            include.setSrc("/demo/profile-mvc.zul");
        }
        
    };
    
    /**
     * Listener for Add Dashboard Button
     */
    EventListener<Event> addDashboardBtnLisnr = new EventListener<Event>() {

        public void onEvent(final Event event) {
            // Defining parameters to send to Modal Dialog
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(Constants.PARENT, sidebarContainer);

            final Window window = (Window) Executions.createComponents(
                    "/demo/layout/dashboard_config.zul", sidebarContainer,
                    parameters);
            window.doModal();
        }

    };
    
    /**
     * Event to be triggered when add dashboard form gets submitted Adds a row
     * to the sidebar
     * 
     * @param event
     */
    public void onCloseDialog(final Event event) {        
        try { 
            final Dashboard dashboard = (Dashboard) event.getData();    
            //updating dashboard sequence into dashboard_details
            List<Component> comp = navBar.getChildren();
            dashboard.setSequence(comp.size());
            // Make entry of new dashboard details into DB
            
             dashboardService.addDashboardDetails(
                        dashboard,
                        authenticationService.getUserCredential().getApplicationId(),
                        null,
                        authenticationService.getUserCredential().getUserId()
                );
                //adding widget details into db while adding new dashboard.
                widgetService.addWidgetDetails(dashboard.getDashboardId(), dashboard.getPortletList());            
            
            dashboard.setPersisted(false);
            final Navitem navitem = constructNavItem(dashboard);
            navBar.appendChild(navitem);
            
            // Redirect to the recently added page
            Events.sendEvent(new Event("onClick", navitem));
            navitem.setSelected(true);
        } catch (DataAccessException exception) {
            Clients.showNotification(Labels.getLabel("addDashboardError"), true);
            LOG.error(Constants.EXCEPTION, exception);
            return;
        } catch (Exception exception) {
            Clients.showNotification(Labels.getLabel("addDashboardError"),true);
            LOG.error(Constants.EXCEPTION, exception);
            return;
        }
    }
    
    /**loads Dashboard in Api call
     * @param centerComp
     * @param event
     */
    protected void includeApiDashboard(Component centerComp,final Event event) {
        if(centerComp != null){
            Component childComp = centerComp.getFirstChild();
            centerComp.removeChild(childComp);
            final Include newInclude = new Include("/demo/layout/dashboard.zul");            
            newInclude.setId("mainInclude");
            newInclude.setDynamicProperty(Constants.ACTIVE_DASHBOARD_ID, event.getTarget().getAttribute(Constants.DASHBOARD_ID));
            centerComp.appendChild(newInclude);
        }        
    }

    /**
     * Method is used to drag and drop the sidebar's dashboard.
     */
    EventListener<DropEvent> onDropEvent = new EventListener<DropEvent>() {
        public void onEvent(DropEvent event) {
            final Navitem dragged = (Navitem) event.getDragged();
            final Navitem dropped = (Navitem) event.getTarget();
            final List<Component> list = navBar.getChildren();
            swapDashboard(list, dragged, dropped);
            }
    };

    /**Swaps dashboard
     * @param list
     * @param dragged
     * @param dropped
     */
    private void swapDashboard(final List<Component> list,
            final Navitem dragged, final Navitem dropped) {
        for (final Component component : list) {
            if (component instanceof Navitem) {
                final Navitem currentNavitem = (Navitem) component;
                if (currentNavitem.equals(dropped)) {
                    navBar.insertBefore(dragged, dropped);
                    updateDashboardSequence();
                    return;
                } else if (currentNavitem.equals(dragged)) {
                    navBar.insertBefore(dropped, dragged);
                    updateDashboardSequence();
                    return;
                }
                if (authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_EDIT_DASHBOARD)) {
                    currentNavitem.addEventListener(Events.ON_CLICK,apiNavItemSelectLisnr);
                } else {
                    currentNavitem.addEventListener(Events.ON_CLICK,navItemSelectLisnr);
                }
            }
        }
    }
        
    private void updateDashboardSequence() {
        try{
        List<Integer> dashboardList = new ArrayList<Integer>();
        for(Component component : navBar.getChildren()){
            Navitem navItem = (Navitem) component;
            if(navItem.isVisible()){
                dashboardList.add((Integer)navItem.getAttribute(Constants.DASHBOARD_ID));
            }
        }
        dashboardService.updateSidebarDetails(dashboardList);
        }catch(DataAccessException ex){
            Clients.showNotification(Labels.getLabel("unableToUpdateOrderOfDashboard"), true);
            LOG.error(Constants.EXCEPTION, ex);
            return;
        }
    }

    
    private List<Dashboard> getApiViewDashboardList(final String userId,final String applicationId,  List<String> dashboardIdList)throws DataAccessException {
        
        if(LOG.isDebugEnabled()){
            LOG.debug("Requested Dashboard Id : "+dashboardIdList);
        }
        List<Dashboard> sideBarPageList =dashboardService.retrieveDashboardMenuPages(applicationId,userId,dashboardIdList,null);    
        if(LOG.isDebugEnabled()){
            LOG.debug("sideBarPageList: "+sideBarPageList);
        }
        return sideBarPageList;
        
    }
}
