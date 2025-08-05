package com.grewmeet.dating.datingcommandservice.service;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingCreated;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingDeleted;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingUpdated;
import com.grewmeet.dating.datingcommandservice.dto.request.CreateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.request.UpdateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;
import com.grewmeet.dating.datingcommandservice.event.OutboxService;
import com.grewmeet.dating.datingcommandservice.repository.DatingMeetingRepository;
import com.grewmeet.dating.datingcommandservice.util.IdParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DatingMeetingServiceImpl implements DatingMeetingService {

    private final DatingMeetingRepository datingMeetingRepository;
    private final OutboxService outboxService;

    @Override
    public DatingMeetingResponse createDatingMeeting(CreateDatingMeetingRequest request) {
        DatingMeeting datingMeeting = DatingMeeting.create(
                request.title(),
                request.description(),
                request.meetingDateTime(),
                request.location(),
                request.maxParticipants()
        );

        DatingMeeting savedDatingMeeting = datingMeetingRepository.save(datingMeeting);
        
        DatingMeetingCreated datingMeetingCreated = DatingMeetingCreated.from(savedDatingMeeting);
        
        outboxService.publishEvent("DatingMeetingCreated", "DatingMeeting", savedDatingMeeting.getId(), datingMeetingCreated);
        
        log.info("Dating meeting created: id={}, title={}", savedDatingMeeting.getId(), savedDatingMeeting.getTitle());
        
        return DatingMeetingResponse.from(savedDatingMeeting);
    }

    @Override
    public DatingMeetingResponse updateDatingMeeting(String eventId, UpdateDatingMeetingRequest request) {
        Long id = IdParser.parseEventId(eventId);
        DatingMeeting datingMeeting = getDatingMeeting(eventId, id);

        datingMeeting.update(
                request.title(),
                request.description(),
                request.meetingDateTime(),
                request.location(),
                request.maxParticipants()
        );

        DatingMeeting updatedDatingMeeting = datingMeetingRepository.save(datingMeeting);

        DatingMeetingUpdated datingMeetingUpdated = DatingMeetingUpdated.from(updatedDatingMeeting);

        outboxService.publishEvent("DatingMeetingUpdated", "DatingMeeting", updatedDatingMeeting.getId(), datingMeetingUpdated);

        log.info("Dating meeting updated: id={}, title={}", updatedDatingMeeting.getId(), updatedDatingMeeting.getTitle());

        return DatingMeetingResponse.from(updatedDatingMeeting);
    }

    @Override
    public void deleteDatingMeeting(String eventId) {
        Long id = IdParser.parseEventId(eventId);
        DatingMeeting datingMeeting = getDatingMeeting(eventId, id);

        if (!datingMeeting.getParticipants().isEmpty()) {
            log.warn("Attempting to delete dating meeting with participants: id={}, participantCount={}",
                    id, datingMeeting.getCurrentParticipantCount());
        }

        DatingMeetingDeleted datingMeetingDeleted = DatingMeetingDeleted.from(datingMeeting);

        outboxService.publishEvent("DatingMeetingDeleted", "DatingMeeting", id, datingMeetingDeleted);

        datingMeetingRepository.delete(datingMeeting);

        log.info("Dating meeting deleted: id={}, title={}, participantCount={}",
                id, datingMeetingDeleted.title(), datingMeetingDeleted.participantIds().size());
    }

    private DatingMeeting getDatingMeeting(String eventId, Long id) {
        DatingMeeting datingMeeting = datingMeetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dating meeting not found: " + eventId));
        return datingMeeting;
    }
}