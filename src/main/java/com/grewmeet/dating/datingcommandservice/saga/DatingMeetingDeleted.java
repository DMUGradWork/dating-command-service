package com.grewmeet.dating.datingcommandservice.saga;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import com.grewmeet.dating.datingcommandservice.domain.Participant;
import java.time.LocalDateTime;
import java.util.UUID;

public record DatingMeetingDeleted(
        UUID datingMeetingId,
        LocalDateTime deletedAt
) {
//    public static DatingMeetingDeleted from(DatingMeeting datingMeeting) {
//        List<UUID> participantIds = datingMeeting.getParticipants().stream()
//                .filter(Participant::isActive)
//                .map(Participant::getAuthUserId)
//                .toList();
//
//        return new DatingMeetingDeleted(
//                datingMeeting.getId(),
//                datingMeeting.getTitle(),
//                datingMeeting.getDescription(),
//                datingMeeting.getMeetingDateTime(),
//                datingMeeting.getLocation(),
//                datingMeeting.getMaxParticipants(),
//                participantIds,
//                LocalDateTime.now()
//        );
//    }
    public static DatingMeetingDeleted from(DatingMeeting datingMeeting) {
        return new DatingMeetingDeleted(
                datingMeeting.getMeetingUuid(),
                LocalDateTime.now()
        );
    }
}