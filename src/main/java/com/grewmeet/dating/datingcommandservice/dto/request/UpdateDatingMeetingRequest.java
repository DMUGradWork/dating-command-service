package com.grewmeet.dating.datingcommandservice.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record UpdateDatingMeetingRequest(
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
        String title,

        @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
        String description,

        @Future(message = "미팅 일시는 현재 시간 이후여야 합니다")
        LocalDateTime meetingDateTime,

        @Size(max = 300, message = "장소는 300자를 초과할 수 없습니다")
        String location,

        @Min(value = 1, message = "최대 남성 참여자 수는 최소 1명 이상이어야 합니다")
        @Max(value = 50, message = "최대 남성 참여자 수는 50명을 초과할 수 없습니다")
        Integer maxMaleParticipants,

        @Min(value = 1, message = "최대 여성 참여자 수는 최소 1명 이상이어야 합니다")
        @Max(value = 50, message = "최대 여성 참여자 수는 50명을 초과할 수 없습니다")
        Integer maxFemaleParticipants
) {}