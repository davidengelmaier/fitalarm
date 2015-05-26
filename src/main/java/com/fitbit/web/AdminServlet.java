package com.fitbit.web;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.samples.calendar.appengine.server.CalendarGwtRpcSample;
import com.google.api.services.samples.calendar.appengine.server.Utils;
import com.google.api.services.samples.calendar.appengine.shared.GwtCalendar;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.totodon.fitalarm.jdos.PrefixJDO;
import com.totodon.molitan.managers.PersistanceManager;
import com.totodon.molitan.managers.exceptions.PersistanceServiceException;
import com.totodon.molitan.services.IPersistanceService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by davidengelmaier on 13/05/15.
 */
public class AdminServlet extends AbstractAppEngineAuthorizationCodeServlet {

    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserService userService = UserServiceFactory.getUserService();
        if(!userService.isUserLoggedIn()) {
            response.getWriter().write(userService.createLoginURL("Login:"));
        } else {
            IPersistanceService persistanceService = PersistanceManager.getPersistanceService();
            String userId = userService.getCurrentUser().getUserId().toString();
            PrefixJDO prefixJDO = (PrefixJDO) persistanceService.getObject(PrefixJDO.class, userId);
            if(prefixJDO == null) {
                prefixJDO = new PrefixJDO(userId, "#");
                try {
                    persistanceService.saveObject(prefixJDO);
                } catch (PersistanceServiceException e) {
                    e.printStackTrace();
                }
            }
            response.getWriter().write("Roger :):" + userService.getCurrentUser().getUserId() + " | " + userService.getCurrentUser().getEmail());

            String pageToken = null;
            do {
                Events events = Utils.loadCalendarClient().events().list("primary").setTimeMin(new DateTime(new Date())).setPageToken(null).execute();
                List<Event> items = events.getItems();
                for (Event event : items) {
                    response.getWriter().write(event.getSummary() + " | ");
                }
                pageToken = events.getNextPageToken();
            } while (pageToken != null);
        }
    }

    @Override
    protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
        return Utils.getRedirectUri(req);
    }

    @Override
    protected AuthorizationCodeFlow initializeFlow() throws IOException {
        return Utils.newFlow();
    }
}
