package com.grewmeet.dating.datingcommandservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grewmeet.dating.datingcommandservice.domain.Outbox;
import com.grewmeet.dating.datingcommandservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishEvent(String eventType, String aggregateType, Long aggregateId, Object eventData) {
        try {
            String payload = objectMapper.writeValueAsString(eventData);
            Outbox outboxEvent = Outbox.create(eventType, aggregateType, aggregateId, payload);
            outboxRepository.save(outboxEvent);
            
            log.info("Outbox event saved: eventType={}, aggregateType={}, aggregateId={}", 
                    eventType, aggregateType, aggregateId);
        } catch (Exception e) {
            log.error("Failed to publish outbox event: eventType={}, aggregateType={}, aggregateId={}", 
                    eventType, aggregateType, aggregateId, e);
            throw new RuntimeException("Failed to publish outbox event", e);
        }
    }
}