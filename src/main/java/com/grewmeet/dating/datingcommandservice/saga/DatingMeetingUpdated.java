package com.grewmeet.dating.datingcommandservice.saga;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import java.time.LocalDateTime;
import java.util.UUID;

public record DatingMeetingUpdated(
        UUID meetingUuid,
        String title,
        String description,
        LocalDateTime meetingDateTime,
        String location,
        Integer maxMaleParticipants,
        Integer maxFemaleParticipants,
        LocalDateTime updatedAt
) {
    public static DatingMeetingUpdated from(DatingMeeting datingMeeting) {
        return new DatingMeetingUpdated(
                datingMeeting.getMeetingUuid(),
                datingMeeting.getTitle(),
                datingMeeting.getDescription(),
                datingMeeting.getMeetingDateTime(),
                datingMeeting.getLocation(),
                datingMeeting.getMaxMaleParticipants(),
                datingMeeting.getMaxFemaleParticipants(),
                datingMeeting.getUpdatedAt()
        );
    }
}