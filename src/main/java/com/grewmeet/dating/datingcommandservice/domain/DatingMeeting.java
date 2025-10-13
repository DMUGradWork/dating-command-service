package com.grewmeet.dating.datingcommandservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
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
    @Column(name = "max_male_participants", nullable = false)
    private Integer maxMaleParticipants;

    @Positive
    @Column(name = "max_female_participants", nullable = false)
    private Integer maxFemaleParticipants;

    @Version
    private Long version;

    @OneToMany(mappedBy = "datingMeeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    private DatingMeeting(String title, String description, LocalDateTime meetingDateTime, String location,
                          Integer maxMaleParticipants, Integer maxFemaleParticipants) {
        this.title = title;
        this.description = description;
        this.meetingDateTime = meetingDateTime;
        this.location = location;
        this.maxMaleParticipants = maxMaleParticipants;
        this.maxFemaleParticipants = maxFemaleParticipants;
    }

    public static DatingMeeting create(String title, String description, LocalDateTime meetingDateTime, String location,
                                       Integer maxMaleParticipants, Integer maxFemaleParticipants) {
        return new DatingMeeting(title, description, meetingDateTime, location, maxMaleParticipants, maxFemaleParticipants);
    }

    public void update(String title, String description, LocalDateTime meetingDateTime, String location,
                       Integer maxMaleParticipants, Integer maxFemaleParticipants) {
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
        if (maxMaleParticipants != null) {
            this.maxMaleParticipants = maxMaleParticipants;
        }
        if (maxFemaleParticipants != null) {
            this.maxFemaleParticipants = maxFemaleParticipants;
        }
    }

    public boolean isParticipantsFull() {
        return isMaleFull() && isFemaleFull();
    }

    public boolean isMaleFull() {
        return getMaleParticipantsCount() >= this.maxMaleParticipants;
    }

    public boolean isFemaleFull() {
        return getFemaleParticipantsCount() >= this.maxFemaleParticipants;
    }

    public int getMaleParticipantsCount() {
        return (int) this.participants.stream()
                .filter(Participant::isActive)
                .filter(Participant::isMale)
                .count();
    }

    public int getFemaleParticipantsCount() {
        return (int) this.participants.stream()
                .filter(Participant::isActive)
                .filter(Participant::isFemale)
                .count();
    }

    public boolean hasParticipant(UUID authUserId) {
        return this.participants.stream()
                .anyMatch(participant -> participant.getAuthUserId().equals(authUserId) && participant.isActive());
    }

    public boolean hasParticipant(Long datingUserId) {
        return this.participants.stream()
                .anyMatch(participant -> participant.getDatingUserId().equals(datingUserId) && participant.isActive());
    }

    public int getCurrentParticipantCount() {
        return (int) this.participants.stream()
                .filter(Participant::isActive)
                .count();
    }
}