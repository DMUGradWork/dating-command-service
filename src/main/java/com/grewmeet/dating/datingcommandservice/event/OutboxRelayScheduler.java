package com.grewmeet.dating.datingcommandservice.event;

import com.grewmeet.dating.datingcommandservice.domain.Outbox;
import com.grewmeet.dating.datingcommandservice.domain.Outbox.OutboxStatus;
import com.grewmeet.dating.datingcommandservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Outbox 패턴 구현 - Relay Scheduler
 *
 * Outbox 테이블에 저장된 PENDING 상태의 이벤트를 주기적으로 폴링하여
 * Kafka로 발행하는 스케줄러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Outbox 이벤트 릴레이 (5초마다 실행)
     *
     * 동작 방식:
     * 1. PENDING 상태의 Outbox 이벤트 조회
     * 2. 각 이벤트를 Kafka로 발행
     * 3. 발행 성공 시 PROCESSED, 실패 시 FAILED로 상태 업데이트
     */
    @Scheduled(fixedDelay = 5000) // 5초마다 실행
    @Transactional
    public void relayOutboxEvents() {
        List<Outbox> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Processing {} pending outbox events", pendingEvents.size());

        for (Outbox event : pendingEvents) {
            try {
                // Kafka 토픽 결정
                String topic = determineKafkaTopic(event.getEventType());
                String key = event.getAggregateType() + ":" + event.getAggregateId();

                log.info("Publishing outbox event to Kafka: eventType={}, topic={}, key={}, payload={}",
                        event.getEventType(), topic, key, event.getPayload());

                // Kafka로 이벤트 발행 (동기 방식으로 변경)
                kafkaTemplate.send(topic, key, event.getPayload()).get();

                log.info("Successfully published event to Kafka: topic={}, key={}, eventType={}",
                        topic, key, event.getEventType());

                // 발행 성공 처리
                event.markAsProcessed();
                outboxRepository.save(event);

                log.info("Outbox event marked as PROCESSED: id={}, eventType={}",
                        event.getId(), event.getEventType());

            } catch (Exception e) {
                // 발행 실패 처리
                log.error("Failed to publish outbox event: id={}, eventType={}",
                        event.getId(), event.getEventType(), e);

                event.markAsFailed(e.getMessage());
                outboxRepository.save(event);
            }
        }

        log.info("Completed processing {} outbox events", pendingEvents.size());
    }

    /**
     * 이벤트 타입에 따른 Kafka Topic 결정
     */
    private String determineKafkaTopic(String eventType) {
        return switch (eventType) {
            case "DatingMeetingCreated" -> "grewmeet.dating.meeting.created";
            case "DatingMeetingUpdated" -> "grewmeet.dating.meeting.updated";
            case "DatingMeetingDeleted" -> "grewmeet.dating.meeting.deleted";
            case "DatingMeetingParticipantJoined" -> "grewmeet.dating.participant.joined";
            case "DatingMeetingParticipantLeft" -> "grewmeet.dating.participant.left";
            default -> {
                log.warn("Unknown event type: {}, using default topic", eventType);
                yield "grewmeet.dating.events";
            }
        };
    }
}
