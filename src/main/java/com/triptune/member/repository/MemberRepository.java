package com.triptune.member.repository;

import com.triptune.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    boolean existsByUserId(String userId);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
    Optional<Member> findByUserId(String userId);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByMemberId(Long memberId);
    Optional<Member> findByNickname(String nickname);
    boolean existsByUserIdAndEmail(String userId, String email);
    Long findMemberIdByUserId(String userId);

    @Modifying
    @Query("UPDATE Member m SET m.refreshToken = NULL WHERE m.nickname = :nickname")
    void deleteRefreshTokenByNickname(@Param("nickname") String nickname);

}
