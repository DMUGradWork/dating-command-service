package com.grewmeet.dating.datingcommandservice.saga;

import java.time.LocalDateTime;

public record DatingMeetingDeleted(
        Long datingMeetingId,
        LocalDateTime deletedAt
) {}