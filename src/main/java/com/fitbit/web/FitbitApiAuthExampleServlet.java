package com.fitbit.web;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.*;
import com.fitbit.api.client.service.FitbitAPIClientService;
import com.fitbit.api.common.model.devices.Device;
import com.fitbit.api.common.model.user.UserInfo;
import com.fitbit.api.model.APIResourceCredentials;
import com.fitbit.api.model.Alarm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 6/22/11
 * Time: 7:05 AM
 */
public class FitbitApiAuthExampleServlet extends HttpServlet {

    public static final String OAUTH_TOKEN = "oauth_token";
    public static final String OAUTH_VERIFIER = "oauth_verifier";

    private FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMapImpl();
    private FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMapImpl();
    private FitbitApiSubscriptionStorage subscriptionStore = new FitbitApiSubscriptionStorageInMemoryImpl();

    private String apiBaseUrl;
    private String fitbitSiteBaseUrl;
    private String exampleBaseUrl;
    private String clientConsumerKey;
    private String clientSecret;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            apiBaseUrl = properties.getProperty("apiBaseUrl");
            fitbitSiteBaseUrl = properties.getProperty("fitbitSiteBaseUrl");
            exampleBaseUrl = properties.getProperty("exampleBaseUrl").replace("/app", "");
            clientConsumerKey = properties.getProperty("clientConsumerKey");
            clientSecret = properties.getProperty("clientSecret");
        } catch (IOException e) {
            throw new ServletException("Exception during loading properties", e);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        FitbitAPIClientService<FitbitApiClientAgent> apiClientService = new FitbitAPIClientService<FitbitApiClientAgent>(
                new FitbitApiClientAgent(apiBaseUrl, fitbitSiteBaseUrl, credentialsCache),
                clientConsumerKey,
                clientSecret,
                credentialsCache,
                entityCache,
                subscriptionStore
        );
        if (request.getParameter("completeAuthorization") != null) {
            String tempTokenReceived = request.getParameter(OAUTH_TOKEN);
            String tempTokenVerifier = request.getParameter(OAUTH_VERIFIER);
            APIResourceCredentials resourceCredentials = apiClientService.getResourceCredentialsByTempToken(tempTokenReceived);

            if (resourceCredentials == null) {
                throw new ServletException("Unrecognized temporary token when attempting to complete authorization: " + tempTokenReceived);
            }
            // Get token credentials only if necessary:
            if (!resourceCredentials.isAuthorized()) {
                // The verifier is required in the request to get token credentials:
                resourceCredentials.setTempTokenVerifier(tempTokenVerifier);
                try {
                    // Get token credentials for user:
                    apiClientService.getTokenCredentials(new LocalUserDetail(resourceCredentials.getLocalUserId()));
                } catch (FitbitAPIException e) {
                    throw new ServletException("Unable to finish authorization with Fitbit.", e);
                }
            }
            try {
                List<Device> devices = apiClientService.getClient().getDevices(new LocalUserDetail(resourceCredentials.getLocalUserId()));
                List<Alarm> alarms = apiClientService.getClient().getAlarms(new LocalUserDetail(resourceCredentials.getLocalUserId()), devices.get(0).getId());
                /*List<String> weekDays = new ArrayList<String>(1);
                weekDays.add("MONDAY");
                Alarm alarm = new Alarm("10:30+01:00", true, false, weekDays);
                Alarm newAlarm = apiClientService.getClient().addAlarm(new LocalUserDetail(resourceCredentials.getLocalUserId()), alarm, devices.get(0).getId());
                UserInfo userInfo = apiClientService.getClient().getUserInfo(new LocalUserDetail(resourceCredentials.getLocalUserId()));
                request.setAttribute("userInfo", userInfo);
                request.getRequestDispatcher("/fitbitApiAuthExample.jsp").forward(request, response);*/
                Iterator<Alarm> iterator = alarms.iterator();
                while(iterator.hasNext()) {
                    Alarm alarm = iterator.next();
                    response.getWriter().write(alarm.getId() + " " + alarm.getLabel() + "<br>");
                }
            } catch (FitbitAPIException e) {
                throw new ServletException("Exception during getting user info", e);
            }
        } else {
            try {
                response.sendRedirect(apiClientService.getResourceOwnerAuthorizationURL(new LocalUserDetail("-"), exampleBaseUrl + "/fitbitApiAuthExample?completeAuthorization="));
            } catch (FitbitAPIException e) {
                throw new ServletException("Exception during performing authorization", e);
            }
        }
    }
}
