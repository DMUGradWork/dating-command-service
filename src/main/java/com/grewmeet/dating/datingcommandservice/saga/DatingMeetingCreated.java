package com.grewmeet.dating.datingcommandservice.saga;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import java.time.LocalDateTime;

public record DatingMeetingCreated(
        Long datingMeetingId,
        String title,
        String description,
        LocalDateTime meetingDateTime,
        String location,
        Integer maxParticipants,
        LocalDateTime createdAt
) {
    public static DatingMeetingCreated from(DatingMeeting datingMeeting) {
        return new DatingMeetingCreated(
                datingMeeting.getId(),
                datingMeeting.getTitle(),
                datingMeeting.getDescription(),
                datingMeeting.getMeetingDateTime(),
                datingMeeting.getLocation(),
                datingMeeting.getMaxParticipants(),
                datingMeeting.getCreatedAt()
        );
    }
}