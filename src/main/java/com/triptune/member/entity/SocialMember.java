package com.triptune.member.entity;

import com.triptune.common.entity.BaseTimeEntity;
import com.triptune.member.enums.DeactivateValue;
import com.triptune.member.enums.SocialType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private SocialMember(SocialType socialType, String socialId) {
        this.socialType = socialType;
        this.socialId = socialId;
    }

    public static SocialMember createSocialMember(Member member, SocialType socialType, String socialId){
        SocialMember socialMember = new SocialMember(
                socialType,
                socialId
        );

        socialMember.assignMember(member);
        return socialMember;
    }

    public void deactivate() {
        this.socialId = DeactivateValue.DEACTIVATE.name();
    }

    public void assignMember(Member member) {
        this.member = member;
        member.addSocialMember(this);
    }
}
