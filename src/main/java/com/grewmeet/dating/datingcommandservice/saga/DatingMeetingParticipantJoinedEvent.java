package com.grewmeet.dating.datingcommandservice.saga;

import java.time.LocalDateTime;

public record DatingMeetingParticipantJoinedEvent(
        Long datingMeetingId,
        Long participantId,
        Long userId,
        LocalDateTime joinedAt
) {}