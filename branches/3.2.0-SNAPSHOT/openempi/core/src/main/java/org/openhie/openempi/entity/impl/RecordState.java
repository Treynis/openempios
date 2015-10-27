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
package org.openhie.openempi.entity.impl;

import java.util.HashSet;
import java.util.Set;

import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.RecordLink;

public class RecordState
{
    private long recordId;
    private String source;
    private String transition;
    private Set<Identifier> preIdentifiers;
    private Set<RecordLink> preLinks;
    private Set<Identifier> postIdentifiers;
    private Set<RecordLink> postLinks;

    public RecordState(long recordId) {
        this.recordId = recordId;
        postLinks = new HashSet<RecordLink>();
        preLinks = new HashSet<RecordLink>();
    }

    public RecordState(long recordId, String source) {
        this(recordId);
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTransition() {
        return transition;
    }

    public void setTransition(String transition) {
        this.transition = transition;
    }

    public Set<Identifier> getPreIdentifiers() {
        return preIdentifiers;
    }

    public void setPreIdentifiers(Set<Identifier> preIdentifiers) {
        this.preIdentifiers = preIdentifiers;
    }

    public Set<RecordLink> getPreLinks() {
        return preLinks;
    }

    public void setPreLinks(Set<RecordLink> preLinks) {
        this.preLinks = preLinks;
    }

    public Set<Identifier> getPostIdentifiers() {
        return postIdentifiers;
    }

    public void setPostIdentifiers(Set<Identifier> postIdentifiers) {
        this.postIdentifiers = postIdentifiers;
    }

    public Set<RecordLink> getPostLinks() {
        return postLinks;
    }

    public void setPostLinks(Set<RecordLink> postLinks) {
        this.postLinks = postLinks;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }
}
