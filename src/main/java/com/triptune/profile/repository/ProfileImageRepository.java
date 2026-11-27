package com.triptune.profile.repository;

import com.triptune.profile.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    @Query("select m.profileImage from Member m where m.memberId = :memberId")
    Optional<ProfileImage> findByMemberId(@Param("memberId") Long memberId);

    @Query("select m.profileImage.s3ObjectKey from Member m where m.memberId = :memberId")
    String findS3ObjectKeyByMemberId(@Param("memberId") Long memberId);
}

