package com.grewmeet.dating.datingcommandservice.dto.response;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
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
) {
    public static DatingMeetingResponse from(DatingMeeting datingMeeting) {
        return new DatingMeetingResponse(
                datingMeeting.getId(),
                datingMeeting.getTitle(),
                datingMeeting.getDescription(),
                datingMeeting.getMeetingDateTime(),
                datingMeeting.getLocation(),
                datingMeeting.getMaxParticipants(),
                datingMeeting.getCurrentParticipantCount(),
                datingMeeting.getCreatedAt(),
                datingMeeting.getUpdatedAt()
        );
    }
}