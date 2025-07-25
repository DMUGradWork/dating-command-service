package com.grewmeet.dating.datingcommandservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 100)
    private String userId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dating_meeting_id", nullable = false)
    private DatingMeeting datingMeeting;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status = ParticipantStatus.ACTIVE;

    private Participant(String userId, DatingMeeting datingMeeting) {
        this.userId = userId;
        this.datingMeeting = datingMeeting;
        this.status = ParticipantStatus.ACTIVE;
    }

    public static Participant create(String userId, DatingMeeting datingMeeting) {
        return new Participant(userId, datingMeeting);
    }

    public void withdraw() {
        this.status = ParticipantStatus.WITHDRAWN;
    }

    public boolean isActive() {
        return this.status == ParticipantStatus.ACTIVE;
    }

    public enum ParticipantStatus {
        ACTIVE,
        WITHDRAWN
    }
}