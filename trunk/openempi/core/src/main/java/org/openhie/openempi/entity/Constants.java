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
package org.openhie.openempi.entity;

public class Constants
{
	public final static String DATE_CREATED_PROPERTY = "dateCreated";
	public final static String DATE_CHANGED_PROPERTY = "dateChanged";
	public final static String DATE_REVIEWED_PROPERTY = "dateReviewed";
	public final static String DATE_VOIDED_PROPERTY = "dateVoided";
	public final static String DIRTY_RECORD_PROPERTY = "_dirty";
	public final static String ENTITY_VERSION_ID_PROPERTY = "entityVersionId";
	public final static String IDENTIFIER_PROPERTY = "identifier";
	public final static String IDENTIFIERS_PROPERTY = "identifiers";
    public final static String IDENTIFIER_EDGE_TYPE = "identifierEdge";
    public final static String IDENTIFIER_IN_PROPERTY = "in_" + IDENTIFIER_EDGE_TYPE;
    public final static String IDENTIFIER_OUT_PROPERTY = "out_" + IDENTIFIER_EDGE_TYPE;
	public final static String IDENTIFIER_DOMAIN_ID_PROPERTY = "identifierDomainId";
	public final static String LINK_SOURCE_PROPERTY = "source";
	public final static String LINK_STATE_PROPERTY = "state";
	public final static String LINK_VECTOR_PROPERTY = "vector";
	public final static String LINK_WEIGHT_PROPERTY = "weight";
	public final static String ORIENTDB_CLUSTER_ID_KEY = "ORIENTDB_CLUSTER_ID";
    public final static String RECORD_PROPERTY = "record";
	public final static String RECORD_ID_PROPERTY = "recordId";
    /**
     * When we return arbitrary records from the graph database in the form of a map per record, this
     * key into the map stores the unique identity for the record regardless of the underlying
     * store used.
     */
    public static final String RECORDID_KEY = "@RECORDIDENTITY";
    public static final String CLUSTERID_KEY = "@CLUSTERID";
    public static final String CLUSTERPOSITION_KEY = "@CLUSTERPOSITION";
    
	public final static String USER_CHANGED_BY_PROPERTY = "userChangedBy";
	public final static String USER_CREATED_BY_PROPERTY = "userCreatedBy";
	public final static String USER_REVIEWED_BY_PROPERTY = "userReviewedBy";
	public final static String USER_VOIDED_BY_PROPERTY = "userVoidedBy";
	public final static String LOCAL_STORAGE_MODE = "local";
	public final static String PLOCAL_STORAGE_MODE = "plocal";
	public final static String REMOTE_STORAGE_MODE = "remote";
	public final static String DATA_DIRECTORY_KEY = "dataDirectory";
	public final static String INDEX_NAME_PREFIX = "idx-";
	public final static String VERTEX_CLASS_NAME = "V";
	public final static String EDGE_CLASS_NAME = "E";
	public final static String GRAPH_DATABASE_TYPE = "graph";
	public final static String RECORD_LINK_TYPE = "recordLink";
	public final static String IDENTIFIER_TYPE = "identifier";
    public final static String EDGE_IN_PROPERTY = "in";
    public final static String EDGE_OUT_PROPERTY = "out";
    public final static String VERTEX_IN_PROPERTY = "in_" + RECORD_LINK_TYPE;
    public final static String VERTEX_OUT_PROPERTY = "out_" + RECORD_LINK_TYPE;
}
