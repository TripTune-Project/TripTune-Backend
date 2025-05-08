package com.triptune.member.entity;

import com.triptune.global.security.oauth.userinfo.OAuth2UserInfo;
import com.triptune.member.dto.request.JoinRequest;
import com.triptune.member.enums.AnonymousValue;
import com.triptune.member.enums.JoinType;
import com.triptune.profile.entity.ProfileImage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private boolean isActive;


    @Builder
    public Member(Long memberId, ProfileImage profileImage, String email, String password, String nickname, String refreshToken, JoinType joinType, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isActive) {
        this.memberId = memberId;
        this.profileImage = profileImage;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.refreshToken = refreshToken;
        this.joinType = joinType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
    }

    public static Member from(JoinRequest joinRequest, ProfileImage profileImage, String encodePassword){
        return Member.builder()
                .profileImage(profileImage)
                .email(joinRequest.getEmail())
                .password(encodePassword)
                .nickname(joinRequest.getNickname())
                .joinType(JoinType.NATIVE)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    public static Member from(ProfileImage profileImage, OAuth2UserInfo oAuth2UserInfo, String nickname){
        return Member.builder()
                .profileImage(profileImage)
                .email(oAuth2UserInfo.getEmail())
                .nickname(nickname)
                .joinType(JoinType.SOCIAL)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }



    public boolean isMatchRefreshToken(String refreshToken){
        return this.refreshToken.equals(refreshToken);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updatePassword(String password) {
        this.password = password;
        updateUpdatedAt();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        updateUpdatedAt();
    }

    public void updateEmail(String email) {
        this.email = email;
        updateUpdatedAt();
    }

    public void updateDeactivate() {
        //(닉네임, 아이디, 비밀번호, 리프레시 토큰, 이메일)
        String unknown = AnonymousValue.ANONYMOUS.getValue();

        this.nickname = unknown;
        this.email = unknown;
        this.password = unknown;
        this.refreshToken = null;
        this.isActive = false;
        updateUpdatedAt();
    }

    public void updateProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
        updateUpdatedAt();
    }

    public void updateOAuth2JoinType(){
        if (this.joinType.equals(JoinType.NATIVE)){
            this.joinType = JoinType.BOTH;
        }
        updateUpdatedAt();
    }

    public void updateUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

}
