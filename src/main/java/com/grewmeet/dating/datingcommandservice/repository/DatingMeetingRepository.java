package com.grewmeet.dating.datingcommandservice.repository;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatingMeetingRepository extends JpaRepository<DatingMeeting, Long> {
}