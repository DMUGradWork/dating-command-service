package com.grewmeet.dating.datingcommandservice.dto.response;

import java.time.LocalDateTime;

public record DatingMeetingResponse(
        Long id,
        String title,
        String description,
        LocalDateTime meetingDateTime,
        String location,
        Integer maxParticipants,
        Integer currentParticipants,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}