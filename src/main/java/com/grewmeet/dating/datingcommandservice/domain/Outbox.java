package com.grewmeet.dating.datingcommandservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String eventId = UUID.randomUUID().toString();

    @NotBlank
    @Column(nullable = false, length = 100)
    private String eventType;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String aggregateType;

    @Column(nullable = false)
    private Long aggregateId;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.PENDING;

    private LocalDateTime processedAt;

    private String errorMessage;

    private Outbox(String eventType, String aggregateType, Long aggregateId, String payload) {
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
    }

    public static Outbox create(String eventType, String aggregateType, Long aggregateId, String payload) {
        return new Outbox(eventType, aggregateType, aggregateId, payload);
    }

    public void markAsProcessed() {
        this.status = OutboxStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = OutboxStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public boolean isPending() {
        return this.status == OutboxStatus.PENDING;
    }

    public enum OutboxStatus {
        PENDING,
        PROCESSED,
        FAILED
    }
}