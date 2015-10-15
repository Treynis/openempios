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
package org.openhie.openempi.service;

import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.ReviewRecordPair;

public class ReviewRecordServiceTest extends BaseServiceTestCase
{
	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
	}
	
//	public void testReviewRecordPair() {
//		PersonQueryService queryService = Context.getPersonQueryService();
//
//		PersonManagerService personService = Context.getPersonManagerService();
//		ReviewRecordPair pair = null;
//		
//		try {
//			pair = generateReviewRecordPair(queryService, "PH2009001", "IHENA", "PH2009001", "IHENA");
//			personService.addReviewRecordPair(pair);
//			assertNotNull(pair.getReviewRecordPairId());
//			
//			pair = generateReviewRecordPair(queryService, "PH2009002", "IHENA", "PH2009002", "IHENA");
//			personService.addReviewRecordPair(pair);
//			assertNotNull(pair.getReviewRecordPairId());
//			
//			pair = generateReviewRecordPair(queryService, "PH2009003", "IHENA", "PH2009003", "IHENA");
//			personService.addReviewRecordPair(pair);
//			assertNotNull(pair.getReviewRecordPairId());
//			
//			pair = generateReviewRecordPair(queryService, "PH2009004", "IHENA", "PH2009004", "IHENA");
//			personService.addReviewRecordPair(pair);
//			assertNotNull(pair.getReviewRecordPairId());
//			
//			pair = generateReviewRecordPair(queryService, "PH2009005", "IHENA", "PH2009004", "IHENA");
//			personService.addReviewRecordPair(pair);
//			assertNotNull(pair.getReviewRecordPairId());
//		} catch (ApplicationException e) {
//			assertTrue(true);			
//		}
//		
//		List<ReviewRecordPair> list = queryService.loadAllUnreviewedPersonLinks();
//		assertNotNull(list);
//		assertTrue(list.size() > 0);
//		for (ReviewRecordPair apair : list) {
//			try {
//				System.out.println(apair);
//				apair.setRecordsMatch(false);
//				personService.matchReviewRecordPair(apair);
//			} catch (ApplicationException e) {
//				// This exception should not be thrown because we did set the disposition of the possible match to false.
//				assertTrue(true);
//			}
//		}
//		
//		// Now the list should be emptry because we marked all the un-reviewed records pairs as matched already 
//		// so there should not be any un-reviewed pairs left.
//		//
//		List<ReviewRecordPair> listNew = queryService.loadAllUnreviewedPersonLinks();
//		assertNotNull(listNew);
//		assertTrue(listNew.size() == 0);
//		
//		for (ReviewRecordPair apair : list) {
//			personService.deleteReviewRecordPair(apair);
//		}
//	}
	
	public void testGenerateSampleReviewRecordPairs() {
		PersonQueryService queryService = Context.getPersonQueryService();

		PersonManagerService personService = Context.getPersonManagerService();
		ReviewRecordPair pair = null;
		try {
			pair = generateReviewRecordPair(queryService, "PH2009001", "IHENA", "PH2009007", "IHENA");
			personService.addReviewRecordPair(pair);
			assertNotNull(pair.getReviewRecordPairId());
			
			pair = generateReviewRecordPair(queryService, "PH2009002", "IHENA", "PH2009008", "IHENA");
			personService.addReviewRecordPair(pair);
			assertNotNull(pair.getReviewRecordPairId());
			
			pair = generateReviewRecordPair(queryService, "PH2009003", "IHENA", "PH2009009", "IHENA");
			personService.addReviewRecordPair(pair);
			assertNotNull(pair.getReviewRecordPairId());
			
			pair = generateReviewRecordPair(queryService, "PH2009004", "IHENA", "PH2009010", "IHENA");
			personService.addReviewRecordPair(pair);
			assertNotNull(pair.getReviewRecordPairId());
			
			pair = generateReviewRecordPair(queryService, "PH2009005", "IHENA", "PH2009011", "IHENA");
			personService.addReviewRecordPair(pair);
			assertNotNull(pair.getReviewRecordPairId());
			
			pair = generateReviewRecordPair(queryService, "PH2009006", "IHENA", "PH2009012", "IHENA");
			personService.addReviewRecordPair(pair);
			assertNotNull(pair.getReviewRecordPairId());
		} catch (ApplicationException e) {
			log.error("The addReviewRecordPair method returned an exception: " + e, e);
			assertTrue(true);
		}
		
		List<ReviewRecordPair> list = queryService.loadAllUnreviewedPersonLinks();
		assertNotNull(list);
		assertTrue(list.size() > 0);
		for (ReviewRecordPair apair : list) {
			System.out.println(apair);
		}
	}

//	public void testDeleteAllSampleReviewRecordPairs() {
//		PersonManagerService personService = Context.getPersonManagerService();
//		personService.deleteReviewRecordPairs();
//	}
	
	private ReviewRecordPair generateReviewRecordPair(PersonQueryService queryService, 
			String leftIdentifier, String leftNamespaceIdentifier, String rightIdentifier, String rightNamespaceIdentifier) {
		PersonIdentifier pi = new PersonIdentifier();
		pi.setIdentifier(leftIdentifier);
		IdentifierDomain id = new IdentifierDomain();
		id.setNamespaceIdentifier(leftNamespaceIdentifier);
		pi.setIdentifierDomain(id);
		Person leftPerson = queryService.findPersonById(pi);
		
		pi.setIdentifier(rightIdentifier);
		id = new IdentifierDomain();
		id.setNamespaceIdentifier(rightNamespaceIdentifier);
		pi.setIdentifierDomain(id);
		Person rightPerson = queryService.findPersonById(pi);
		
		ReviewRecordPair pair = new ReviewRecordPair();
		pair.setPersonLeft(leftPerson);
		pair.setPersonRight(rightPerson);
		pair.setDateCreated(new java.util.Date());
		pair.setUserCreatedBy(Context.getUserContext().getUser());
		pair.setLinkSource(new LinkSource(1));
		pair.setWeight(1.0);
		return pair;
	}
}
