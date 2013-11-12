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

import java.util.Map;
import java.util.List;

import org.apache.log4j.Logger;
import org.openhie.openempi.entity.Constants;
import org.openhie.openempi.model.AttributeDatatype;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLinkState;

public class QueryGenerator
{
	private static final String DATE_FORMAT_STRING = "'%1$tY-%1$tm-%1$td'";
	private static final String DATETIME_FORMAT_STRING = "'%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS'";
	private static Logger log = Logger.getLogger(QueryGenerator.class);

	public static String generateQueryFromRecordIds(Entity entity, int clusterId,  List<Integer> recordIds) {
		
		StringBuffer query = new StringBuffer("select from " + entity.getName() + " where dateVoided is null and @rid in ");
		boolean start = true;
		for (Integer recordId : recordIds) {			
			 if(start ) {
			    query.append("[");
			    start = false;
			 } else {
				query.append(",");
			 }
			 query.append("#")
			.append(clusterId)
			.append(":")
			.append(recordId);	
		}
		 query.append("]");
		
		if (log.isDebugEnabled()) {
			log.debug("Generated query: " + query.toString());
		}
		return query.toString();
	}	
	
	public static String generateQueryFromRecord(Entity entity, Record record,  Map<String, Object> params, int firstResult, int maxResults) {
		StringBuffer query = new StringBuffer("select from " + entity.getName() + " where dateVoided is null");
		
		for (String property : record.getPropertyNames()) {
			if (record.get(property) == null) {
				continue;
			}
			EntityAttribute attrib = entity.findAttributeByName(property);
			if (attrib == null) {
				log.error("This should not occur; a query record has property " + property + " that is not defined in the entity: " + entity.getName());
				continue;
			}
			addCriterionToQuery(attrib, record.get(property), query, params);
		}
		addPagingModifiersToQuery(firstResult, maxResults, query);

		if (log.isDebugEnabled()) {
			log.debug("Generated query: " + query.toString());
		}
		return query.toString();
	}

	public static String generateCountQueryFromRecordLinks(RecordLinkState state) {
		StringBuffer query = new StringBuffer("select count(*) from recordLink");
		if (state != null) {
			query.append(" where state = '").append(state.getState().toString()).append("'");
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Generated query: " + query.toString());
		}
		return query.toString();
	}	
	
	public static String generateQueryForRecordLinks(RecordLinkState state, int firstResult, int maxResults) {
		StringBuffer query = new StringBuffer("select from recordLink");
		if (state != null) {
			query.append(" where state = '").append(state.getState().toString()).append("'");
		}
		addPagingModifiersToQuery(firstResult, maxResults, query);

		if (log.isDebugEnabled()) {
			log.debug("Generated query: " + query.toString());
		}
		return query.toString();
	}

	public static String generateQueryForRecordLink(String recordLinkId) {
		StringBuffer query = new StringBuffer("select from " + recordLinkId);
		return query.toString();
	}

	public static String generateQueryForRecordLink(LinkSource linkSource, RecordLinkState state) {
		StringBuffer query = new StringBuffer("select from recordLink where source = " + linkSource.getLinkSourceId());
		if (state != null) {
			query.append(" and state = '" + state.getState() + "'");
		}
		return query.toString();
	}
	
	public static String generateQueryForRecordLinkByRecordId(String recordId) {
		StringBuffer query = new StringBuffer("select from recordLink where in = " + recordId);
		return query.toString();
	}

	public static String generateQueryForRecordLinks(Entity entity, String recordId) {
		StringBuffer query = new StringBuffer("select from (traverse ");
		query.append(entity.getName()).append(".in_, recordLink.out from ");
		query.append(recordId);
		query.append(") where @class = 'recordLink' and state = 'M'");
		return query.toString();
	}

	private static void addPagingModifiersToQuery(int firstResult, int maxResults, StringBuffer query) {
		// Add paging modifiers
		if (firstResult > 0) {
			query.append(" skip " + firstResult);
		}
		if (maxResults > 0) {
			query.append(" limit " + maxResults);
		}
	}	

	public static String generateCountQueryFromRecord(Entity entity, Record record,  Map<String, Object> params) {
		StringBuffer query = new StringBuffer("select count(*) from " + entity.getName() + " where dateVoided is null");
		
		for (String property : record.getPropertyNames()) {
			if (record.get(property) == null) {
				continue;
			}
			EntityAttribute attrib = entity.findAttributeByName(property);
			if (attrib == null) {
				log.error("This should not occur; a query record has property " + property + " that is not defined in the entity: " + entity.getName());
				continue;
			}
			addCriterionToQuery(attrib, record.get(property), query, params);
		}

		if (log.isDebugEnabled()) {
			log.debug("Generated query: " + query.toString());
		}
		return query.toString();
	}	

	public static String generateCountQueryFromRecord(Entity entity, Identifier identifier, Map<String, Object> params) {
	    StringBuffer query = new StringBuffer("select from identifier where dateVoided is null ");
	
		if (identifier.getIdentifier() != null) {
			String value = identifier.getIdentifier();
			query.append(" and ").append(Constants.IDENTIFIER_PROPERTY)
				.append(" ")
				.append(chooseWhereClauseOperator(value))
				.append(" ")
				.append(":").append(Constants.IDENTIFIER_PROPERTY);
                params.put(Constants.IDENTIFIER_PROPERTY, value);
		}
		if (identifier.getIdentifierDomain() != null ) {
				Integer integerValue = identifier.getIdentifierDomain().getIdentifierDomainId();
				if( integerValue != null ) {
					query.append(" and ").append(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY)
					.append(" ")
					.append('=')
					.append(" ")
					.append(":").append(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY);
                    params.put(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, integerValue);
				}
		}
		
		
		if (log.isDebugEnabled()) {
			log.debug("Generated query: " + query.toString());
		}
		return query.toString();
	}	
	
