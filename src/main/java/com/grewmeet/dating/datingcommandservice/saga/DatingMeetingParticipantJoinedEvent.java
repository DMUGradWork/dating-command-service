package com.grewmeet.dating.datingcommandservice.saga;

import java.time.LocalDateTime;
import java.util.UUID;

public record DatingMeetingParticipantJoinedEvent(
        UUID meetingUuid,
        UUID authUserId,
        String gender,             // Query: 정원 계산
        String meetingTitle,       // Schedule: 일정 제목
        LocalDateTime meetingDateTime, // Schedule: 일정 일시
        LocalDateTime joinedAt
) {}