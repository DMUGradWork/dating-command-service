package com.grewmeet.dating.datingcommandservice.saga;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import com.grewmeet.dating.datingcommandservice.domain.Participant;
import java.time.LocalDateTime;
import java.util.List;

public record DatingMeetingDeleted(
        Long datingMeetingId,
        String title,
        String description,
        LocalDateTime meetingDateTime,
        String location,
        Integer maxParticipants,
        List<String> participantIds,
        LocalDateTime deletedAt
) {
    public static DatingMeetingDeleted from(DatingMeeting datingMeeting) {
        List<String> participantIds = datingMeeting.getParticipants().stream()
                .filter(Participant::isActive)
                .map(Participant::getUserId)
                .toList();
        
        return new DatingMeetingDeleted(
                datingMeeting.getId(),
                datingMeeting.getTitle(),
                datingMeeting.getDescription(),
                datingMeeting.getMeetingDateTime(),
                datingMeeting.getLocation(),
                datingMeeting.getMaxParticipants(),
                participantIds,
                LocalDateTime.now()
        );
    }
}