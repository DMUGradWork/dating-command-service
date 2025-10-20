package com.grewmeet.dating.datingcommandservice.saga;

import java.time.LocalDateTime;
import java.util.UUID;

public record DatingMeetingParticipantLeftEvent(
        UUID meetingUuid,
        UUID authUserId,
        String gender,             // Query: 정원 재계산
        LocalDateTime leftAt
) {}