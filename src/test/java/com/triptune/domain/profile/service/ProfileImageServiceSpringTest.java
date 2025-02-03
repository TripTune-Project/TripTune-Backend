package com.triptune.domain.profile.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.profile.ProfileImageTest;
import com.triptune.domain.profile.entity.ProfileImage;
import com.triptune.domain.profile.repository.ProfileImageRepository;
import com.triptune.global.properties.DefaultProfileImageProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ProfileImageServiceSpringTest extends ProfileImageTest {
    private static final long DEFAULT_PROFILE_IMAGE_SIZE = 14914;
    private static final String DEFAULT_PROFILE_IMAGE_NAME = "defaultProfileImage.png";

    private final ProfileImageService profileImageService;
    private final ProfileImageRepository profileImageRepository;
    private final DefaultProfileImageProperties profileImageProperties;
    private final MemberRepository memberRepository;

    @Autowired
    ProfileImageServiceSpringTest(ProfileImageService profileImageService, ProfileImageRepository profileImageRepository, DefaultProfileImageProperties profileImageProperties, MemberRepository memberRepository) {
        this.profileImageService = profileImageService;
        this.profileImageRepository = profileImageRepository;
        this.profileImageProperties = profileImageProperties;
        this.memberRepository = memberRepository;
    }


    @Test
    @DisplayName("프로필 이미지 업로드")
    void saveDefaultProfileImage(){
        // given
        Member member = memberRepository.save(createMember(null, "member"));

        // when
        ProfileImage response = profileImageService.saveDefaultProfileImage(member);

        // then
        assertThat(response.getOriginalName()).isEqualTo(DEFAULT_PROFILE_IMAGE_NAME);
        assertThat(response.getFileSize()).isEqualTo(DEFAULT_PROFILE_IMAGE_SIZE);
    }



}