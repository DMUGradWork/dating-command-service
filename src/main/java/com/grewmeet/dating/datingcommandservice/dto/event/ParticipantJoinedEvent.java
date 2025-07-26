package com.grewmeet.dating.datingcommandservice.dto.event;

import java.time.LocalDateTime;

public record ParticipantJoinedEvent(
        Long datingMeetingId,
        Long participantId,
        String userId,
        LocalDateTime joinedAt
) {}