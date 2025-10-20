package com.grewmeet.dating.datingcommandservice.controller;

import com.grewmeet.dating.datingcommandservice.dto.request.CreateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.request.JoinEventRequest;
import com.grewmeet.dating.datingcommandservice.dto.request.UpdateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;
import com.grewmeet.dating.datingcommandservice.dto.response.ParticipantResponse;
import com.grewmeet.dating.datingcommandservice.service.DatingMeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/events")
@Tag(name = "DatingMeeting", description = "데이팅 미팅 관리 API")
@RequiredArgsConstructor
public class DatingMeetingController {

    private final DatingMeetingService datingMeetingService;

    @PostMapping
    @Operation(summary = "새 데이팅 미팅 생성", description = "새로운 데이팅 미팅을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "미팅 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "409", description = "미팅 생성 중 충돌 발생")
    })
    public ResponseEntity<DatingMeetingResponse> createDatingMeeting(
            @Parameter(description = "인증된 사용자 ID (호스트)", required = true) @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateDatingMeetingRequest request) {
        DatingMeetingResponse response = datingMeetingService.createDatingMeeting(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{datingMeetingId}")
    @Operation(summary = "이벤트 정보 부분 수정", description = "기존 데이팅 이벤트의 정보를 부분적으로 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이벤트 수정 중 충돌 발생")
    })
    public ResponseEntity<DatingMeetingResponse> updateEvent(
            @Parameter(description = "데이팅 미팅 ID") @PathVariable String datingMeetingId,
            @Valid @RequestBody UpdateDatingMeetingRequest request) {
        DatingMeetingResponse response = datingMeetingService.updateDatingMeeting(datingMeetingId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{datingMeetingId}")
    @Operation(summary = "이벤트 삭제", description = "데이팅 이벤트를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "이벤트 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "참여자가 있어 삭제할 수 없음")
    })
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "데이팅 미팅 ID") @PathVariable String datingMeetingId) {
        datingMeetingService.deleteDatingMeeting(datingMeetingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{datingMeetingId}/participants")
    @Operation(summary = "이벤트 참여", description = "데이팅 이벤트에 참여합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이벤트 참여 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 참여중이거나 정원 초과")
    })
    public ResponseEntity<ParticipantResponse> joinEvent(
            @Parameter(description = "데이팅 미팅 ID") @PathVariable String datingMeetingId,
            @Parameter(description = "인증된 사용자 ID", required = true) @RequestHeader("X-User-Id") UUID userId) {
        JoinEventRequest request = new JoinEventRequest(userId);
        ParticipantResponse response = datingMeetingService.joinEvent(datingMeetingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{datingMeetingId}/participants/{participantId}")
    @Operation(summary = "이벤트 탈퇴", description = "데이팅 이벤트에서 탈퇴합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "이벤트 탈퇴 성공"),
            @ApiResponse(responseCode = "404", description = "이벤트 또는 참여자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "탈퇴할 수 없는 상태")
    })
    public ResponseEntity<Void> leaveEvent(
            @Parameter(description = "데이팅 미팅 ID") @PathVariable String datingMeetingId,
            @Parameter(description = "참여자 ID") @PathVariable Long participantId) {
        datingMeetingService.leaveEvent(datingMeetingId, participantId);
        return ResponseEntity.noContent().build();
    }
}