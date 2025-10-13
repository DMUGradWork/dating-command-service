package com.grewmeet.dating.datingcommandservice.domain;

import com.grewmeet.dating.datingcommandservice.domain.user.Gender;
import com.grewmeet.dating.datingcommandservice.domain.user.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 분기별 데이팅 서비스 참여 자격 엔티티
 * Auth Service의 User와는 별개로, 특정 분기 동안의 데이팅 서비스 이용 권한과 상태를 관리
 */
@Entity
@Table(name = "dating_users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"auth_user_id", "quarter"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatingUser extends BaseEntity {

    private static final int DEFAULT_MAX_PARTICIPATIONS = 5;
    private static final int MAX_CONCURRENT_PARTICIPATIONS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dating_user_id")
    private Long id;

    /**
     * Auth Service의 User UUID 참조
     */
    @NotNull
    @Column(name = "auth_user_id", nullable = false)
    private UUID authUserId;

    /**
     * 분기 식별자 (예: "2025-Q1", "2025-Q2")
     */
    @NotBlank
    @Column(nullable = false, length = 7)
    private String quarter;

    /**
     * 닉네임 (호스트 신원 공개용)
     */
    @NotBlank
    @Column(nullable = false, length = 50)
    private String nickname;

    /**
     * 성별 (데이팅 매칭에 필요)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    /**
     * 데이팅 서비스 내 권한 (BANNED, GUEST, PARTICIPANT, HOST, ADMIN)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /**
     * 현재 분기 참여 횟수
     */
    @Column(name = "quarter_participation_count", nullable = false)
    private Integer quarterParticipationCount;

    /**
     * 분기당 최대 참여 가능 횟수
     */
    @Column(name = "max_participations", nullable = false)
    private Integer maxParticipations;

    /**
     * 자격 유효 종료일 (분기 마지막 날)
     */
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    /**
     * 참여 이력
     */
    @OneToMany(mappedBy = "datingUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Participant> participants = new ArrayList<>();

    private DatingUser(UUID authUserId, String quarter, String nickname, Gender gender, UserRole role, LocalDateTime validUntil) {
        this.authUserId = authUserId;
        this.quarter = quarter;
        this.nickname = nickname;
        this.gender = gender;
        this.role = role;
        this.quarterParticipationCount = 0;
        this.maxParticipations = DEFAULT_MAX_PARTICIPATIONS;
        this.validUntil = validUntil;
    }

    public static DatingUser create(UUID authUserId, String quarter, String nickname, Gender gender, UserRole role, LocalDateTime validUntil) {
        return new DatingUser(authUserId, quarter, nickname, gender, role, validUntil);
    }

    // === 비즈니스 로직 메서드 ===

    public boolean isMale() {
        return this.gender == Gender.MALE;
    }

    public boolean isFemale() {
        return this.gender == Gender.FEMALE;
    }

    public boolean isBanned() {
        return this.role == UserRole.BANNED;
    }

    public boolean isHost() {
        return this.role == UserRole.HOST || this.role == UserRole.ADMIN;
    }

    /**
     * 이벤트 생성 가능 여부
     */
    public boolean canCreateEvent() {
        return !isBanned() && isHost();
    }

    /**
     * 이벤트 참여 가능 여부 (권한 기반)
     */
    public boolean canParticipateInEvents() {
        return !isBanned();
    }

    /**
     * 현재 활성 참여 수
     */
    public int getActiveParticipationCount() {
        return (int) participants.stream()
                .filter(Participant::isActive)
                .count();
    }

    /**
     * 동시 참여 제한 체크
     */
    public boolean canParticipateMoreEvents() {
        return getActiveParticipationCount() < MAX_CONCURRENT_PARTICIPATIONS;
    }

    /**
     * 분기 참여 횟수 제한 체크
     */
    public boolean canParticipateInQuarter() {
        return quarterParticipationCount < maxParticipations;
    }

    /**
     * 전체 참여 가능 여부
     */
    public boolean canParticipate() {
        return canParticipateInEvents()
            && canParticipateMoreEvents()
            && canParticipateInQuarter();
    }

    /**
     * 특정 이벤트에 이미 참여 중인지 확인
     */
    public boolean hasActiveParticipationInEvent(Long datingMeetingId) {
        return participants.stream()
                .filter(Participant::isActive)
                .anyMatch(p -> p.getDatingMeeting().getId().equals(datingMeetingId));
    }

    /**
     * 참여 횟수 증가
     */
    public void incrementParticipation() {
        this.quarterParticipationCount++;
    }

    /**
     * 참여 횟수 감소 (탈퇴 시)
     */
    public void decrementParticipation() {
        if (this.quarterParticipationCount > 0) {
            this.quarterParticipationCount--;
        }
    }

    /**
     * 자격 유효성 검증
     */
    public boolean isValid() {
        return validUntil == null || LocalDateTime.now().isBefore(validUntil);
    }
}