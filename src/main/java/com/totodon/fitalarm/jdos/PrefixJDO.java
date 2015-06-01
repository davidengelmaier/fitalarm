package com.totodon.fitalarm.jdos;

import com.totodon.molitan.jdos.IJDO;

import javax.jdo.annotations.*;
import java.io.Serializable;

/**
 * Created by davidengelmaier on 26/05/15.
 */
@SuppressWarnings("serial")
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PrefixJDO implements IJDO, Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private String id;

    @Persistent
    private String prefix;

    public PrefixJDO(String id, String prefix) {
        this.id = id;
        this.prefix = prefix;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
