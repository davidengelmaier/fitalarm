package com.totodon.molitan.gae.managers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.totodon.molitan.gae.utils.MemCacheUtility;
import com.totodon.molitan.gae.utils.PMF;
import com.totodon.molitan.jdos.IJDO;
import com.totodon.molitan.managers.exceptions.PersistanceServiceException;
import com.totodon.molitan.services.IPersistanceService;

/**
 * 
 * @author David Engelmaier
 *
 */
public final class GAEPersistanceManager implements IPersistanceService
{
	private static GAEPersistanceManager instance = null;
	private static final Logger log = Logger.getLogger(GAEPersistanceManager.class.getName());
	
	private GAEPersistanceManager()
	{
		
	}
	
	public static GAEPersistanceManager getInstance()
	{
		if(instance == null)
		{
			instance = new GAEPersistanceManager();
		}
		
		return instance;
	}
	
	public Object getObject(Class<?> classType, Object id) {
		return getObject(classType, id, false);
	}
	
	public Object getObject(Class<?> classType, Object id, boolean useMemCache) {

		Object result = null;
			
		if(useMemCache) {
			try {
				result = MemCacheUtility.get(MemCacheUtility.getUniqueId(classType, id));
				if(result != null) {
					return result;
				}
			} catch (Exception e) {
				log.severe(e.getMessage());
			}
		}
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
	
		try {
			result = pm.getObjectById(classType, id);
		} catch (Exception e) {
			return null;
		} finally {
			pm.flush();
			pm.close();
		}
		
		return result; 
	}
	
	public IJDO nonTransactionalSaveObject(IJDO jdo) throws PersistanceServiceException {
		return nonTransactionalSaveObject(jdo);
	}
	
	public IJDO nonTransactionalSaveObject(IJDO jdo, boolean useMemCache) throws PersistanceServiceException {
		
		IJDO result = null;
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		try {
			result = pm.makePersistent(jdo);
					
			if(useMemCache) {
				if(jdo instanceof Serializable) {
					try {
						int uniqueMemCacheId = MemCacheUtility.getUniqueId(jdo.getClass(), result.getId());
						MemCacheUtility.invalidate(uniqueMemCacheId);
						MemCacheUtility.cache(uniqueMemCacheId, jdo);
					} catch (Exception e) {
						log.severe(e.getMessage());
					}
				} else {
					throw new PersistanceServiceException(jdo.toString() + " doesn't implement the java.io.Serializable interface");
				}
			}
			
		} catch (Exception e) {
			throw new PersistanceServiceException(e.getMessage());
		} finally {
        	pm.flush();
            pm.close();
        }
        
        return result;
	}
	
	public Object saveObject(IJDO jdo) throws PersistanceServiceException {
		return saveObjects(new IJDO[]{jdo}).get(0);
	}
	
	public Object saveObject(IJDO jdo, boolean useMemCache) throws PersistanceServiceException {
		return saveObjects(new IJDO[]{jdo}, useMemCache).get(0);
	}
	
	public List<Object> saveObjects(List<IJDO> jdos) throws PersistanceServiceException {
		IJDO[] jdosArray = new IJDO[jdos.size()];
		return saveObjects(jdos.toArray(jdosArray));
	}
	
	public List<Object> saveObjects(List<IJDO> jdos, boolean useMemCache) throws PersistanceServiceException {
		IJDO[] jdosArray = new IJDO[jdos.size()];
		return saveObjects(jdos.toArray(jdosArray), useMemCache);
	}
	
	public List<Object> saveObjects(IJDO[] jdos) throws PersistanceServiceException {
		return saveObjects(jdos, false);
	}
	
	public List<Object> saveObjects(IJDO[] jdos, boolean useMemCache) throws PersistanceServiceException {
		
		// TODO: implement automatic rollback in case of more than 5 entity groups save interuption
		
		List<Object> result = new ArrayList<Object>();
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		try {
			pm.currentTransaction().begin();
			
			int i = 0;
			for(IJDO jdo : jdos) {
				Object id = pm.makePersistent(jdo).getId();
				result.add(id);
				/* This is a hack since more than 5 entity groups in a transaction throw an exception */
				if(i++ == 4) {
					i = 0;
					pm.currentTransaction().commit();
					
					if(useMemCache) {
						if(jdo instanceof Serializable) {
							try {
								int uniqueMemCacheId = MemCacheUtility.getUniqueId(jdo.getClass(), id);
								MemCacheUtility.invalidate(uniqueMemCacheId);
								MemCacheUtility.cache(uniqueMemCacheId, jdo);
							} catch (Exception e) {
								log.severe(e.getMessage());
							}
						} else {
							throw new PersistanceServiceException(jdo.toString() + " doesn't implement the java.io.Serializable interface");
						}
					}
					
					pm.currentTransaction().begin();
				}
			}
			
			if(i <= 4) {
				pm.currentTransaction().commit();
			}
			
		} catch (Exception e) {
			if(pm.currentTransaction().isActive()) {
        		pm.currentTransaction().rollback();
        	}
			throw new PersistanceServiceException(e.getMessage());
		} finally {
        	pm.flush();
            pm.close();
        }
        
        return result;
	}
	
