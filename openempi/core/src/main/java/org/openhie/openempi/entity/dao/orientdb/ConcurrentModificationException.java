package org.openhie.openempi.entity.dao.orientdb;

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;

public class ConcurrentModificationException extends RuntimeException
{
    private static final long serialVersionUID = -7629797260452344717L;

    public ConcurrentModificationException(OConcurrentModificationException e) {
        super(e);
    }

}
