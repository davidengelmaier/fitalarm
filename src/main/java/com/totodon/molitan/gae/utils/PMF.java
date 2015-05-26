package com.totodon.molitan.gae.utils;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

public final class PMF 
{
    private static final PersistenceManagerFactory PMF_INSTANCE =
        JDOHelper.getPersistenceManagerFactory("transactions-optional");

    private PMF() {}

    public static PersistenceManagerFactory get() 
    {
        return PMF_INSTANCE;
    }
}