	public void deleteObjects(Class<?> classType, List<Object> ids) throws PersistanceServiceException {
		Object[] idsArray = new Object[ids.size()];
		deleteObjects(classType, ids.toArray(idsArray));
	}
	
	public void deleteObjects(Class<?> classType, List<Object> ids, boolean useMemCache) throws PersistanceServiceException {
		Object[] idsArray = new Object[ids.size()];
		deleteObjects(classType, ids.toArray(idsArray), useMemCache);
	}
	
	public void deleteObjects(Class<?> classType, Object[] ids) throws PersistanceServiceException {
		for(Object id : ids) {
			deleteObject(classType, id);
		}
	}
	
	public void deleteObjects(Class<?> classType, Object[] ids, boolean useMemCache) throws PersistanceServiceException {
		for(Object id : ids) {
			deleteObject(classType, id, useMemCache);
		}
	}
	
	public void deleteObject(Class<?> classType, Object id) throws PersistanceServiceException {
		deleteObject(classType, id, false);
	}
	
	public void deleteObject(Class<?> classType, Object id, boolean useMemCache) throws PersistanceServiceException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try 
		{
			Object object = pm.getObjectById(classType, id);
			
			pm.currentTransaction().begin();
			pm.deletePersistent(object);
			pm.currentTransaction().commit();
			
			if(useMemCache) {
				try {
					int uniqueMemCacheId = MemCacheUtility.getUniqueId(classType, id);
					MemCacheUtility.invalidate(uniqueMemCacheId);
				} catch (Exception e) {
					log.severe(e.getMessage());
				}
			}
		}
		catch(Exception e)
		{
			if(pm.currentTransaction().isActive())
        	{
				pm.currentTransaction().rollback();
        	}
        	throw new PersistanceServiceException(e.getMessage());
		}
		finally
		{
			pm.flush();
			pm.close();
		}
	}
	
	public void deleteObjects(IJDO[] jdos) throws PersistanceServiceException {
		for(IJDO jdo : jdos) {
			deleteObject(jdo, false);
		}
	}
	
	public void deleteObjects(IJDO[] jdos, boolean useMemCache) throws PersistanceServiceException {
		for(IJDO jdo : jdos) {
			deleteObject(jdo, useMemCache);
		}
	}
	
	public void deleteObjects(List<IJDO> jdos) throws PersistanceServiceException {
		IJDO[] jdosArray = new IJDO[jdos.size()];
		deleteObjects(jdos.toArray(jdosArray));
	}
	
	public void deleteObjects(List<IJDO> jdos, boolean useMemCache) throws PersistanceServiceException {
		IJDO[] jdosArray = new IJDO[jdos.size()];
		deleteObjects(jdos.toArray(jdosArray), useMemCache);
	}
	
	public void deleteObject(IJDO jdo) throws PersistanceServiceException {
		deleteObject(jdo, false);
	}
	
	public void deleteObject(IJDO jdo, boolean useMemCache) throws PersistanceServiceException {
		deleteObject(jdo.getClass(), jdo.getId());
	}
	
	public void deleteQuery(Class<?> classType, String filter) throws PersistanceServiceException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		try {
			pm.currentTransaction().begin();
			Query query = pm.newQuery(classType);
			query.setFilter(filter);
			query.deletePersistentAll();
			pm.currentTransaction().commit();
		} catch (Exception e) {
			if(pm.currentTransaction().isActive())
        	{
				pm.currentTransaction().rollback();
        	}
			throw new PersistanceServiceException(e.getMessage());
		} finally {
			pm.flush();
			pm.close();
		}
	}
	
	public Object executeQuery(Class<?> classType, String filter) throws PersistanceServiceException {
		return executeQuery(classType, filter, null, null, null, false);
	}
	
	public Object executeQuery(Class<?> classType, String filter, String ordering, String grouping, String range, boolean unique) throws PersistanceServiceException {
		Object result = null;
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(classType);
		if(filter != null) {
			query.setFilter(filter);
		}
		if(ordering != null) {
			query.setOrdering(ordering);
		}
		if(grouping != null) {
			query.setGrouping(grouping);
		}
		if(range != null) {
			query.setRange(range);
		}
		query.setUnique(unique);
		
		try 
		{
			pm.currentTransaction().begin();
			result = query.execute();
			pm.currentTransaction().commit();
		}
		catch(Exception e)
		{
			if(pm.currentTransaction().isActive())
        	{
				pm.currentTransaction().rollback();
        	}
        	throw new PersistanceServiceException(e.getMessage());
		}
		finally
		{
			pm.flush();
			pm.close();
		}
		
		return result;
	}
	
	public Object nonTransactionalExecuteQuery(Class<?> classType, String filter, String ordering, String grouping, String range, boolean unique) throws PersistanceServiceException {
		Object result = null;
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(classType);
		if(filter != null) {
			query.setFilter(filter);
		}
		if(ordering != null) {
			query.setOrdering(ordering);
		}
		if(grouping != null) {
			query.setGrouping(grouping);
		}
		if(range != null) {
			query.setRange(range);
		}
		query.setUnique(unique);
		
		try 
		{
			result = query.execute();
		}
		catch(Exception e)
		{
        	throw new PersistanceServiceException(e.getMessage());
		}
		finally
		{
			pm.flush();
			pm.close();
		}
		
		return result;
	}
}
