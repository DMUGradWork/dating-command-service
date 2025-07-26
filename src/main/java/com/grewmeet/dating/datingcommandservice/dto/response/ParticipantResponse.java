package com.grewmeet.dating.datingcommandservice.dto.response;

import com.grewmeet.dating.datingcommandservice.domain.Participant;

import java.time.LocalDateTime;

public record ParticipantResponse(
        Long id,
        String userId,
        Participant.ParticipantStatus status,
        LocalDateTime joinedAt
) {}