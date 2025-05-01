package com.triptune.member.repository;

import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialMemberRepository extends JpaRepository<SocialMember, Long> {

    @Query("select m from SocialMember sm join sm.member m where sm.socialId = :socialId and sm.socialType = :socialType")
    Optional<Member> findBySocialIdAndSocialType(@Param("socialId")String socialId, @Param("socialType") SocialType socialType);
}
