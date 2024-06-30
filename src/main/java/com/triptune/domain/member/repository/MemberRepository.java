package com.triptune.domain.member.repository;

import com.triptune.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUserId(String userId);
    boolean existsByNickname(String nickname);
    Member findByUserId(String userId);
    Member findByEmail(String email);

    @Modifying
    @Query("UPDATE Member m SET m.refreshToken = NULL WHERE m.userId = :userId")
    void deleteRefreshToken(@Param("userId") String userId);
}
