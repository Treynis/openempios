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
package org.openhie.openempi.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

public class TestOrientDbQuery
{
    private static Logger log = Logger.getLogger(TestOrientDbQuery.class);
    public static void main(String[] args) {
        OrientGraph database = new OrientGraph("plocal:/mnt/sysnet/person-db", "admin", "admin");
        String[][] pairs = {
                {"11:22", "11:4522"},
                {"11:4522", "11:22"},
                {"11:24", "11:4524"},
                {"11:4524", "11:24"},
                {"11:22", "11:23"},
                {"11:4519", "11:19"},
                {"11:19", "11:4519"},
                {"11:4520", "11:20"},
                {"11:20", "11:4520"}
        };
        for (String[] pair : pairs) {
            System.out.println("Link exists returns: " + alternateLinkExists(database, pair[0], pair[1]));
        }
        database.getRawGraph().close();
    }
    
    private static OSQLSynchQuery<ODocument> buildCommand(OrientGraph db, String oneRid) {
        String query = "select from (traverse * from " + oneRid + ") where @class = 'person'";
        OSQLSynchQuery<ODocument> obj = db.getRawGraph().command(new OSQLSynchQuery<ODocument>(query));
        System.out.println("Object is of type: " + obj.getClass());
        return obj;
    }
    
    public static boolean alternateLinkExists(OrientGraph db, String oneRid, String otherRid) {
        long startTime = new java.util.Date().getTime();
        boolean exists = false;
        String query = "select expand(in) from (select expand(out_) from " + oneRid + ") where source = 3";
        List<ORecordId> result = db.getRawGraph().query(new OSQLSynchQuery<ORecordId>(query));
        String otherNode = "#" + otherRid;
        for (ORecordId odoc : result) {
            String node = odoc.getRecord().getIdentity().toString();
            System.out.println("Node: " + node + " to " + otherNode);
            if (node.equalsIgnoreCase("#"+otherRid))
                exists = true;
        }
        long endTime = new java.util.Date().getTime();
        System.out.println("Checking for link existence took " + (endTime-startTime) + " msec.");
        return exists;
    }

    public static boolean linkExists(OrientGraph db, String oneRid, String otherRid) {
        long startTime = new java.util.Date().getTime();
        boolean exists = false;
        OSQLSynchQuery<ODocument> command = buildCommand(db, oneRid);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("oneRid", oneRid);
        List<ODocument> result = command.execute(params);
        String otherNode = "#" + otherRid;
        for (ODocument odoc : result) {
            String node = odoc.getRecord().getIdentity().toString();
            System.out.println("Node: " + node + " to " + otherNode);
            if (node.equalsIgnoreCase("#"+otherRid))
                exists = true;
        }
        long endTime = new java.util.Date().getTime();
        System.out.println("Checking for link existence took " + (endTime-startTime) + " msec.");
        return exists;
    }

}
