package com.grewmeet.dating.datingcommandservice.service;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import com.grewmeet.dating.datingcommandservice.domain.DatingUser;
import com.grewmeet.dating.datingcommandservice.domain.Participant;
import com.grewmeet.dating.datingcommandservice.repository.DatingUserRepository;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingCreated;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingDeleted;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingUpdated;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingParticipantJoinedEvent;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingParticipantLeftEvent;
import com.grewmeet.dating.datingcommandservice.dto.request.CreateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.request.JoinEventRequest;
import com.grewmeet.dating.datingcommandservice.dto.request.UpdateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;
import com.grewmeet.dating.datingcommandservice.dto.response.ParticipantResponse;
import com.grewmeet.dating.datingcommandservice.event.OutboxService;
import com.grewmeet.dating.datingcommandservice.repository.DatingMeetingRepository;
import com.grewmeet.dating.datingcommandservice.repository.ParticipantRepository;
import com.grewmeet.dating.datingcommandservice.util.IdParser;
import java.util.UUID;
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
    private final DatingUserRepository datingUserRepository;
    private final OutboxService outboxService;

    @Override
    public DatingMeetingResponse createDatingMeeting(CreateDatingMeetingRequest request, UUID hostId) {
        DatingMeeting datingMeeting = DatingMeeting.create(
                request.title(),
                request.description(),
                getDatingHostId(hostId),
                request.meetingDateTime(),
                request.location(),
                request.maxMaleParticipants(),
                request.maxFemaleParticipants()
        );
        DatingMeeting savedDatingMeeting = datingMeetingRepository.save(datingMeeting);
        
        DatingMeetingCreated datingMeetingCreated = DatingMeetingCreated.from(savedDatingMeeting);
        
        outboxService.publishEvent("DatingMeetingCreated", "DatingMeeting", savedDatingMeeting.getId(), datingMeetingCreated);
        
        log.info("Dating meeting created: id={}, title={}", savedDatingMeeting.getId(), savedDatingMeeting.getTitle());
        
        return DatingMeetingResponse.from(savedDatingMeeting);
    }

    @Override
    public DatingMeetingResponse updateDatingMeeting(String datingMeetingId, UpdateDatingMeetingRequest request) {
        Long id = IdParser.parseDatingMeetingId(datingMeetingId);
        DatingMeeting datingMeeting = getDatingMeeting(datingMeetingId, id);

        datingMeeting.update(
                request.title(),
                request.description(),
                request.meetingDateTime(),
                request.location(),
                request.maxMaleParticipants(),
                request.maxFemaleParticipants()
        );

        DatingMeeting updatedDatingMeeting = datingMeetingRepository.save(datingMeeting);

        DatingMeetingUpdated datingMeetingUpdated = DatingMeetingUpdated.from(updatedDatingMeeting);

        outboxService.publishEvent("DatingMeetingUpdated", "DatingMeeting", updatedDatingMeeting.getId(), datingMeetingUpdated);

        log.info("Dating meeting updated: id={}, title={}", updatedDatingMeeting.getId(), updatedDatingMeeting.getTitle());

        return DatingMeetingResponse.from(updatedDatingMeeting);
    }

    @Override
    public void deleteDatingMeeting(String datingMeetingId) {
        Long id = IdParser.parseDatingMeetingId(datingMeetingId);
        DatingMeeting datingMeeting = getDatingMeeting(datingMeetingId, id);

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
    public ParticipantResponse joinEvent(String datingMeetingId, JoinEventRequest request) {
        Long id = IdParser.parseDatingMeetingId(datingMeetingId);
        DatingMeeting datingMeeting = getDatingMeeting(datingMeetingId, id);

        // DatingUser 조회 (현재 분기)
        String currentQuarter = getCurrentQuarter();
        DatingUser datingUser = datingUserRepository.findByAuthUserIdAndQuarter(request.userId(), currentQuarter)
                .orElseThrow(() -> new IllegalArgumentException("DatingUser not found for this quarter: " + request.userId()));

        // 비즈니스 룰 검증
        if (!datingUser.canParticipate()) {
            throw new IllegalStateException("User cannot participate in events: " + request.userId());
        }

        if (datingMeeting.hasParticipant(datingUser.getId())) {
            throw new IllegalStateException("User is already participating in this event: " + request.userId());
        }

        if (datingMeeting.isGenderFull(datingUser)) {
            throw new IllegalStateException("Gender quota is full. Cannot join: " + datingMeetingId);
        }

        // 참여자 생성 및 저장
        Participant participant = Participant.create(datingUser, datingMeeting);
        Participant savedParticipant = participantRepository.save(participant);

        // 참여 횟수 증가
        datingUser.incrementParticipation();
        datingUserRepository.save(datingUser);

        // 이벤트 발행
        DatingMeetingParticipantJoinedEvent joinedEvent = new DatingMeetingParticipantJoinedEvent(
                datingMeeting.getId(),
                savedParticipant.getId(),
                savedParticipant.getDatingUserId(),
                savedParticipant.getCreatedAt()
        );

        outboxService.publishEvent("DatingMeetingParticipantJoined", "DatingMeetingParticipant", savedParticipant.getId(), joinedEvent);

        log.info("User joined event: authUserId={}, datingUserId={}, datingMeetingId={}, participantId={}",
                request.userId(), datingUser.getId(), datingMeetingId, savedParticipant.getId());

        return new ParticipantResponse(
                savedParticipant.getId(),
                savedParticipant.getDatingUserId(),
                savedParticipant.getStatus(),
                savedParticipant.getCreatedAt()
        );
    }

    @Override
    public void leaveEvent(String datingMeetingId, Long participantId) {
        Long datingMeetingIdLong = IdParser.parseDatingMeetingId(datingMeetingId);
        DatingMeeting datingMeeting = getDatingMeeting(datingMeetingId, datingMeetingIdLong);

        Participant participant = participantRepository.findByIdAndDatingMeetingId(participantId, datingMeetingIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found: " + participantId));

        if (!participant.isActive()) {
            throw new IllegalStateException("Participant is already withdrawn: " + participantId);
        }

        // 참여자 탈퇴 처리
        participant.withdraw();
        participantRepository.save(participant);

        // 이벤트 발행
        DatingMeetingParticipantLeftEvent leftEvent = new DatingMeetingParticipantLeftEvent(
                datingMeeting.getId(),
                participant.getId(),
                participant.getDatingUserId(),
                java.time.LocalDateTime.now()
        );

        outboxService.publishEvent("DatingMeetingParticipantLeft", "DatingMeetingParticipant", participant.getId(), leftEvent);

        log.info("User left event: userId={}, datingMeetingId={}, participantId={}",
                participant.getDatingUserId(), datingMeetingId, participantId);
    }

    private DatingMeeting getDatingMeeting(String datingMeetingId, Long id) {
        DatingMeeting datingMeeting = datingMeetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dating meeting not found: " + datingMeetingId));
        return datingMeeting;
    }

    private Long getDatingHostId(UUID id) {
        DatingUser datingUser = getDatingUserByAuthId(id);
        if(!datingUser.isHost()) {
            throw new IllegalStateException("Dating user {"+ id + "} can't make dating event : not host.");
        }
        return datingUser.getId();
    }

    private DatingUser getDatingUserByAuthId(UUID id) {
        return datingUserRepository.findByAuthUserIdAndQuarter(id, getCurrentQuarter())
                .orElseThrow(() -> new IllegalArgumentException("Dating User not found for this quarter: " + id));
    }

    private String getCurrentQuarter() {
        java.time.LocalDate now = java.time.LocalDate.now();
        int year = now.getYear();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;
        return year + "-Q" + quarter;
    }
}