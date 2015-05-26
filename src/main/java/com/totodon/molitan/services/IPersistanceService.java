package com.totodon.molitan.services;

import java.util.List;

import com.totodon.molitan.jdos.IJDO;
import com.totodon.molitan.managers.exceptions.PersistanceServiceException;


public interface IPersistanceService {
	
	Object getObject(Class<?> classType, Object id);
	
	Object getObject(Class<?> classType, Object id, boolean useMemCache);

	Object saveObject(IJDO jdo) throws PersistanceServiceException;
	
	Object saveObject(IJDO jdo, boolean useMemCache) throws PersistanceServiceException;
	
	List<Object> saveObjects(List<IJDO> jdos) throws PersistanceServiceException;
	
	List<Object> saveObjects(List<IJDO> jdos, boolean useMemCache) throws PersistanceServiceException;
	
	List<Object> saveObjects(IJDO[] jdos) throws PersistanceServiceException;
	
	List<Object> saveObjects(IJDO[] jdos, boolean useMemCache) throws PersistanceServiceException;
		
	void deleteObjects(Class<?> classType, List<Object> ids) throws PersistanceServiceException;
	
	void deleteObjects(Class<?> classType, List<Object> ids, boolean useMemCache) throws PersistanceServiceException;
	
	void deleteObjects(Class<?> classType, Object[] ids) throws PersistanceServiceException;
	
	void deleteObjects(Class<?> classType, Object[] ids, boolean useMemCache) throws PersistanceServiceException;
	
	void deleteObject(Class<?> classType, Object id) throws PersistanceServiceException;
	
	void deleteObject(Class<?> classType, Object id, boolean useMemCache) throws PersistanceServiceException;
	
	void deleteObjects(IJDO[] jdos) throws PersistanceServiceException;
	
	void deleteObjects(IJDO[] jdos, boolean useMemCache) throws PersistanceServiceException;
	
	void deleteObjects(List<IJDO> jdos) throws PersistanceServiceException;
	
	void deleteObjects(List<IJDO> jdos, boolean useMemCache) throws PersistanceServiceException;
	
	void deleteObject(IJDO jdo) throws PersistanceServiceException;
	
	void deleteObject(IJDO jdo, boolean useMemCache) throws PersistanceServiceException;
	
	void deleteQuery(Class<?> classType, String filter) throws PersistanceServiceException;
	
	Object executeQuery(Class<?> classType, String filter) throws PersistanceServiceException;
	
	Object executeQuery(Class<?> classType, String filter, String ordering, String grouping, String range, boolean unique) throws PersistanceServiceException;
}
