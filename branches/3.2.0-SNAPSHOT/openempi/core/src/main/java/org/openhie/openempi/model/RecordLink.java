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
package org.openhie.openempi.model;

import java.util.Date;

public class RecordLink extends BaseObject
{
	private static final long serialVersionUID = -1636162770470811564L;
	
	private String recordLinkId;
	private Record leftRecord;
	private Record rightRecord;
	private Date dateCreated;
	private Date dateReviewed;
	private User userCreatedBy;
	private User userReviewedBy;
	private Double weight;
	private Integer vector;
	private LinkSource linkSource;
	private RecordLinkState state;
	
	public RecordLink() {
	}
	
	public RecordLink(String recordLinkId) {
		this.recordLinkId = recordLinkId;
	}

	public String getRecordLinkId() {
		return recordLinkId;
	}

	public void setRecordLinkId(String entityLinkId) {
		this.recordLinkId = entityLinkId;
	}

	public Record getLeftRecord() {
		return leftRecord;
	}

	public void setLeftRecord(Record leftRecord) {
		this.leftRecord = leftRecord;
	}

	public Record getRightRecord() {
		return rightRecord;
	}

	public void setRightRecord(Record rightRecord) {
		this.rightRecord = rightRecord;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateReviewed() {
		return dateReviewed;
	}

	public void setDateReviewed(Date dateReviewed) {
		this.dateReviewed = dateReviewed;
	}

	public User getUserCreatedBy() {
		return userCreatedBy;
	}

	public void setUserCreatedBy(User userCreatedBy) {
		this.userCreatedBy = userCreatedBy;
	}

	public User getUserReviewedBy() {
		return userReviewedBy;
	}

	public void setUserReviewedBy(User userReviewedBy) {
		this.userReviewedBy = userReviewedBy;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public Integer getVector() {
		return vector;
	}

	public void setVector(Integer vector) {
		this.vector = vector;
	}

	public LinkSource getLinkSource() {
		return linkSource;
	}

	public void setLinkSource(LinkSource linkSource) {
		this.linkSource = linkSource;
	}

	public RecordLinkState getState() {
		return state;
	}

	public void setState(RecordLinkState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "EntityLink [entityLinkId=" + recordLinkId + ", leftRecord=" + leftRecord + ", rightRecord="
				+ rightRecord + ", dateCreated=" + dateCreated + ", dateReviewed=" + dateReviewed + ", userCreatedBy="
				+ userCreatedBy + ", userReviewedBy=" + userReviewedBy + ", weight=" + weight + ", vector=" + vector
				+ ", linkSource=" + linkSource + ", state=" + state + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecordLink other = (RecordLink) obj;
		if (recordLinkId == null) {
			if (other.recordLinkId != null)
				return false;
		} else if (!recordLinkId.equals(other.recordLinkId)) {
			return false;
		}
		
		// Check the case were either one or both of the leftRecords are null
		if (leftRecord == null) {
			if (other.leftRecord != null) {
				return false;
			} else {
				return true;
			}
		} else if (other.leftRecord == null) {
			return false;
		}
		
		// Check the case were either one or both of the rightRecords are null
		if (rightRecord == null) {
			if (other.rightRecord != null) {
				return false;
			} else {
				return true;
			}
		} else if (other.rightRecord == null) {
			return false;
		}
		
		// Check the case were either one or both of the leftRecord.recordIds are null
		if (leftRecord.getRecordId() == null) {
			if (other.leftRecord.getRecordId() != null) {
				return false;
			}
		}

		// Check the case were either one or both of the leftRecord.recordIds are null
		if (rightRecord.getRecordId() == null) {
			if (other.rightRecord.getRecordId() != null) {
				return false;
			}
		}

		if (leftRecord.getRecordId() == null && other.leftRecord.getRecordId() == null &&
				rightRecord.getRecordId() == null && other.rightRecord.getRecordId() == null) {
			return true;
		}
		
		if (leftRecord.getRecordId().equals(other.leftRecord.getRecordId()) &&
				rightRecord.getRecordId().equals(other.rightRecord.getRecordId())) {
			return true;
		}
		
		if (leftRecord.getRecordId().equals(other.rightRecord.getRecordId()) &&
				rightRecord.getRecordId().equals(other.leftRecord.getRecordId())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((recordLinkId == null) ? 0 : recordLinkId.hashCode());
		result = prime * result + ((leftRecord == null) ? 0 : leftRecord.hashCode());
		result = prime * result + ((rightRecord == null) ? 0 : rightRecord.hashCode());
		return result;
	}
}
