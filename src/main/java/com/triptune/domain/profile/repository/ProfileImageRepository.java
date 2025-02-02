package com.triptune.domain.profile.repository;

import com.triptune.domain.profile.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    Optional<ProfileImage> findByMember_UserId(String userId);
}
