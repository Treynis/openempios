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
package org.openhie.openempi.persistence;

import java.util.Collection;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.orientechnologies.orient.core.collate.OCaseInsensitiveCollate;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.metadata.schema.OPropertyImpl;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

public class TestOrientDbCreateClassAndIndex extends TestCase
{
    private final static String DB_URL = "remote://localhost/person-db";
    private Logger log = Logger.getLogger(getClass());

    public void testCreateClassAndIndex() {
        OrientGraphNoTx db = new OrientGraphNoTx(DB_URL);
        
        int[] clusterIds = null;
        String className = "personTest";
        OClass vertexClass = findGraphClass(db, "V");
        final OClassImpl sourceClass = (OClassImpl) ((OSchemaProxy) db.getRawGraph().getMetadata().getSchema())
                .createClass(className, vertexClass, clusterIds);
        log.info("Class " + className + " has been assigned cluster " + sourceClass.getDefaultClusterId());
        sourceClass.saveInternal();

        String[] attributes = { "givenName", "familyName", "middleName" };
        for (String fieldName : attributes) {
            OType type = OType.STRING;
            OPropertyImpl prop = (OPropertyImpl) sourceClass.getProperty(fieldName);
            if (prop != null) {
                log.warn("Property '" + className + "." + fieldName + "' already exists.");
                continue;
            }
            prop = (OPropertyImpl) sourceClass.createProperty(fieldName, type);
            log.debug("Adding field " + fieldName + " to class " + className);
            prop.setCollate(new OCaseInsensitiveCollate());
            sourceClass.saveInternal();
        }
        
        db.getRawGraph().getStorage().reload();
        db.getRawGraph().getMetadata().getSchema().reload();
        db.getRawGraph().getMetadata().getIndexManager().reload();
        
        Collection<OClass> classes = db.getRawGraph().getMetadata().getSchema().getClasses();
        StringBuffer sb = new StringBuffer();
        for (OClass clazz : classes) {
            sb.append("Class: " + clazz.getName() + "\n");
        }
        System.out.println("Before creating the indexes the list of classes is: " + sb.toString());
        
        String sql = "CREATE INDEX idx-person-familyName ON personTest (familyName)  NOTUNIQUE";
        log.debug("Creating index: " + sql);
        db.command(new OCommandSQL(sql.toString())).execute(new Object[] {});
    }

    private OClass findGraphClass(OrientBaseGraph db, String className) {
        Collection<OClass> classes = db.getRawGraph().getMetadata().getSchema().getClasses();
        log.debug("The repository currently has " + classes.size() + " classes defined:");
        for (OClass oclass : classes) {
            if (oclass.getName().equalsIgnoreCase(className)) {
                return oclass;
            }
        }
        return null;
    }
}
