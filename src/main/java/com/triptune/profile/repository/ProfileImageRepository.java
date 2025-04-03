package com.triptune.profile.repository;

import com.triptune.profile.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    @Query("select p from ProfileImage p join fetch p.member where p.member.userId = :userId")
    Optional<ProfileImage> findByUserId(@Param("userId") String userId);
}

