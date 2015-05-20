package com.google.api.services.samples.calendar.appengine.client;

import com.google.gwt.user.client.rpc.impl.RemoteServiceProxy;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter.ResponseReader;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.core.client.impl.Impl;
import com.google.gwt.user.client.rpc.impl.RpcStatsContext;

public class CalendarService_Proxy extends RemoteServiceProxy implements com.google.api.services.samples.calendar.appengine.client.CalendarServiceAsync {
  private static final String REMOTE_SERVICE_INTERFACE_NAME = "com.google.api.services.samples.calendar.appengine.client.CalendarService";
  private static final String SERIALIZATION_POLICY ="968DA0F41A8CD41D970C46AFF1EEACB8";
  private static final com.google.api.services.samples.calendar.appengine.client.CalendarService_TypeSerializer SERIALIZER = new com.google.api.services.samples.calendar.appengine.client.CalendarService_TypeSerializer();
  
  public CalendarService_Proxy() {
    super(GWT.getModuleBaseURL(),
      "calendarService", 
      SERIALIZATION_POLICY, 
      SERIALIZER);
  }
  
  public void delete(com.google.api.services.samples.calendar.appengine.shared.GwtCalendar calendar, com.google.gwt.user.client.rpc.AsyncCallback callback) {
    com.google.gwt.user.client.rpc.impl.RemoteServiceProxy.ServiceHelper helper = new com.google.gwt.user.client.rpc.impl.RemoteServiceProxy.ServiceHelper("CalendarService_Proxy", "delete");
    try {
      SerializationStreamWriter streamWriter = helper.start(REMOTE_SERVICE_INTERFACE_NAME, 1);
      streamWriter.writeString("com.google.api.services.samples.calendar.appengine.shared.GwtCalendar/3847389469");
      streamWriter.writeObject(calendar);
      helper.finish(callback, ResponseReader.VOID);
    } catch (SerializationException ex) {
      callback.onFailure(ex);
    }
  }
  
  public void getCalendars(com.google.gwt.user.client.rpc.AsyncCallback callback) {
    com.google.gwt.user.client.rpc.impl.RemoteServiceProxy.ServiceHelper helper = new com.google.gwt.user.client.rpc.impl.RemoteServiceProxy.ServiceHelper("CalendarService_Proxy", "getCalendars");
    try {
      SerializationStreamWriter streamWriter = helper.start(REMOTE_SERVICE_INTERFACE_NAME, 0);
      helper.finish(callback, ResponseReader.OBJECT);
    } catch (SerializationException ex) {
      callback.onFailure(ex);
    }
  }
  
  public void insert(com.google.api.services.samples.calendar.appengine.shared.GwtCalendar calendar, com.google.gwt.user.client.rpc.AsyncCallback callback) {
    com.google.gwt.user.client.rpc.impl.RemoteServiceProxy.ServiceHelper helper = new com.google.gwt.user.client.rpc.impl.RemoteServiceProxy.ServiceHelper("CalendarService_Proxy", "insert");
    try {
      SerializationStreamWriter streamWriter = helper.start(REMOTE_SERVICE_INTERFACE_NAME, 1);
      streamWriter.writeString("com.google.api.services.samples.calendar.appengine.shared.GwtCalendar/3847389469");
      streamWriter.writeObject(calendar);
      helper.finish(callback, ResponseReader.OBJECT);
    } catch (SerializationException ex) {
      callback.onFailure(ex);
    }
  }
  
  public void update(com.google.api.services.samples.calendar.appengine.shared.GwtCalendar updated, com.google.gwt.user.client.rpc.AsyncCallback callback) {
    com.google.gwt.user.client.rpc.impl.RemoteServiceProxy.ServiceHelper helper = new com.google.gwt.user.client.rpc.impl.RemoteServiceProxy.ServiceHelper("CalendarService_Proxy", "update");
    try {
      SerializationStreamWriter streamWriter = helper.start(REMOTE_SERVICE_INTERFACE_NAME, 1);
      streamWriter.writeString("com.google.api.services.samples.calendar.appengine.shared.GwtCalendar/3847389469");
      streamWriter.writeObject(updated);
      helper.finish(callback, ResponseReader.OBJECT);
    } catch (SerializationException ex) {
      callback.onFailure(ex);
    }
  }
  @Override
  public SerializationStreamWriter createStreamWriter() {
    ClientSerializationStreamWriter toReturn =
      (ClientSerializationStreamWriter) super.createStreamWriter();
    if (getRpcToken() != null) {
      toReturn.addFlags(ClientSerializationStreamWriter.FLAG_RPC_TOKEN_INCLUDED);
    }
    return toReturn;
  }
  @Override
  protected void checkRpcTokenType(RpcToken token) {
    if (!(token instanceof com.google.gwt.user.client.rpc.XsrfToken)) {
      throw new RpcTokenException("Invalid RpcToken type: expected 'com.google.gwt.user.client.rpc.XsrfToken' but got '" + token.getClass() + "'");
    }
  }
}
