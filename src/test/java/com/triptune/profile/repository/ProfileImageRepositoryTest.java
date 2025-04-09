package com.triptune.profile.repository;

import com.triptune.global.config.QueryDSLConfig;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.ProfileImageTest;
import com.triptune.profile.entity.ProfileImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("h2")
class ProfileImageRepositoryTest extends ProfileImageTest {

    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private MemberRepository memberRepository;

    @Test
    @DisplayName("사용자 아이디 이용해 프로필 이미지 조회")
    void findProfileImageByEmail() {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member1Image"));
        Member member1 = memberRepository.save(createMember(null, "member1", profileImage));

        // when
        Optional<ProfileImage> response = profileImageRepository.findByEmail(member1.getEmail());

        // then
        assertThat(response.get().getProfileImageId()).isEqualTo(profileImage.getProfileImageId());
        assertThat(response.get().getFileName()).isEqualTo(profileImage.getFileName());
        assertThat(response.get().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
    }

}