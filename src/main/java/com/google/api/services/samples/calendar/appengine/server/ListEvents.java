package com.google.api.services.samples.calendar.appengine.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * Created by davidengelmaier on 10/02/15.
 */

@SuppressWarnings("serial")
public class ListEvents extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Initialize Calendar service with valid OAuth credentials
        Calendar service = Utils.loadCalendarClient();

        // Iterate over the events in the specified calendar
        String pageToken = null;
        do {
            Events events = service.events().list("primary").setPageToken(pageToken).execute();
            List<Event> items = events.getItems();
            for (Event event : items) {
                 resp.getWriter().println(event.getSummary());
            }
            pageToken = events.getNextPageToken();
        } while (pageToken != null);
    }
}
