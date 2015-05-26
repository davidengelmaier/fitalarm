package com.totodon.molitan.managers;

import com.totodon.molitan.gae.managers.GAEPersistanceManager;
import com.totodon.molitan.services.IPersistanceService;

public class PersistanceManager {
	public static IPersistanceService getPersistanceService() {
		return GAEPersistanceManager.getInstance();
	}
}
