package com.grewmeet.dating.datingcommandservice.repository;

import com.grewmeet.dating.datingcommandservice.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("SELECT p FROM Participant p WHERE p.datingMeeting.id = :datingMeetingId AND p.datingUser.id = :datingUserId AND p.status = :status")
    Optional<Participant> findByDatingMeetingIdAndDatingUserIdAndStatus(
            @Param("datingMeetingId") Long datingMeetingId,
            @Param("datingUserId") Long datingUserId,
            @Param("status") Participant.ParticipantStatus status);

    @Query("SELECT p FROM Participant p WHERE p.id = :participantId AND p.datingMeeting.id = :datingMeetingId")
    Optional<Participant> findByIdAndDatingMeetingId(
            @Param("participantId") Long participantId,
            @Param("datingMeetingId") Long datingMeetingId);
}