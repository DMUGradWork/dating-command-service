package com.grewmeet.dating.datingcommandservice.service;

import com.grewmeet.dating.datingcommandservice.dto.request.CreateDatingMeetingRequest;
import com.grewmeet.dating.datingcommandservice.dto.response.DatingMeetingResponse;

public interface DatingMeetingService {
    
    DatingMeetingResponse createDatingMeeting(CreateDatingMeetingRequest request);
}