package com.grewmeet.dating.datingcommandservice.dto.event;

import java.time.LocalDateTime;

public record ParticipantLeftEvent(
        Long datingMeetingId,
        Long participantId,
        String userId,
        LocalDateTime leftAt
) {}