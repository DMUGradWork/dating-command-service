package com.grewmeet.dating.datingcommandservice.service;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingCreated;
import com.grewmeet.dating.datingcommandservice.dto.request.CreateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;
import com.grewmeet.dating.datingcommandservice.event.OutboxService;
import com.grewmeet.dating.datingcommandservice.repository.DatingMeetingRepository;
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
}