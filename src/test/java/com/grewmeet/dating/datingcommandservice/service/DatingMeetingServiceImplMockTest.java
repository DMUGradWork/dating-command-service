package com.grewmeet.dating.datingcommandservice.service;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import com.grewmeet.dating.datingcommandservice.dto.request.UpdateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;
import com.grewmeet.dating.datingcommandservice.event.OutboxService;
import com.grewmeet.dating.datingcommandservice.repository.DatingMeetingRepository;
import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingUpdated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DatingMeetingServiceImplTest {

    @Mock
    private DatingMeetingRepository datingMeetingRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private DatingMeetingServiceImpl datingMeetingService;

    private DatingMeeting existingMeeting;
    private final Long meetingId = 1L;

    @BeforeEach
    void setUp() {
        existingMeeting = DatingMeeting.create(
                "기존 제목",
                "기존 설명",
                LocalDateTime.of(2025, 12, 25, 14, 0),
                "기존 장소",
                10
        );
    }

    @Test
    @DisplayName("이벤트 부분 수정 성공")
    void updateDatingMeetingPartially() {
        // given
        UpdateDatingMeetingRequest request = new UpdateDatingMeetingRequest(
                "새로운 제목",
                null, // 설명은 변경하지 않음
                LocalDateTime.of(2025, 12, 26, 15, 0),
                null, // 장소는 변경하지 않음
                15
        );

        given(datingMeetingRepository.findById(meetingId)).willReturn(Optional.of(existingMeeting));
        given(datingMeetingRepository.save(any(DatingMeeting.class))).willReturn(existingMeeting);

        // when
        DatingMeetingResponse response = datingMeetingService.updateDatingMeeting("1", request);

        // then
        assertThat(response.title()).isEqualTo("새로운 제목");
        assertThat(response.description()).isEqualTo("기존 설명"); // 변경되지 않음
        assertThat(response.meetingDateTime()).isEqualTo(LocalDateTime.of(2025, 12, 26, 15, 0));
        assertThat(response.location()).isEqualTo("기존 장소"); // 변경되지 않음
        assertThat(response.maxParticipants()).isEqualTo(15);

        then(outboxService).should().publishEvent(
                eq("DatingMeetingUpdated"),
                eq("DatingMeeting"),
                eq(existingMeeting.getId()),
                any(DatingMeetingUpdated.class)
        );
    }

    @Test
    @DisplayName("이벤트 전체 필드 수정 성공")
    void updateDatingMeetingAllFields() {
        // given
        UpdateDatingMeetingRequest request = new UpdateDatingMeetingRequest(
                "새로운 제목",
                "새로운 설명",
                LocalDateTime.of(2025, 12, 26, 15, 0),
                "새로운 장소",
                20
        );

        given(datingMeetingRepository.findById(meetingId)).willReturn(Optional.of(existingMeeting));
        given(datingMeetingRepository.save(any(DatingMeeting.class))).willReturn(existingMeeting);

        // when
        DatingMeetingResponse response = datingMeetingService.updateDatingMeeting("1", request);

        // then
        assertThat(response.title()).isEqualTo("새로운 제목");
        assertThat(response.description()).isEqualTo("새로운 설명");
        assertThat(response.meetingDateTime()).isEqualTo(LocalDateTime.of(2025, 12, 26, 15, 0));
        assertThat(response.location()).isEqualTo("새로운 장소");
        assertThat(response.maxParticipants()).isEqualTo(20);
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 수정시 예외발생")
    void throwExceptionWhenMeetingNotFound() {
        // given
        UpdateDatingMeetingRequest request = new UpdateDatingMeetingRequest(
                "새로운 제목",
                null,
                null,
                null,
                null
        );

        given(datingMeetingRepository.findById(meetingId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> datingMeetingService.updateDatingMeeting("1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dating meeting not found: 1");
    }

    @Test
    @DisplayName("잘못된 ID 형식시 예외발생")
    void throwExceptionWhenInvalidIdFormat() {
        // given
        UpdateDatingMeetingRequest request = new UpdateDatingMeetingRequest(
                "새로운 제목",
                null,
                null,
                null,
                null
        );

        // when & then
        assertThatThrownBy(() -> datingMeetingService.updateDatingMeeting("invalid", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid event ID format");
    }
}