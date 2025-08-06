package com.grewmeet.dating.datingcommandservice.service;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import com.grewmeet.dating.datingcommandservice.domain.Participant;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingCreated;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingDeleted;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingUpdated;
import com.grewmeet.dating.datingcommandservice.saga.ParticipantJoinedEvent;
import com.grewmeet.dating.datingcommandservice.saga.ParticipantLeftEvent;
import com.grewmeet.dating.datingcommandservice.dto.request.CreateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.request.JoinEventRequest;
import com.grewmeet.dating.datingcommandservice.dto.request.UpdateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;
import com.grewmeet.dating.datingcommandservice.dto.response.ParticipantResponse;
import com.grewmeet.dating.datingcommandservice.event.OutboxService;
import com.grewmeet.dating.datingcommandservice.repository.DatingMeetingRepository;
import com.grewmeet.dating.datingcommandservice.repository.ParticipantRepository;
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
    private final ParticipantRepository participantRepository;
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

    @Override
    public ParticipantResponse joinEvent(String eventId, JoinEventRequest request) {
        Long id = IdParser.parseEventId(eventId);
        DatingMeeting datingMeeting = getDatingMeeting(eventId, id);
        
        // 비즈니스 룰 검증
        if (datingMeeting.hasParticipant(request.userId())) {
            throw new IllegalStateException("User is already participating in this event: " + request.userId());
        }
        
        if (datingMeeting.isParticipantsFull()) {
            throw new IllegalStateException("Event is full. Cannot join: " + eventId);
        }
        
        // 참여자 생성 및 저장
        Participant participant = Participant.create(request.userId(), datingMeeting);
        Participant savedParticipant = participantRepository.save(participant);
        
        // 이벤트 발행
        ParticipantJoinedEvent joinedEvent = new ParticipantJoinedEvent(
                datingMeeting.getId(),
                savedParticipant.getId(),
                savedParticipant.getUserId(),
                savedParticipant.getCreatedAt()
        );
        
        outboxService.publishEvent("ParticipantJoined", "Participant", savedParticipant.getId(), joinedEvent);
        
        log.info("User joined event: userId={}, eventId={}, participantId={}", 
                request.userId(), eventId, savedParticipant.getId());
        
        return new ParticipantResponse(
                savedParticipant.getId(),
                savedParticipant.getUserId(),
                savedParticipant.getStatus(),
                savedParticipant.getCreatedAt()
        );
    }
    
    @Override
    public void leaveEvent(String eventId, Long participantId) {
        Long eventIdLong = IdParser.parseEventId(eventId);
        DatingMeeting datingMeeting = getDatingMeeting(eventId, eventIdLong);
        
        Participant participant = participantRepository.findByIdAndDatingMeetingId(participantId, eventIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found: " + participantId));
        
        if (!participant.isActive()) {
            throw new IllegalStateException("Participant is already withdrawn: " + participantId);
        }
        
        // 참여자 탈퇴 처리
        participant.withdraw();
        participantRepository.save(participant);
        
        // 이벤트 발행
        ParticipantLeftEvent leftEvent = new ParticipantLeftEvent(
                datingMeeting.getId(),
                participant.getId(),
                participant.getUserId(),
                java.time.LocalDateTime.now()
        );
        
        outboxService.publishEvent("ParticipantLeft", "Participant", participant.getId(), leftEvent);
        
        log.info("User left event: userId={}, eventId={}, participantId={}", 
                participant.getUserId(), eventId, participantId);
    }

    private DatingMeeting getDatingMeeting(String eventId, Long id) {
        DatingMeeting datingMeeting = datingMeetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dating meeting not found: " + eventId));
        return datingMeeting;
    }
}