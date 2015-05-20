package com.google.api.services.samples.calendar.appengine.shared;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

@SuppressWarnings("deprecation")
public class AuthenticationException_FieldSerializer implements com.google.gwt.user.client.rpc.impl.TypeHandler {
  public static void deserialize(SerializationStreamReader streamReader, com.google.api.services.samples.calendar.appengine.shared.AuthenticationException instance) throws SerializationException {
    
    com.google.gwt.user.client.rpc.core.java.io.IOException_FieldSerializer.deserialize(streamReader, instance);
  }
  
  public static com.google.api.services.samples.calendar.appengine.shared.AuthenticationException instantiate(SerializationStreamReader streamReader) throws SerializationException {
    return new com.google.api.services.samples.calendar.appengine.shared.AuthenticationException();
  }
  
  public static void serialize(SerializationStreamWriter streamWriter, com.google.api.services.samples.calendar.appengine.shared.AuthenticationException instance) throws SerializationException {
    
    com.google.gwt.user.client.rpc.core.java.io.IOException_FieldSerializer.serialize(streamWriter, instance);
  }
  
  public Object create(SerializationStreamReader reader) throws SerializationException {
    return com.google.api.services.samples.calendar.appengine.shared.AuthenticationException_FieldSerializer.instantiate(reader);
  }
  
  public void deserial(SerializationStreamReader reader, Object object) throws SerializationException {
    com.google.api.services.samples.calendar.appengine.shared.AuthenticationException_FieldSerializer.deserialize(reader, (com.google.api.services.samples.calendar.appengine.shared.AuthenticationException)object);
  }
  
  public void serial(SerializationStreamWriter writer, Object object) throws SerializationException {
    com.google.api.services.samples.calendar.appengine.shared.AuthenticationException_FieldSerializer.serialize(writer, (com.google.api.services.samples.calendar.appengine.shared.AuthenticationException)object);
  }
  
}
