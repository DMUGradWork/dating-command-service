package com.grewmeet.dating.datingcommandservice.util;

public class IdParser {

    public static Long parseDatingMeetingId(String datingMeetingId) {
        if (datingMeetingId == null || datingMeetingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Dating meeting ID cannot be null or empty");
        }
        
        try {
            return Long.parseLong(datingMeetingId.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid dating meeting ID format: " + datingMeetingId, e);
        }
    }
}