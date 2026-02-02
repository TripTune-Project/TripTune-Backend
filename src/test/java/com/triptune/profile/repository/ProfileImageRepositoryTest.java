package com.triptune.profile.repository;

import com.triptune.global.config.QuerydslConfig;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.entity.ProfileImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
class ProfileImageRepositoryTest {

    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private MemberRepository memberRepository;

    @Test
    @DisplayName("프로필 이미지 생성")
    void createMember() {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("testProfileImage");

        // when
        profileImageRepository.save(profileImage);

        // then
        assertThat(profileImage.getProfileImageId()).isNotNull();
        assertThat(profileImage.getCreatedAt()).isEqualTo(profileImage.getUpdatedAt());
    }


    @Test
    @DisplayName("회원 아이디 이용해 프로필 이미지 조회")
    void findByMemberId() {
        // given
        ProfileImage profileImage = profileImageRepository.save(ProfileImageFixture.createProfileImage("member1Image"));
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member1@email.com", profileImage));

        // when
        ProfileImage response = profileImageRepository.findByMemberId(member.getMemberId()).get();

        // then
        assertThat(response.getProfileImageId()).isEqualTo(profileImage.getProfileImageId());
        assertThat(response.getFileName()).isEqualTo(profileImage.getFileName());
        assertThat(response.getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
    }

}