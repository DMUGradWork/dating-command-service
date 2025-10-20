package com.grewmeet.dating.datingcommandservice.saga;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import java.time.LocalDateTime;
import java.util.UUID;

public record DatingMeetingCreated(
        UUID meetingUuid,
        String title,
        String description,
        UUID hostAuthUserId,
        String hostNickname,
        LocalDateTime meetingDateTime,
        String location,
        Integer maxMaleParticipants,
        Integer maxFemaleParticipants,
        LocalDateTime createdAt
) {
    public static DatingMeetingCreated from(DatingMeeting datingMeeting, UUID hostAuthUserId, String hostNickname) {
        return new DatingMeetingCreated(
                datingMeeting.getMeetingUuid(),
                datingMeeting.getTitle(),
                datingMeeting.getDescription(),
                hostAuthUserId,
                hostNickname,
                datingMeeting.getMeetingDateTime(),
                datingMeeting.getLocation(),
                datingMeeting.getMaxMaleParticipants(),
                datingMeeting.getMaxFemaleParticipants(),
                datingMeeting.getCreatedAt()
        );
    }
}