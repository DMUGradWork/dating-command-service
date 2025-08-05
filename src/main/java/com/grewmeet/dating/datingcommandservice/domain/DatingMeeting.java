package com.grewmeet.dating.datingcommandservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dating_meetings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatingMeeting extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime meetingDateTime;

    @NotBlank
    @Column(nullable = false, length = 300)
    private String location;

    @Positive
    @Column(nullable = false)
    private Integer maxParticipants;

    @Version
    private Long version;

    @OneToMany(mappedBy = "datingMeeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    private DatingMeeting(String title, String description, LocalDateTime meetingDateTime, String location, Integer maxParticipants) {
        this.title = title;
        this.description = description;
        this.meetingDateTime = meetingDateTime;
        this.location = location;
        this.maxParticipants = maxParticipants;
    }

    public static DatingMeeting create(String title, String description, LocalDateTime meetingDateTime, String location, Integer maxParticipants) {
        return new DatingMeeting(title, description, meetingDateTime, location, maxParticipants);
    }

    public void update(String title, String description, LocalDateTime meetingDateTime, String location, Integer maxParticipants) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (meetingDateTime != null) {
            this.meetingDateTime = meetingDateTime;
        }
        if (location != null) {
            this.location = location;
        }
        if (maxParticipants != null) {
            this.maxParticipants = maxParticipants;
        }
    }

    public boolean isParticipantsFull() {
        return this.participants.size() >= this.maxParticipants;
    }

    public boolean hasParticipant(String userId) {
        return this.participants.stream()
                .anyMatch(participant -> participant.getUserId().equals(userId) && participant.isActive());
    }

    public int getCurrentParticipantCount() {
        return (int) this.participants.stream()
                .filter(Participant::isActive)
                .count();
    }
}