package com.triptune.member.entity;

import com.triptune.common.entity.BaseTimeEntity;
import com.triptune.global.security.oauth.userinfo.OAuth2UserInfo;
import com.triptune.member.enums.DeactivateValue;
import com.triptune.member.enums.SocialType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class SocialMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_member_id")
    private Long socialMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type")
    private SocialType socialType;

    @Column(name = "social_id")
    private String socialId;


    @Builder
    public SocialMember(Long socialMemberId, Member member, SocialType socialType, String socialId) {
        this.socialMemberId = socialMemberId;
        this.member = member;
        this.socialType = socialType;
        this.socialId = socialId;
    }

    public static SocialMember from(Member member, OAuth2UserInfo oAuth2UserInfo){
        SocialMember socialMember = SocialMember.builder()
                .member(member)
                .socialType(oAuth2UserInfo.getSocialType())
                .socialId(oAuth2UserInfo.getSocialId())
                .build();

        member.addSocialMember(socialMember);
        return socialMember;
    }

    public void deactivate() {
        this.socialId = DeactivateValue.DEACTIVATE.name();
    }
}
