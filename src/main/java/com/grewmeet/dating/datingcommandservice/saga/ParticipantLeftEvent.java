package com.grewmeet.dating.datingcommandservice.saga;

import java.time.LocalDateTime;

public record ParticipantLeftEvent(
        Long datingMeetingId,
        Long participantId,
        String userId,
        LocalDateTime leftAt
) {}