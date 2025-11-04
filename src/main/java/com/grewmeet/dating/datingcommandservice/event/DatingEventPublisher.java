package com.grewmeet.dating.datingcommandservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatingEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishMeetingCreated(Object event, String aggregateId) {
        publish("grewmeet.dating.meeting.created", aggregateId, event);
    }

    public void publishMeetingUpdated(Object event, String aggregateId) {
        publish("grewmeet.dating.meeting.updated", aggregateId, event);
    }

    public void publishMeetingDeleted(Object event, String aggregateId) {
        publish("grewmeet.dating.meeting.deleted", aggregateId, event);
    }

    public void publishParticipantJoined(Object event, String aggregateId) {
        publish("grewmeet.dating.participant.joined", aggregateId, event);
    }

    public void publishParticipantLeft(Object event, String aggregateId) {
        publish("grewmeet.dating.participant.left", aggregateId, event);
    }

    private void publish(String topic, String key, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            log.info("Publishing event to Kafka: topic={}, key={}, payload={}", topic, key, payload);
            kafkaTemplate.send(topic, key, payload);
            log.info("Successfully published event to topic: {}", topic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event to JSON: {}", event, e);
            throw new RuntimeException("Failed to publish event", e);
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka: topic={}, key={}", topic, key, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
