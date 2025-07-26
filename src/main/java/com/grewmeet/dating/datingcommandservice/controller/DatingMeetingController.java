package com.grewmeet.dating.datingcommandservice.controller;

import com.grewmeet.dating.datingcommandservice.dto.request.CreateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;
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

import java.util.Map;

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
    public ResponseEntity<DatingMeetingResponse> createDatingMeeting(@Valid @RequestBody CreateDatingMeetingRequest request) {
        DatingMeetingResponse response = datingMeetingService.createDatingMeeting(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "이벤트 정보 수정", description = "기존 데이팅 이벤트의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이벤트 수정 중 충돌 발생")
    })
    public ResponseEntity<Map<String, Object>> updateEvent(
            @Parameter(description = "이벤트 ID") @PathVariable String eventId,
            @RequestBody Map<String, Object> request) {
        // TODO: EventService.updateEvent() 구현 예정
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "이벤트 삭제", description = "데이팅 이벤트를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "이벤트 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "참여자가 있어 삭제할 수 없음")
    })
    public ResponseEntity<Void> deleteEvent(@Parameter(description = "이벤트 ID") @PathVariable String eventId) {
        // TODO: EventService.deleteEvent() 구현 예정
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/participants")
    @Operation(summary = "이벤트 참여", description = "데이팅 이벤트에 참여합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이벤트 참여 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 참여중이거나 정원 초과")
    })
    public ResponseEntity<Map<String, Object>> joinEvent(
            @Parameter(description = "이벤트 ID") @PathVariable String eventId,
            @RequestBody Map<String, Object> request) {
        // TODO: EventService.joinEvent() 구현 예정
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{eventId}/participants/{participantId}")
    @Operation(summary = "이벤트 탈퇴", description = "데이팅 이벤트에서 탈퇴합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "이벤트 탈퇴 성공"),
            @ApiResponse(responseCode = "404", description = "이벤트 또는 참여자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "탈퇴할 수 없는 상태")
    })
    public ResponseEntity<Void> leaveEvent(
            @Parameter(description = "이벤트 ID") @PathVariable String eventId,
            @Parameter(description = "참여자 ID") @PathVariable String participantId) {
        // TODO: EventService.leaveEvent() 구현 예정
        return ResponseEntity.noContent().build();
    }
}