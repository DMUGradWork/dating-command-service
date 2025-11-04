package com.grewmeet.dating.datingcommandservice.service;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import com.grewmeet.dating.datingcommandservice.domain.DatingUser;
import com.grewmeet.dating.datingcommandservice.domain.Participant;
import com.grewmeet.dating.datingcommandservice.domain.user.Gender;
import com.grewmeet.dating.datingcommandservice.domain.user.UserRole;
import com.grewmeet.dating.datingcommandservice.dto.request.JoinEventRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.ParticipantResponse;
import com.grewmeet.dating.datingcommandservice.event.OutboxService;
import com.grewmeet.dating.datingcommandservice.repository.DatingMeetingRepository;
import com.grewmeet.dating.datingcommandservice.repository.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class EventParticipationServiceTest {

    @Mock
    private DatingMeetingRepository datingMeetingRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private DatingMeetingServiceImpl datingMeetingService;

    private DatingMeeting datingMeeting;
    private JoinEventRequest joinEventRequest;

    @BeforeEach
    void setUp() {
        datingMeeting = DatingMeeting.create(
                "테스트 이벤트",
                "테스트 설명",
                1L,
                LocalDateTime.now().plusDays(1),
                "서울시 강남구",
                3,
                2
        );
        joinEventRequest = new JoinEventRequest(UUID.randomUUID());
    }

    @Test
    @DisplayName("이벤트 참여 성공")
    void joinEvent_Success() {
        // given
        given(datingMeetingRepository.findById(1L)).willReturn(Optional.of(datingMeeting));

        DatingUser testUser = DatingUser.create(UUID.randomUUID(), "2025-Q1", "테스트유저", Gender.MALE, UserRole.GUEST, null);
        Participant savedParticipant = Participant.create(testUser, datingMeeting);
        given(participantRepository.save(any(Participant.class))).willReturn(savedParticipant);

        // when
        ParticipantResponse response = datingMeetingService.joinEvent("1", joinEventRequest);

        // then
        assertThat(response.userId()).isEqualTo(testUser.getId());
        assertThat(response.status()).isEqualTo(Participant.ParticipantStatus.ACTIVE);

        verify(participantRepository).save(any(Participant.class));
        verify(outboxService).publishEvent(eq("DatingMeetingParticipantJoined"), eq("DatingMeetingParticipant"), any(), any());
    }

    @Test
    @DisplayName("이미 참여한 사용자는 재참여 불가")
    void joinEvent_AlreadyParticipating_ThrowsException() {
        // given
        DatingUser testUser = DatingUser.create(UUID.randomUUID(), "2025-Q1", "테스트유저", Gender.MALE, UserRole.GUEST, null);
        Participant existingParticipant = Participant.create(testUser, datingMeeting);
        datingMeeting.getParticipants().add(existingParticipant);

        given(datingMeetingRepository.findById(1L)).willReturn(Optional.of(datingMeeting));

        // when & then
        assertThatThrownBy(() -> datingMeetingService.joinEvent("1", joinEventRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User is already participating");

        verify(participantRepository, never()).save(any());
        verify(outboxService, never()).publishEvent(any(), any(), any(), any());
    }

    @Test
    @DisplayName("정원 초과시 참여 불가")
    void joinEvent_EventFull_ThrowsException() {
        // given
        // 남성 정원 3명 채우기
        for (int i = 1; i <= 3; i++) {
            DatingUser testUser = DatingUser.create(UUID.randomUUID(), "2025-Q1", "남성유저" + i, Gender.MALE, UserRole.GUEST, null);
            Participant participant = Participant.create(testUser, datingMeeting);
            datingMeeting.getParticipants().add(participant);
        }
        // 여성 정원 2명 채우기
        for (int i = 1; i <= 2; i++) {
            DatingUser testUser = DatingUser.create(UUID.randomUUID(), "2025-Q1", "여성유저" + i, Gender.FEMALE, UserRole.GUEST, null);
            Participant participant = Participant.create(testUser, datingMeeting);
            datingMeeting.getParticipants().add(participant);
        }

        given(datingMeetingRepository.findById(1L)).willReturn(Optional.of(datingMeeting));

        JoinEventRequest newUserRequest = new JoinEventRequest(UUID.randomUUID());

        // when & then
        assertThatThrownBy(() -> datingMeetingService.joinEvent("1", newUserRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Event is full");

        verify(participantRepository, never()).save(any());
        verify(outboxService, never()).publishEvent(any(), any(), any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 참여시 예외 발생")
    void joinEvent_EventNotFound_ThrowsException() {
        // given
        given(datingMeetingRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> datingMeetingService.joinEvent("1", joinEventRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dating meeting not found");

        verify(participantRepository, never()).save(any());
        verify(outboxService, never()).publishEvent(any(), any(), any(), any());
    }

    @Test
    @DisplayName("이벤트 탈퇴 성공")
    void leaveEvent_Success() {
        // given
        DatingUser testUser = DatingUser.create(UUID.randomUUID(), "2025-Q1", "테스트유저", Gender.MALE, UserRole.GUEST, null);
        Participant participant = Participant.create(testUser, datingMeeting);
        datingMeeting.getParticipants().add(participant);

        given(datingMeetingRepository.findById(1L)).willReturn(Optional.of(datingMeeting));
        given(participantRepository.findByIdAndDatingMeetingId(1L, 1L)).willReturn(Optional.of(participant));
        given(participantRepository.save(any(Participant.class))).willReturn(participant);

        // when
        datingMeetingService.leaveEvent("1", 1L);

        // then
        assertThat(participant.getStatus()).isEqualTo(Participant.ParticipantStatus.WITHDRAWN);

        verify(participantRepository).save(participant);
        verify(outboxService).publishEvent(eq("DatingMeetingParticipantLeft"), eq("DatingMeetingParticipant"), any(), any());
    }

    @Test
    @DisplayName("이미 탈퇴한 참여자는 재탈퇴 불가")
    void leaveEvent_AlreadyWithdrawn_ThrowsException() {
        // given
        DatingUser testUser = DatingUser.create(UUID.randomUUID(), "2025-Q1", "테스트유저", Gender.MALE, UserRole.GUEST, null);
        Participant participant = Participant.create(testUser, datingMeeting);
        participant.withdraw(); // 이미 탈퇴 처리

        given(datingMeetingRepository.findById(1L)).willReturn(Optional.of(datingMeeting));
        given(participantRepository.findByIdAndDatingMeetingId(1L, 1L)).willReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> datingMeetingService.leaveEvent("1", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Participant is already withdrawn");

        verify(participantRepository, never()).save(any());
        verify(outboxService, never()).publishEvent(any(), any(), any(), any());
    }
}