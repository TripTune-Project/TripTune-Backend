package com.triptune.member.entity;

import com.triptune.common.entity.BaseTimeEntity;
import com.triptune.global.security.oauth.userinfo.OAuth2UserInfo;
import com.triptune.member.dto.request.JoinRequest;
import com.triptune.member.enums.DeactivateValue;
import com.triptune.member.enums.JoinType;
import com.triptune.profile.entity.ProfileImage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id")
    private ProfileImage profileImage;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_type")
    private JoinType joinType;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<SocialMember> socialMembers = new ArrayList<>();


    private Member(String email, String password, String nickname, JoinType joinType, boolean isActive) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.joinType = joinType;
        this.isActive = isActive;
    }

    private Member(String email, String nickname, JoinType joinType, boolean isActive) {
        this.email = email;
        this.nickname = nickname;
        this.joinType = joinType;
        this.isActive = isActive;
    }

    public static Member createNativeMember(String email, String encodedPassword, String nickname, ProfileImage profileImage){
        Member member = new Member(
                email,
                encodedPassword,
                nickname,
                JoinType.NATIVE,
                true
        );
        member.initProfileImage(profileImage);
        return member;
    }

    public static Member createSocialMember(String email, String nickname, ProfileImage profileImage){
        Member member = new Member(
                email,
                nickname,
                JoinType.SOCIAL,
                true
        );
        member.initProfileImage(profileImage);
        return member;
    }

    private void initProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
        profileImage.assignMember(this);
    }

    public boolean isMatchRefreshToken(String refreshToken){
        return refreshToken != null && refreshToken.equals(this.refreshToken);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void deactivate() {
        //(이메일, 닉네임, 비밀번호, 리프레시 토큰)
        String deactivation = DeactivateValue.DEACTIVATE.name();

        this.nickname = deactivation;
        this.email = deactivation;
        this.password = deactivation;
        this.refreshToken = null;
        this.isActive = false;

        for (SocialMember socialMember : socialMembers) {
            socialMember.deactivate();
        }
    }

    public void updateProfileImage(ProfileImage newProfileImage) {
        // 기존 이미지와 관계 끊기
        this.profileImage.assignMember(null);

        this.profileImage = newProfileImage;
        newProfileImage.assignMember(this);
    }


    public void resetPassword(String password) {
        if (isSocialMember()){
            this.joinType = JoinType.BOTH;
        }

        this.password = password;
    }

    public boolean isSocialMember() {
        return this.isActive && this.joinType.equals(JoinType.SOCIAL) && this.password == null;
    }


    public void linkSocialAccount() {
        if (isNativeMember()){
            this.joinType = JoinType.BOTH;
        }
    }

    public boolean isNativeMember(){
        return this.isActive && joinType.equals(JoinType.NATIVE);
    }

    public void addSocialMember(SocialMember socialMember){
        this.socialMembers.add(socialMember);
    }

    public void logout() {
        this.refreshToken = null;
    }
}
