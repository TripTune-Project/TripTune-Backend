package com.triptune.member.repository;

import com.triptune.member.MemberTest;
import com.triptune.member.dto.response.MemberProfileResponse;
import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.global.config.QueryDSLConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({QueryDSLConfig.class})
@ActiveProfiles("h2")
class MemberCustomRepositoryImplTest extends MemberTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;

    private ProfileImage profileImage1;
    private ProfileImage profileImage2;

    @Test
    @DisplayName("채팅 사용자들 프로필 조회")
    void findMembersProfileByMemberId() {
        // given
        Member member1 = memberRepository.save(createMember(null, "member1", profileImage1));
        Member member2 = memberRepository.save(createMember(null, "member2", profileImage2));
        profileImage1 = profileImageRepository.save(createProfileImage(null, "member1Image", member1));
        profileImage2 = profileImageRepository.save(createProfileImage(null, "member2Image", member2));



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
}