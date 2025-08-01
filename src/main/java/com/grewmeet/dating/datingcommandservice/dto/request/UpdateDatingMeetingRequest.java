package com.grewmeet.dating.datingcommandservice.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record UpdateDatingMeetingRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
        String title,

        @NotBlank(message = "설명은 필수입니다")
        @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
        String description,

        @NotNull(message = "미팅 일시는 필수입니다")
        @Future(message = "미팅 일시는 현재 시간 이후여야 합니다")
        LocalDateTime meetingDateTime,

        @NotBlank(message = "장소는 필수입니다")
        @Size(max = 300, message = "장소는 300자를 초과할 수 없습니다")
        String location,

        @NotNull(message = "최대 참여자 수는 필수입니다")
        @Min(value = 2, message = "최대 참여자 수는 최소 2명 이상이어야 합니다")
        @Max(value = 100, message = "최대 참여자 수는 100명을 초과할 수 없습니다")
        Integer maxParticipants
) {}