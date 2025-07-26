package com.grewmeet.dating.datingcommandservice.saga;

import java.time.LocalDateTime;

public record ParticipantJoinedEvent(
        Long datingMeetingId,
        Long participantId,
        String userId,
        LocalDateTime joinedAt
) {}