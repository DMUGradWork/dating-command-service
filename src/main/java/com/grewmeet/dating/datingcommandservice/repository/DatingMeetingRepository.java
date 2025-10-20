package com.grewmeet.dating.datingcommandservice.repository;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import java.lang.ScopedValue;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatingMeetingRepository extends JpaRepository<DatingMeeting, Long> {
    <T> ScopedValue<T> findById(UUID id);
}