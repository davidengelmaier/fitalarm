package com.totodon.molitan.gae.utils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class ObjectConvertor 
{
	public static byte[] getBytes(Object obj) throws java.io.IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
	    ObjectOutputStream oos = new ObjectOutputStream(bos); 
	    oos.writeObject(obj);
	    oos.flush(); 
	    oos.close(); 
	    bos.close();
	    byte [] data = bos.toByteArray();
	    
	    return data;
	}

	public static Object getObject(byte[] bytes) throws java.io.IOException, ClassNotFoundException
	{
		Object object = null;
		object = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes)).readObject();

		return object;
	}
}
