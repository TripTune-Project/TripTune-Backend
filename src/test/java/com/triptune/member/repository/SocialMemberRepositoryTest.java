package com.triptune.member.repository;

import com.triptune.global.config.QueryDSLConfig;
import com.triptune.member.MemberTest;
import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.enumclass.SocialType;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("h2")
class SocialMemberRepositoryTest extends MemberTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private SocialMemberRepository socialMemberRepository;


    @Test
    @DisplayName("소셜 정보로 사용자 조회")
    void findBySocialInfo() {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "image"));
        Member member = memberRepository.save(createMember(null, "member@email.com", true, profileImage));
        SocialMember socialMember = socialMemberRepository.save(createSocialMember(null, member, "socialMember", SocialType.NAVER));

        // when
        Member response = socialMemberRepository.findBySocialIdAndSocialType(socialMember.getSocialId(), socialMember.getSocialType())
                .get();

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage().getS3ObjectUrl()).isEqualTo(profileImage.getS3ObjectUrl());
    }

}