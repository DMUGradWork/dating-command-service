package com.grewmeet.dating.datingcommandservice.dto.event;

import java.time.LocalDateTime;

public record DatingMeetingDeleted(
        Long datingMeetingId,
        LocalDateTime deletedAt
) {}