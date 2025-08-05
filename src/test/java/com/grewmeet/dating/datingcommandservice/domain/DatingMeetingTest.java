package com.grewmeet.dating.datingcommandservice.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
}