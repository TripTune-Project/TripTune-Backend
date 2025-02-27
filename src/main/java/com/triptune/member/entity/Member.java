package com.triptune.member.entity;

import com.triptune.bookmark.entity.Bookmark;
import com.triptune.member.dto.request.JoinRequest;
import com.triptune.member.enumclass.AnonymousValue;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.schedule.entity.TravelAttendee;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private ProfileImage profileImage;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "is_social_login")
    private boolean isSocialLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<TravelAttendee> travelAttendeeList = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Bookmark> bookmarkList = new ArrayList<>();

    @Builder
    public Member(Long memberId, ProfileImage profileImage, String userId, String nickname, String email, String password, String refreshToken, boolean isSocialLogin, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isActive, List<TravelAttendee> travelAttendeeList, List<Bookmark> bookmarkList) {
        this.memberId = memberId;
        this.profileImage = profileImage;
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.refreshToken = refreshToken;
        this.isSocialLogin = isSocialLogin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
        this.travelAttendeeList = travelAttendeeList;
        this.bookmarkList = bookmarkList;
    }

    public static Member from(JoinRequest joinRequest, String encodePassword){
        return Member.builder()
                .userId(joinRequest.getUserId())
                .nickname(joinRequest.getNickname())
                .email(joinRequest.getEmail())
                .password(encodePassword)
                .isSocialLogin(false)
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
        this.updatedAt = LocalDateTime.now();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDeactivate() {
        //(닉네임, 아이디, 비밀번호, 리프레시 토큰, 이메일)
        String unknown = AnonymousValue.ANONYMOUS.getValue();

        this.userId = unknown;
        this.nickname = unknown;
        this.email = unknown;
        this.password = unknown;
        this.refreshToken = null;
        this.updatedAt = LocalDateTime.now();
        this.isActive = false;
    }

    public void updateUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
