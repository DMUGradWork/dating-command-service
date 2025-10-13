package com.grewmeet.dating.datingcommandservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Entity
@Table(name = "participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dating_user_id", nullable = false)
    private DatingUser datingUser;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dating_meeting_id", nullable = false)
    private DatingMeeting datingMeeting;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantStatus status = ParticipantStatus.ACTIVE;

    private Participant(DatingUser datingUser, DatingMeeting datingMeeting) {
        this.datingUser = datingUser;
        this.datingMeeting = datingMeeting;
        this.status = ParticipantStatus.ACTIVE;
    }

    public static Participant create(DatingUser datingUser, DatingMeeting datingMeeting) {
        return new Participant(datingUser, datingMeeting);
    }

    public void withdraw() {
        this.status = ParticipantStatus.WITHDRAWN;
    }

    public boolean isActive() {
        return this.status == ParticipantStatus.ACTIVE;
    }

    public boolean isMale() {
        return datingUser.isMale();
    }

    public boolean isFemale() {
        return datingUser.isFemale();
    }

    public UUID getAuthUserId() {
        return datingUser.getAuthUserId();
    }

    public Long getDatingUserId() {
        return datingUser.getId();
    }

    public enum ParticipantStatus {
        ACTIVE,
        WITHDRAWN
    }
}