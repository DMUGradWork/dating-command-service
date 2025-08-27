package com.grewmeet.dating.datingcommandservice.saga;

import java.time.LocalDateTime;

public record DatingMeetingParticipantLeftEvent(
        Long datingMeetingId,
        Long participantId,
        Long userId,
        LocalDateTime leftAt
) {}