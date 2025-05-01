package com.triptune.member.entity;

import com.triptune.global.security.oauth.userinfo.OAuth2UserInfo;
import com.triptune.member.enums.SocialType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class SocialMember {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public SocialMember(Long socialMemberId, Member member, SocialType socialType, String socialId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.socialMemberId = socialMemberId;
        this.member = member;
        this.socialType = socialType;
        this.socialId = socialId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SocialMember from(Member member, OAuth2UserInfo oAuth2UserInfo){
        return SocialMember.builder()
                .member(member)
                .socialType(oAuth2UserInfo.getSocialType())
                .socialId(oAuth2UserInfo.getSocialId())
                .createdAt(LocalDateTime.now())
                .build();
    }

}
