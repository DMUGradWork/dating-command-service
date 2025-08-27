package com.grewmeet.dating.datingcommandservice.service;

import com.grewmeet.dating.datingcommandservice.dto.request.CreateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.request.JoinEventRequest;
import com.grewmeet.dating.datingcommandservice.dto.request.UpdateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;
import com.grewmeet.dating.datingcommandservice.dto.response.ParticipantResponse;

public interface DatingMeetingService {
    DatingMeetingResponse createDatingMeeting(CreateDatingMeetingRequest request);
    DatingMeetingResponse updateDatingMeeting(String datingMeetingId, UpdateDatingMeetingRequest request);
    void deleteDatingMeeting(String datingMeetingId);
    ParticipantResponse joinEvent(String datingMeetingId, JoinEventRequest request);
    void leaveEvent(String datingMeetingId, Long participantId);
}