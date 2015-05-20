package com.google.api.services.samples.calendar.appengine.shared;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

@SuppressWarnings("deprecation")
public class GwtCalendar_FieldSerializer implements com.google.gwt.user.client.rpc.impl.TypeHandler {
  public static void deserialize(SerializationStreamReader streamReader, com.google.api.services.samples.calendar.appengine.shared.GwtCalendar instance) throws SerializationException {
    instance.id = streamReader.readString();
    instance.title = streamReader.readString();
    
  }
  
  public static com.google.api.services.samples.calendar.appengine.shared.GwtCalendar instantiate(SerializationStreamReader streamReader) throws SerializationException {
    return new com.google.api.services.samples.calendar.appengine.shared.GwtCalendar();
  }
  
  public static void serialize(SerializationStreamWriter streamWriter, com.google.api.services.samples.calendar.appengine.shared.GwtCalendar instance) throws SerializationException {
    streamWriter.writeString(instance.id);
    streamWriter.writeString(instance.title);
    
  }
  
  public Object create(SerializationStreamReader reader) throws SerializationException {
    return com.google.api.services.samples.calendar.appengine.shared.GwtCalendar_FieldSerializer.instantiate(reader);
  }
  
  public void deserial(SerializationStreamReader reader, Object object) throws SerializationException {
    com.google.api.services.samples.calendar.appengine.shared.GwtCalendar_FieldSerializer.deserialize(reader, (com.google.api.services.samples.calendar.appengine.shared.GwtCalendar)object);
  }
  
  public void serial(SerializationStreamWriter writer, Object object) throws SerializationException {
    com.google.api.services.samples.calendar.appengine.shared.GwtCalendar_FieldSerializer.serialize(writer, (com.google.api.services.samples.calendar.appengine.shared.GwtCalendar)object);
  }
  
}
