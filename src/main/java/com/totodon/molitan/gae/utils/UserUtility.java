package com.totodon.molitan.gae.utils;

import java.util.logging.Logger;

import javax.cache.CacheException;

import com.totodon.molitan.gae.managers.GAEPersistanceManager;
import com.totodon.molitan.jdos.IJDO;
import com.totodon.molitan.managers.exceptions.PersistanceServiceException;


public class UserUtility {
	
	private static final Logger log = Logger.getLogger(UserUtility.class.getName());
	
	public static IJDO getUser(Class<?> userClass, String userId) {
		return getUser(userClass, userId, false);
	}
	
	public static IJDO getUser(Class<?> userClass, String userId, boolean useMemCache) {
		IJDO user = null;
		
		try {
			if(useMemCache) {
				user = (IJDO) MemCacheUtility.get(MemCacheUtility.getUniqueId(userClass, userId));
			}
			
			if(user == null) {
				user = (IJDO) GAEPersistanceManager.getInstance().getObject(userClass, userId);
				if(user != null && useMemCache) {
					MemCacheUtility.cache(MemCacheUtility.getUniqueId(userClass, userId), user);
				}
			}
		} catch (Exception e) {
			log.severe(e.getMessage());
			return user;
		}
		
		return user;
	}
	
	public static void saveUser(Class<?> userClass, IJDO user) throws PersistanceServiceException, CacheException {
		saveUser(userClass, user, false);
	}
	
	public static void saveUser(Class<?> userClass, IJDO user, boolean useMemCache) throws PersistanceServiceException, CacheException {
		GAEPersistanceManager.getInstance().nonTransactionalSaveObject(user, useMemCache);
		if(!useMemCache) {
			MemCacheUtility.invalidate(MemCacheUtility.getUniqueId(userClass, user.getId()));
		}
	}
}
