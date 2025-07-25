package com.grewmeet.dating.datingcommandservice.service;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import com.grewmeet.dating.datingcommandservice.dto.event.DatingMeetingCreated;
import com.grewmeet.dating.datingcommandservice.dto.request.CreateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;
import com.grewmeet.dating.datingcommandservice.repository.DatingMeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DatingMeetingService {

    private final DatingMeetingRepository datingMeetingRepository;
    private final OutboxService outboxService;

    public DatingMeetingResponse createDatingMeeting(CreateDatingMeetingRequest request) {
        DatingMeeting datingMeeting = DatingMeeting.create(
                request.title(),
                request.description(),
                request.meetingDateTime(),
                request.location(),
                request.maxParticipants()
        );

        DatingMeeting savedDatingMeeting = datingMeetingRepository.save(datingMeeting);
        
        DatingMeetingCreated datingMeetingCreated = new DatingMeetingCreated(
                savedDatingMeeting.getId(),
                savedDatingMeeting.getTitle(),
                savedDatingMeeting.getDescription(),
                savedDatingMeeting.getMeetingDateTime(),
                savedDatingMeeting.getLocation(),
                savedDatingMeeting.getMaxParticipants(),
                savedDatingMeeting.getCreatedAt()
        );
        
        outboxService.publishEvent("DatingMeetingCreated", "DatingMeeting", savedDatingMeeting.getId(), datingMeetingCreated);
        
        log.info("Dating meeting created: id={}, title={}", savedDatingMeeting.getId(), savedDatingMeeting.getTitle());
        
        return mapToDatingMeetingResponse(savedDatingMeeting);
    }

    private DatingMeetingResponse mapToDatingMeetingResponse(DatingMeeting datingMeeting) {
        return new DatingMeetingResponse(
                datingMeeting.getId(),
                datingMeeting.getTitle(),
                datingMeeting.getDescription(),
                datingMeeting.getMeetingDateTime(),
                datingMeeting.getLocation(),
                datingMeeting.getMaxParticipants(),
                datingMeeting.getCurrentParticipantCount(),
                datingMeeting.getCreatedAt(),
                datingMeeting.getUpdatedAt()
        );
    }
}