/**
 *
 * Copyright (C) 2002-2012 "SYSNET International, Inc."
 * support@sysnetint.com [http://www.sysnetint.com]
 *
 * This file is part of OpenEMPI.
 *
 * OpenEMPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openhie.openempi.entity.dao.orientdb;

import org.openhie.openempi.entity.Constants;
import org.openhie.openempi.model.Entity;

import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;

public interface SchemaManager
{
    static final InternalAttribute DATE_CHANGED_PROPERTY = new InternalAttribute(
            Constants.DATE_CHANGED_PROPERTY, OType.DATETIME, false);
    static final InternalAttribute DATE_CREATED_PROPERTY = new InternalAttribute(
            Constants.DATE_CREATED_PROPERTY, OType.DATETIME, false);
    static final InternalAttribute DATE_REVIEWED_PROPERTY = new InternalAttribute(
            Constants.DATE_REVIEWED_PROPERTY, OType.DATETIME, false);
    static final InternalAttribute DATE_VOIDED_PROPERTY = new InternalAttribute(Constants.DATE_VOIDED_PROPERTY,
            OType.DATETIME, false);
    static final InternalAttribute DIRTY_RECORD_PROPERTY = new InternalAttribute(Constants.DIRTY_RECORD_PROPERTY,
            OType.BOOLEAN, true);
    static final InternalAttribute ENTITY_VERSION_ID_PROPERTY = new InternalAttribute(
            Constants.ENTITY_VERSION_ID_PROPERTY, OType.LONG, false);
    static final InternalAttribute IDENTIFIER_PROPERTY = new InternalAttribute(Constants.IDENTIFIER_PROPERTY,
            OType.STRING, true);
//    static final InternalAttribute IDENTIFIER_SET_PROPERTY = new InternalAttribute(
//            Constants.IDENTIFIER_EDGE_TYPE, OType.LINKSET, false);
    static final InternalAttribute IDENTIFIER_DOMAIN_ID_PROPERTY = new InternalAttribute(
            Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, OType.INTEGER, false);
    static final InternalAttribute LINK_SOURCE_PROPERTY = new InternalAttribute(Constants.LINK_SOURCE_PROPERTY,
            OType.INTEGER, false);
    static final InternalAttribute LINK_STATE_PROPERTY = new InternalAttribute(Constants.LINK_STATE_PROPERTY,
            OType.STRING, true);
    static final InternalAttribute LINK_VECTOR_PROPERTY = new InternalAttribute(Constants.LINK_VECTOR_PROPERTY,
            OType.INTEGER, false);
    static final InternalAttribute LINK_WEIGHT_PROPERTY = new InternalAttribute(Constants.LINK_WEIGHT_PROPERTY,
            OType.DOUBLE, true);
    static final InternalAttribute USER_CHANGED_BY_PROPERTY = new InternalAttribute(
            Constants.USER_CHANGED_BY_PROPERTY, OType.LONG, false);
    static final InternalAttribute USER_CREATED_BY_PROPERTY = new InternalAttribute(
            Constants.USER_CREATED_BY_PROPERTY, OType.LONG, false);
    static final InternalAttribute USER_REVIEWED_BY_PROPERTY = new InternalAttribute(
            Constants.USER_REVIEWED_BY_PROPERTY, OType.LONG, false);
    static final InternalAttribute USER_VOIDED_BY_PROPERTY = new InternalAttribute(
            Constants.USER_VOIDED_BY_PROPERTY, OType.LONG, false);

    static final InternalAttribute[] INTERNAL_ATTRIBUTES = { DATE_CHANGED_PROPERTY, DATE_CREATED_PROPERTY,
            DATE_VOIDED_PROPERTY, DIRTY_RECORD_PROPERTY, ENTITY_VERSION_ID_PROPERTY, // IDENTIFIER_SET_PROPERTY,
            USER_CHANGED_BY_PROPERTY, USER_CREATED_BY_PROPERTY, USER_VOIDED_BY_PROPERTY, 
            };

    static final InternalAttribute[] IDENTIFIER_ATTRIBUTES = { IDENTIFIER_PROPERTY,
            IDENTIFIER_DOMAIN_ID_PROPERTY, USER_CREATED_BY_PROPERTY, USER_VOIDED_BY_PROPERTY,
            DATE_CREATED_PROPERTY, DATE_VOIDED_PROPERTY };

    static final InternalAttribute[] LINK_ATTRIBUTES = { DATE_CREATED_PROPERTY, DATE_REVIEWED_PROPERTY,
            LINK_STATE_PROPERTY, LINK_VECTOR_PROPERTY, LINK_WEIGHT_PROPERTY, LINK_SOURCE_PROPERTY,
            USER_CREATED_BY_PROPERTY, USER_REVIEWED_BY_PROPERTY };
    
    public void createDatabase(EntityStore store, OrientBaseGraph db);
    
    public void dropDatabase(EntityStore store, OrientBaseGraph db);
    
    public void createIndexes(Entity entity, OrientBaseGraph db);
    
    public ConnectionManager getConnectionManager();
    
    public EntityStore getEntityStoreByName(String entityName);
    
    public void createClass(OrientBaseGraph db, Entity entity, String baseClassName);

    public void dropClass(OrientBaseGraph db, String className);
    
    public boolean isClassDefined(OrientBaseGraph db, String className);
    
    public Object getParameter(String key);

    public void initializeSchema(Entity entity, EntityStore store);
    
    public void dropSchema(Entity entity, EntityStore store);

    public boolean isInternalAttribute(String fieldName);
    
    public void removeIndexes(Entity entity, OrientBaseGraph db);
    
    public void setParameter(String key, Object value);
    
    public void shutdownStore(Entity entity);
}
