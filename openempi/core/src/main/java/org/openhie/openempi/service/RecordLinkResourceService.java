package org.openhie.openempi.service;

import java.util.List;

import org.openhie.openempi.BadRequestException;
import org.openhie.openempi.ConflictException;
import org.openhie.openempi.NotFoundException;
import org.openhie.openempi.model.RecordLink;

public interface RecordLinkResourceService
{
    public List<RecordLink> getRecordLinks(String versionId, Integer entityId, String linkState, Integer firstResult,
            Integer maxResults) throws BadRequestException, NotFoundException;

    public RecordLink loadByRecordLinkId(String versionId, Integer entityId, String recordLinkId)
            throws BadRequestException, NotFoundException;

    public List<RecordLink> loadByRecordId(String versionId, Integer entityId, Long recordId)
            throws BadRequestException, NotFoundException;

    public RecordLink addRecordLink(String versionId, Integer entityId, RecordLink recordLink)
            throws ConflictException, BadRequestException;

    public RecordLink updateRecordLink(String versionId, Integer entityId, RecordLink recordLink)
            throws BadRequestException, ConflictException, NotFoundException;

    public void removeRecordLink(String versionId, Integer entityId, Long recordLinkId)
            throws BadRequestException, ConflictException, NotFoundException;
}
