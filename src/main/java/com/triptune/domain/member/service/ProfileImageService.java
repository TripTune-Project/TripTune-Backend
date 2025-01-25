package com.triptune.domain.member.service;

import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.ProfileImageRepository;
import com.triptune.global.properties.DefaultProfileImageProperties;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProfileImageService {

    private final DefaultProfileImageProperties profileImageProperties;
    private final ProfileImageRepository profileImageRepository;

    public ProfileImage saveDefaultProfileImage() {
        ProfileImage profileImage = ProfileImage.of(
                profileImageProperties.getS3ObjectUrl(),
                profileImageProperties.getOriginalName(),
                profileImageProperties.getFileName(),
                profileImageProperties.getExtension(),
                profileImageProperties.getSize());

        return profileImageRepository.save(profileImage);
    }

}
