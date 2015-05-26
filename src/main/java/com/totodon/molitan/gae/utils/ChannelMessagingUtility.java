package com.totodon.molitan.gae.utils;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;


public class ChannelMessagingUtility {

	public static void sendMessage(String channelKey, String message) {
		try {
			ChannelService channelService = ChannelServiceFactory
					.getChannelService();
			channelService.sendMessage(new ChannelMessage(channelKey, message));
		} catch(Exception e) {
			//TODO: implement handling
		}
	}
	
	public static String getChannelKey(String userId, String sessionId) {
		String clientId = userId + sessionId;
		return clientId.substring(0, Math.min(clientId.length(), 64));
	}
	
	public static String createChannel(String userId, String roomId) {
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		return channelService.createChannel(ChannelMessagingUtility.getChannelKey(userId, roomId));
	}
}
