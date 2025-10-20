package com.grewmeet.dating.datingcommandservice.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record JoinEventRequest(
    @NotNull(message = "사용자 ID는 필수입니다.")
    UUID userId
) {
}