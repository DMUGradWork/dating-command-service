package com.grewmeet.dating.datingcommandservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grewmeet.dating.datingcommandservice.domain.Participant;
import com.grewmeet.dating.datingcommandservice.dto.request.JoinEventRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.ParticipantResponse;
import com.grewmeet.dating.datingcommandservice.service.DatingMeetingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DatingMeetingController.class)
@WithMockUser
class EventParticipationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DatingMeetingService datingMeetingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("이벤트 참여 API - 성공")
    void joinEvent_Success() throws Exception {
        // given
        JoinEventRequest request = new JoinEventRequest(1L);
        ParticipantResponse response = new ParticipantResponse(
                1L, 1L, Participant.ParticipantStatus.ACTIVE, LocalDateTime.now()
        );
        
        given(datingMeetingService.joinEvent(eq("1"), any(JoinEventRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/events/1/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(datingMeetingService).joinEvent("1", request);
    }

    @Test
    @DisplayName("이벤트 참여 API - 유효성 검증 실패")
    void joinEvent_ValidationFails() throws Exception {
        // given
        JoinEventRequest invalidRequest = new JoinEventRequest(null);

        // when & then
        mockMvc.perform(post("/events/1/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(datingMeetingService, never()).joinEvent(any(), any());
    }

    @Test
    @DisplayName("이벤트 참여 API - 이미 참여한 경우")
    void joinEvent_AlreadyParticipating() throws Exception {
        // given
        JoinEventRequest request = new JoinEventRequest(1L);
        
        given(datingMeetingService.joinEvent(eq("1"), any(JoinEventRequest.class)))
                .willThrow(new IllegalStateException("User is already participating"));

        // when & then
        mockMvc.perform(post("/events/1/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("이벤트 탈퇴 API - 성공")
    void leaveEvent_Success() throws Exception {
        // given
        willDoNothing().given(datingMeetingService).leaveEvent("1", 1L);

        // when & then
        mockMvc.perform(delete("/events/1/participants/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(datingMeetingService).leaveEvent("1", 1L);
    }

    @Test
    @DisplayName("이벤트 탈퇴 API - 참여자 없음")
    void leaveEvent_ParticipantNotFound() throws Exception {
        // given
        willThrow(new IllegalArgumentException("Participant not found"))
                .given(datingMeetingService).leaveEvent("1", 999L);

        // when & then
        mockMvc.perform(delete("/events/1/participants/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}