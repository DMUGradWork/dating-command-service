package com.grewmeet.dating.datingcommandservice.repository;

import com.grewmeet.dating.datingcommandservice.domain.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    List<Outbox> findByStatusOrderByCreatedAtAsc(Outbox.OutboxStatus status);
}