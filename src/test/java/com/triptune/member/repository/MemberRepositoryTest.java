package com.triptune.member.repository;

import com.triptune.member.MemberTest;
import com.triptune.member.dto.response.MemberProfileResponse;
import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.enums.JoinType;
import com.triptune.member.enums.SocialType;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class})
@ActiveProfiles("h2")
class MemberRepositoryTest extends MemberTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private SocialMemberRepository socialMemberRepository;


    @Test
    @DisplayName("사용자 생성")
    void createMember() {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = createNativeTypeMember("member@email.com", profileImage);

        // when
        memberRepository.save(member);

        // then
        assertThat(member.getMemberId()).isNotNull();
        assertThat(member.getCreatedAt()).isEqualTo(member.getUpdatedAt());
    }


    @Test
    @DisplayName("채팅 회원들 프로필 조회")
    void findMembersProfileByMemberId() {
        // given
        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage("member1Image"));
        Member member1 = memberRepository.save(createNativeTypeMember("member1@email.com", profileImage1));

        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage("member2Image"));
        Member member2 = memberRepository.save(createNativeTypeMember("member2@email.com", profileImage2));


        Set<Long> request = new HashSet<>();
        request.add(member1.getMemberId());
        request.add(member1.getMemberId());
        request.add(member2.getMemberId());

        // when
        List<MemberProfileResponse> response = memberRepository.findMembersProfileByMemberId(request);

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getMemberId()).isEqualTo(member1.getMemberId());
        assertThat(response.get(0).getNickname()).isEqualTo(member1.getNickname());
        assertThat(response.get(0).getProfileUrl()).isEqualTo(profileImage1.getS3ObjectUrl());
        assertThat(response.get(1).getMemberId()).isEqualTo(member2.getMemberId());
        assertThat(response.get(1).getNickname()).isEqualTo(member2.getNickname());
        assertThat(response.get(1).getProfileUrl()).isEqualTo(profileImage2.getS3ObjectUrl());
    }

    @Test
    @DisplayName("회원의 일반 정보와 소셜 정보 조회 - 소셜 회원")
    void findByIdWithSocialMembers_socialMember() {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createSocialTypeMember("member1@email.com", profileImage));

        SocialMember socialMember1 = socialMemberRepository.save(createSocialMember(member, SocialType.KAKAO, "kakao"));
        SocialMember socialMember2 = socialMemberRepository.save(createSocialMember(member, SocialType.NAVER, "naver"));

        // when
        Member response = memberRepository.findByIdWithSocialMembers(member.getMemberId()).get();

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getJoinType()).isEqualTo(JoinType.SOCIAL);
        assertThat(response.getSocialMembers().size()).isEqualTo(2);
        assertThat(response.getSocialMembers().get(0).getSocialId()).isEqualTo(socialMember1.getSocialId());
        assertThat(response.getSocialMembers().get(1).getSocialType()).isEqualTo(SocialType.NAVER);
    }

    @Test
    @DisplayName("회원의 일반 정보와 소셜 정보 조회 - 일반 회원")
    void findByIdWithSocialMembers_nativeMember() {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        // when
        Member response = memberRepository.findByIdWithSocialMembers(member.getMemberId()).get();

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getJoinType()).isEqualTo(JoinType.NATIVE);
        assertThat(response.getSocialMembers().size()).isEqualTo(0);
    }


    @Test
    @DisplayName("회원의 일반 정보와 소셜 정보 조회 - 통합 회원")
    void findByIdWithSocialMembers_bothMember() {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createBothTypeMember("member@email.com", profileImage));

        SocialMember socialMember1 = socialMemberRepository.save(createSocialMember(member, SocialType.KAKAO, "kakao"));
        SocialMember socialMember2 = socialMemberRepository.save(createSocialMember(member, SocialType.NAVER, "naver"));

        // when
        Member response = memberRepository.findByIdWithSocialMembers(member.getMemberId()).get();

        // then
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getJoinType()).isEqualTo(JoinType.BOTH);
        assertThat(response.getSocialMembers().size()).isEqualTo(2);
        assertThat(response.getSocialMembers().get(0).getSocialId()).isEqualTo(socialMember1.getSocialId());
        assertThat(response.getSocialMembers().get(1).getSocialType()).isEqualTo(SocialType.NAVER);
    }


}

