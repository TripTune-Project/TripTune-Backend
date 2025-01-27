package com.triptune.domain.profile.service;

import com.triptune.domain.profile.entity.ProfileImage;
import com.triptune.domain.profile.repository.ProfileImageRepository;
import com.triptune.global.properties.DefaultProfileImageProperties;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileImageService {

    private final DefaultProfileImageProperties profileImageProperties;
    private final ProfileImageRepository profileImageRepository;

    public ProfileImage saveDefaultProfileImage() {
        ProfileImage profileImage = ProfileImage.from(profileImageProperties);
        return profileImageRepository.save(profileImage);
    }

}
