package com.grewmeet.dating.datingcommandservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grewmeet.dating.datingcommandservice.dto.request.UpdateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;
import com.grewmeet.dating.datingcommandservice.service.DatingMeetingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DatingMeetingController.class)
class DatingMeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatingMeetingService datingMeetingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("이벤트 부분 수정 성공")
    void updateEventPartially() throws Exception {
        // given
        UpdateDatingMeetingRequest request = new UpdateDatingMeetingRequest(
                "새로운 제목",
                null,
                LocalDateTime.of(2025, 12, 26, 15, 0),
                null,
                15
        );

        DatingMeetingResponse expectedResponse = new DatingMeetingResponse(
                1L,
                "새로운 제목",
                "기존 설명",
                LocalDateTime.of(2025, 12, 26, 15, 0),
                "기존 장소",
                15,
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(datingMeetingService.updateDatingMeeting("1", request)).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(patch("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("새로운 제목"))
                .andExpect(jsonPath("$.description").value("기존 설명"))
                .andExpect(jsonPath("$.maxParticipants").value(15));
    }

    @Test
    @DisplayName("이벤트 수정시 검증 실패")
    void failValidationWhenTitleTooLong() throws Exception {
        // given - 제목이 너무 긴 경우
        UpdateDatingMeetingRequest request = new UpdateDatingMeetingRequest(
                "a".repeat(201), // 200자 초과
                null,
                null,
                null,
                null
        );

        // when & then
        mockMvc.perform(patch("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("미래 시간 검증 실패")
    void failValidationWhenPastDateTime() throws Exception {
        // given - 과거 시간 설정
        UpdateDatingMeetingRequest request = new UpdateDatingMeetingRequest(
                null,
                null,
                LocalDateTime.of(2020, 1, 1, 10, 0), // 과거 시간
                null,
                null
        );

        // when & then
        mockMvc.perform(patch("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("참여자수 범위 검증 실패")
    void failValidationWhenMaxParticipantsOutOfRange() throws Exception {
        // given - 최대 참여자 수가 범위를 벗어남
        UpdateDatingMeetingRequest request = new UpdateDatingMeetingRequest(
                null,
                null,
                null,
                null,
                1 // 최소 2명 미만
        );

        // when & then
        mockMvc.perform(patch("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 수정시 예외")
    void throwExceptionWhenEventNotFound() throws Exception {
        // given
        UpdateDatingMeetingRequest request = new UpdateDatingMeetingRequest(
                "새로운 제목",
                null,
                null,
                null,
                null
        );

        given(datingMeetingService.updateDatingMeeting("999", request))
                .willThrow(new IllegalArgumentException("Dating meeting not found: 999"));

        // when & then
        mockMvc.perform(patch("/events/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}