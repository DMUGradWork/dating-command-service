package com.grewmeet.dating.datingcommandservice.repository;

import com.grewmeet.dating.datingcommandservice.domain.DatingUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DatingUserRepository extends JpaRepository<DatingUser, Long> {

    /**
     * Auth User ID와 분기로 DatingUser 조회
     */
    Optional<DatingUser> findByAuthUserIdAndQuarter(UUID authUserId, String quarter);

    /**
     * Auth User ID와 분기로 존재 여부 확인
     */
    boolean existsByAuthUserIdAndQuarter(UUID authUserId, String quarter);

    /**
     * Auth User ID로 모든 DatingUser 조회 (분기별)
     */
    java.util.List<DatingUser> findByAuthUserId(UUID authUserId);
}