	public static String generateRecordIdQuery(Entity entity) {
		String query = "select @rid from " + entity.getName();
		return query;
	}

	public static String generateLoadQueryFromRecordId(String rid) {
		String query = "select from " + rid + " where dateVoided is null";
		return query;
	}
	
	public static String generateRecordQueryByIdentifier(Entity entity, Identifier identifier, Map<String, Object> params) {
		StringBuffer query = new StringBuffer("select from identifier where dateVoided is null ");
		if (identifier.getIdentifier() != null) {
			String value = identifier.getIdentifier();
			query.append(" and ").append(Constants.IDENTIFIER_PROPERTY)
				.append(" ")
				.append(chooseWhereClauseOperator(value))
				.append(" ")
				.append(":").append(Constants.IDENTIFIER_PROPERTY);
                params.put(Constants.IDENTIFIER_PROPERTY, value);
		}
		if (identifier.getIdentifierDomain() != null ) {
			Integer value = identifier.getIdentifierDomain().getIdentifierDomainId();
			if( value != null ) {
				query.append(" and ").append(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY)
				    .append(" ")
				 	.append('=')
				 	.append(" ")
				 	.append(":").append(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY);
	                params.put(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, value);
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Generated query: " + query.toString());
		}
		return query.toString();
	}

	public static String generateRecordQueryPaged(Entity entity, int firstResult, int maxResults) {
		StringBuffer query = new StringBuffer("select from " + entity.getName() + " where dateVoided is null");
		addPagingModifiersToQuery(firstResult, maxResults, query);

		if (log.isDebugEnabled()) {
			log.debug("Generated query: " + query.toString());
		}
		return query.toString();
	}

	public static String generateRecordQueryByIdentifier(Entity entity, Identifier identifier, Map<String, Object> params, int firstResult, int maxResults) {
		StringBuffer query = new StringBuffer("select from identifier where dateVoided is null ");
		if (identifier.getIdentifier() != null) {			
			String value = identifier.getIdentifier();
			query.append(" and ").append(Constants.IDENTIFIER_PROPERTY)
				.append(" ")
				.append(chooseWhereClauseOperator(value))
				.append(" ")
				.append(":").append(Constants.IDENTIFIER_PROPERTY);
                params.put(Constants.IDENTIFIER_PROPERTY, value);
		 }

		if (identifier.getIdentifierDomain() != null ) {
				Integer integerValue = identifier.getIdentifierDomain().getIdentifierDomainId();
				if( integerValue != null ) {
					query.append(" and ")
					.append(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY)
					.append(" ")
					.append('=')
					.append(" ")
					.append(":").append(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY);
                    params.put(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, integerValue);
				}
		}
		
		addPagingModifiersToQuery(firstResult, maxResults, query);

		if (log.isDebugEnabled()) {
			log.debug("Generated query: " + query.toString());
		}
		return query.toString();
	}

    public static String generateRecordQueryNotInIdentifierDomain(Entity entity, Integer identifierDomainId,
            boolean hasLinks, Map<String, Object> params, int firstResult, int maxResults) {
        StringBuffer query = new StringBuffer("select from ");
        query.append(entity.getName()).append(" where dateVoided is null ");
        if (identifierDomainId != null ) {
            query
                .append(" and identifierSet contains (")
                .append(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY)
                .append(" <> :")
                .append(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY)
                .append(")");
            params.put(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, identifierDomainId);
        }

        if (hasLinks) {
            query.append(" and in_ is not null");
        } else {
            query.append(" and in_ is null");
        }
        
        addPagingModifiersToQuery(firstResult, maxResults, query);

        if (log.isDebugEnabled()) {
            log.debug("Generated query: " + query.toString());
        }
        return query.toString();
    }
	
	public static String generateQueryFromRecord(Entity entity, Record record, Map<String, Object> params) {
		return generateQueryFromRecord(entity, record, params, 0, -1);
	}

	private static void addCriterionToQuery(EntityAttribute attrib, Object value, StringBuffer query, Map<String, Object> params) {
		query.append(" and ");
		switch(AttributeDatatype.getById(attrib.getDatatype().getDatatypeCd())) {
			case BOOLEAN:
			case INTEGER:
			case LONG:
			case SHORT:
			case DOUBLE:
			case FLOAT:
				query.append(attrib.getName())
				    .append(" ")
					.append("=")
					.append(" ")
					.append(":").append((attrib.getName()));
                    params.put(attrib.getName(), value);
				break;
			case DATE:
				query.append("format(")
					.append(DATE_FORMAT_STRING)
					.append(",").append(attrib.getName()).append(") ")
					.append(chooseWhereClauseOperator(value))
					.append(" ")
					.append(":").append((attrib.getName()));
                    params.put(attrib.getName(), value);
				break;
			case STRING:
				query.append(attrib.getName())
					.append(" ")
					.append(chooseWhereClauseOperator(value))
					.append(" ")
					.append(":").append((attrib.getName()));
                    params.put(attrib.getName(), (String)value);
				break;
			case TIMESTAMP:
				query.append("format(")
				.append(DATETIME_FORMAT_STRING)
				.append(",").append(attrib.getName()).append(") ")
				.append(chooseWhereClauseOperator(value))
				.append(" ")
				.append(":").append((attrib.getName()));
                params.put(attrib.getName(), value);
				break;
		}
	}

	private static Object chooseWhereClauseOperator(Object value) {
		String strValue = value.toString();
		if (strValue.indexOf('%') >= 0 || strValue.indexOf('_') >= 0) {
			return "like";
		}
		return '=';
	}
}
