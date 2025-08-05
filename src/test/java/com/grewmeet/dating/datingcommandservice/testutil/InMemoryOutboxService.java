package com.grewmeet.dating.datingcommandservice.testutil;

import java.util.ArrayList;
import java.util.List;

public class InMemoryOutboxService {

    private final List<PublishedEvent> publishedEvents = new ArrayList<>();

    public void publishEvent(String eventType, String aggregateType, Long aggregateId, Object eventData) {
        publishedEvents.add(new PublishedEvent(eventType, aggregateType, aggregateId, eventData));
    }

    // 테스트용 메서드들
    public List<PublishedEvent> getPublishedEvents() {
        return new ArrayList<>(publishedEvents);
    }

    public int getEventCount() {
        return publishedEvents.size();
    }

    public PublishedEvent getLastEvent() {
        if (publishedEvents.isEmpty()) {
            return null;
        }
        return publishedEvents.get(publishedEvents.size() - 1);
    }

    public boolean hasEventType(String eventType) {
        return publishedEvents.stream()
                .anyMatch(event -> event.eventType().equals(eventType));
    }

    public boolean hasEventForAggregate(String aggregateType, Long aggregateId) {
        return publishedEvents.stream()
                .anyMatch(event -> event.aggregateType().equals(aggregateType) 
                        && event.aggregateId().equals(aggregateId));
    }

    public void clear() {
        publishedEvents.clear();
    }

    // 이벤트 정보를 담는 record
    public record PublishedEvent(
            String eventType,
            String aggregateType,
            Long aggregateId,
            Object eventData
    ) {}
}