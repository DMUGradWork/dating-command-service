package com.grewmeet.dating.datingcommandservice.saga;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import java.time.LocalDateTime;

public record DatingMeetingUpdated(
        Long datingMeetingId,
        String title,
        String description,
        LocalDateTime meetingDateTime,
        String location,
        Integer maxParticipants,
        LocalDateTime updatedAt
) {
    public static DatingMeetingUpdated from(DatingMeeting datingMeeting) {
        return new DatingMeetingUpdated(
                datingMeeting.getId(),
                datingMeeting.getTitle(),
                datingMeeting.getDescription(),
                datingMeeting.getMeetingDateTime(),
                datingMeeting.getLocation(),
                datingMeeting.getMaxParticipants(),
                datingMeeting.getUpdatedAt()
        );
    }
}