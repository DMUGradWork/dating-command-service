package com.grewmeet.dating.datingcommandservice.repository;

import com.grewmeet.dating.datingcommandservice.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    Optional<Participant> findByDatingMeetingIdAndUserIdAndStatus(Long datingMeetingId, String userId, Participant.ParticipantStatus status);
    
    Optional<Participant> findByIdAndDatingMeetingId(Long participantId, Long datingMeetingId);
}