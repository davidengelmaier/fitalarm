package com.totodon.molitan.gae.utils;

import java.io.IOException;
import java.util.Collections;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;

import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class MemCacheUtility {
		
	public static void invalidate(Object key) throws CacheException {
		Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
		cache.remove(key);
	}
	
	@SuppressWarnings("unchecked")
	public static void cache(Object key, Object value) throws CacheException, IOException {
		Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
		cache.put(key, ObjectConvertor.getBytes(value));
	}
	
	public static Object get(Object id) throws CacheException, IOException, ClassNotFoundException {
		Object result = null;
		Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
		Object value = cache.get(id);
		if(value != null) {
			result = ObjectConvertor.getObject((byte[]) value);
		}
		
		return result;
	}
	
	public static int getUniqueId(Class<?> classType, Object id) {
		return (classType.getClass().getName() + id.toString()).hashCode();
	}
	
	public static void clearAll() {
		MemcacheServiceFactory.getMemcacheService().clearAll();
	}
}
