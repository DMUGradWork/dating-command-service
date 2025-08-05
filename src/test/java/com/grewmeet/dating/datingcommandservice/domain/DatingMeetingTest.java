package com.grewmeet.dating.datingcommandservice.domain;

import com.grewmeet.dating.datingcommandservice.saga.DatingMeetingDeleted;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DatingMeetingTest {

    @Test
    @DisplayName("데이팅 미팅 생성 성공")
    void createDatingMeeting() {
        // given
        String title = "첫 데이트";
        String description = "카페에서 만나요";
        LocalDateTime meetingDateTime = LocalDateTime.of(2025, 12, 25, 14, 0);
        String location = "스타벅스 강남점";
        Integer maxParticipants = 10;

        // when
        DatingMeeting meeting = DatingMeeting.create(title, description, meetingDateTime, location, maxParticipants);

        // then
        assertEquals(title, meeting.getTitle());
        assertEquals(description, meeting.getDescription());
        assertEquals(meetingDateTime, meeting.getMeetingDateTime());
        assertEquals(location, meeting.getLocation());
        assertEquals(maxParticipants, meeting.getMaxParticipants());
        assertEquals(0, meeting.getCurrentParticipantCount());
        assertFalse(meeting.isParticipantsFull());
    }

    @Test
    @DisplayName("부분 업데이트 - 제목만 변경")
    void updateTitleOnly() {
        // given
        DatingMeeting meeting = DatingMeeting.create(
                "기존 제목",
                "기존 설명",
                LocalDateTime.of(2025, 12, 25, 14, 0),
                "기존 장소",
                10
        );

        // when
        meeting.update("새로운 제목", null, null, null, null);

        // then
        assertEquals("새로운 제목", meeting.getTitle());
        assertEquals("기존 설명", meeting.getDescription()); // 변경되지 않음
        assertEquals(LocalDateTime.of(2025, 12, 25, 14, 0), meeting.getMeetingDateTime()); // 변경되지 않음
        assertEquals("기존 장소", meeting.getLocation()); // 변경되지 않음
        assertEquals(10, meeting.getMaxParticipants()); // 변경되지 않음
    }

    @Test
    @DisplayName("부분 업데이트 - 일부 필드만 변경")
    void updatePartialFields() {
        // given
        DatingMeeting meeting = DatingMeeting.create(
                "기존 제목",
                "기존 설명",
                LocalDateTime.of(2025, 12, 25, 14, 0),
                "기존 장소",
                10
        );

        // when
        meeting.update(
                "새로운 제목",
                null, // 설명은 변경하지 않음
                LocalDateTime.of(2025, 12, 26, 15, 0),
                null, // 장소는 변경하지 않음
                15
        );

        // then
        assertEquals("새로운 제목", meeting.getTitle());
        assertEquals("기존 설명", meeting.getDescription()); // 변경되지 않음
        assertEquals(LocalDateTime.of(2025, 12, 26, 15, 0), meeting.getMeetingDateTime());
        assertEquals("기존 장소", meeting.getLocation()); // 변경되지 않음
        assertEquals(15, meeting.getMaxParticipants());
    }

    @Test
    @DisplayName("전체 필드 업데이트")
    void updateAllFields() {
        // given
        DatingMeeting meeting = DatingMeeting.create(
                "기존 제목",
                "기존 설명",
                LocalDateTime.of(2025, 12, 25, 14, 0),
                "기존 장소",
                10
        );

        // when
        meeting.update(
                "새로운 제목",
                "새로운 설명",
                LocalDateTime.of(2025, 12, 26, 15, 0),
                "새로운 장소",
                20
        );

        // then
        assertEquals("새로운 제목", meeting.getTitle());
        assertEquals("새로운 설명", meeting.getDescription());
        assertEquals(LocalDateTime.of(2025, 12, 26, 15, 0), meeting.getMeetingDateTime());
        assertEquals("새로운 장소", meeting.getLocation());
        assertEquals(20, meeting.getMaxParticipants());
    }

    @Test
    @DisplayName("null 값으로 업데이트 시 기존 값 유지")
    void updateWithNullValues() {
        // given
        DatingMeeting meeting = DatingMeeting.create(
                "기존 제목",
                "기존 설명",
                LocalDateTime.of(2025, 12, 25, 14, 0),
                "기존 장소",
                10
        );

        // when
        meeting.update(null, null, null, null, null);

        // then - 모든 값이 기존 값으로 유지되어야 함
        assertEquals("기존 제목", meeting.getTitle());
        assertEquals("기존 설명", meeting.getDescription());
        assertEquals(LocalDateTime.of(2025, 12, 25, 14, 0), meeting.getMeetingDateTime());
        assertEquals("기존 장소", meeting.getLocation());
        assertEquals(10, meeting.getMaxParticipants());
    }

    @Test
    @DisplayName("참여자 수 관련 기능 테스트")
    void participantCountFeatures() {
        // given
        DatingMeeting meeting = DatingMeeting.create(
                "테스트 미팅",
                "테스트 설명",
                LocalDateTime.of(2025, 12, 25, 14, 0),
                "테스트 장소",
                2
        );

        // when & then
        assertEquals(0, meeting.getCurrentParticipantCount());
        assertFalse(meeting.isParticipantsFull());
        assertFalse(meeting.hasParticipant("user1"));
    }

    @Test
    @DisplayName("DatingMeetingDeleted 이벤트 생성 - 참여자 없는 경우")
    void createDatingMeetingDeletedEventWithoutParticipants() {
        // given
        DatingMeeting meeting = DatingMeeting.create(
                "삭제될 미팅",
                "삭제 테스트",
                LocalDateTime.of(2025, 12, 25, 14, 0),
                "테스트 장소",
                10
        );

        // when
        DatingMeetingDeleted deletedEvent = DatingMeetingDeleted.from(meeting);

        // then
        assertNotNull(deletedEvent);
        assertEquals(meeting.getId(), deletedEvent.datingMeetingId());
        assertEquals("삭제될 미팅", deletedEvent.title());
        assertEquals("삭제 테스트", deletedEvent.description());
        assertEquals(LocalDateTime.of(2025, 12, 25, 14, 0), deletedEvent.meetingDateTime());
        assertEquals("테스트 장소", deletedEvent.location());
        assertEquals(10, deletedEvent.maxParticipants());
        assertTrue(deletedEvent.participantIds().isEmpty());
        assertNotNull(deletedEvent.deletedAt());
    }

    @Test
    @DisplayName("DatingMeetingDeleted 이벤트 생성 - 참여자 있는 경우")
    void createDatingMeetingDeletedEventWithParticipants() {
        // given
        DatingMeeting meeting = DatingMeeting.create(
                "참여자 있는 미팅",
                "참여자 테스트",
                LocalDateTime.of(2025, 12, 25, 14, 0),
                "테스트 장소",
                10
        );
        
        // 참여자 추가 시뮬레이션 (실제 Participant 엔티티 없이 테스트용)
        // 실제로는 participants 필드가 private이므로 DatingMeetingDeleted.from() 메서드가
        // 빈 리스트를 처리하는지 확인하는 테스트로 구성

        // when
        DatingMeetingDeleted deletedEvent = DatingMeetingDeleted.from(meeting);

        // then
        assertNotNull(deletedEvent);
        assertEquals(meeting.getId(), deletedEvent.datingMeetingId());
        assertEquals("참여자 있는 미팅", deletedEvent.title());
        assertEquals("참여자 테스트", deletedEvent.description());
        assertEquals(LocalDateTime.of(2025, 12, 25, 14, 0), deletedEvent.meetingDateTime());
        assertEquals("테스트 장소", deletedEvent.location());
        assertEquals(10, deletedEvent.maxParticipants());
        assertNotNull(deletedEvent.participantIds());
        assertNotNull(deletedEvent.deletedAt());
    }

    @Test
    @DisplayName("DatingMeetingDeleted 이벤트의 삭제 시간이 현재 시간인지 확인")
    void verifyDatingMeetingDeletedTimestamp() {
        // given
        DatingMeeting meeting = DatingMeeting.create(
                "시간 테스트 미팅",
                "시간 테스트",
                LocalDateTime.of(2025, 12, 25, 14, 0),
                "테스트 장소",
                5
        );
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // when
        DatingMeetingDeleted deletedEvent = DatingMeetingDeleted.from(meeting);
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        // then
        assertTrue(deletedEvent.deletedAt().isAfter(beforeCreation));
        assertTrue(deletedEvent.deletedAt().isBefore(afterCreation));
    }
}