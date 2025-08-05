package com.grewmeet.dating.datingcommandservice.util;

public class IdParser {

    public static Long parseEventId(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }
        
        try {
            return Long.parseLong(eventId.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid event ID format: " + eventId, e);
        }
    }
}