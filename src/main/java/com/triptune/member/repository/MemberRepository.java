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
    boolean existsByUserId(@Param("userId") String userId);
    boolean existsByNickname(@Param("nickname") String nickname);
    boolean existsByEmail(@Param("email") String email);
    Optional<Member> findByUserId(@Param("userId") String userId);
    Optional<Member> findByEmail(@Param("email") String email);
    Optional<Member> findByNickname(@Param("nickname") String nickname);
    boolean existsByUserIdAndEmail(@Param("userId") String userId, @Param("email") String email);

    @Modifying
    @Query("UPDATE Member m SET m.refreshToken = NULL WHERE m.nickname = :nickname")
    void deleteRefreshTokenByNickname(@Param("nickname") String nickname);

}
