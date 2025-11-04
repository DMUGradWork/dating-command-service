package com.grewmeet.dating.datingcommandservice.dto.response;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import java.time.LocalDateTime;
import java.util.UUID;

public record DatingMeetingResponse(
        Long id,
        UUID meetingUuid,
        String title,
        String description,
        LocalDateTime meetingDateTime,
        String location,
        Integer maxMaleParticipants,
        Integer maxFemaleParticipants,
        Integer currentMaleParticipants,
        Integer currentFemaleParticipants,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DatingMeetingResponse from(DatingMeeting datingMeeting) {
        return new DatingMeetingResponse(
                datingMeeting.getId(),
                datingMeeting.getMeetingUuid(),
                datingMeeting.getTitle(),
                datingMeeting.getDescription(),
                datingMeeting.getMeetingDateTime(),
                datingMeeting.getLocation(),
                datingMeeting.getMaxMaleParticipants(),
                datingMeeting.getMaxFemaleParticipants(),
                datingMeeting.getMaleParticipantsCount(),
                datingMeeting.getFemaleParticipantsCount(),
                datingMeeting.getCreatedAt(),
                datingMeeting.getUpdatedAt()
        );
    }
}