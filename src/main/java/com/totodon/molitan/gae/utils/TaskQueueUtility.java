package com.totodon.molitan.gae.utils;

import java.util.Enumeration;
import java.util.Hashtable;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;

public class TaskQueueUtility {
	
	public static void addTask(String queueName, String taskURL, Hashtable<String, String> params) {
		
		Queue queue = queueName == null ? QueueFactory.getDefaultQueue() : QueueFactory.getQueue(queueName);
		
		String url = taskURL + "?";
		
		if(params != null) {
			Enumeration<String> keys = params.keys();
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				String value = params.get(key);
				
				url += key + "=" + value + (keys.hasMoreElements() ? "&" : "");
			}
		}
		queue.add(Builder.withUrl(url).method(TaskOptions.Method.GET));
	}
	
	public static void addTask(String queueName, String taskURL) {
		addTask(queueName, taskURL, null);
	}
	
	public static void addTask(String taskURL) {
		addTask(null, taskURL, null);
	}
	
	public static void addTask(String taskURL, Hashtable<String, String> params) {
		addTask(null, taskURL, params);
	}
}
