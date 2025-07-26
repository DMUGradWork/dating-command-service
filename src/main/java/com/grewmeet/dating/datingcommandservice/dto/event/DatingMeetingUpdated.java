package com.grewmeet.dating.datingcommandservice.dto.event;

import java.time.LocalDateTime;

public record DatingMeetingUpdated(
        Long datingMeetingId,
        String title,
        String description,
        LocalDateTime meetingDateTime,
        String location,
        Integer maxParticipants,
        LocalDateTime updatedAt
) {}