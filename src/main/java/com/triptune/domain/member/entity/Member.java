package com.triptune.domain.member.entity;

import com.triptune.domain.member.dto.request.MemberRequest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne
    @JoinColumn(name = "profile_image_id")
    private ProfileImage profileImage;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "password")
    private String password;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "is_social_login")
    private boolean isSocialLogin;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TravelAttendee> travelAttendeeList = new ArrayList<>();


    @Builder
    public Member(Long memberId, ProfileImage profileImage, String userId, String password, String refreshToken, boolean isSocialLogin, String nickname, String email, LocalDateTime createdAt, LocalDateTime updatedAt, List<TravelAttendee> travelAttendeeList) {
        this.memberId = memberId;
        this.profileImage = profileImage;
        this.userId = userId;
        this.password = password;
        this.refreshToken = refreshToken;
        this.isSocialLogin = isSocialLogin;
        this.nickname = nickname;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.travelAttendeeList = travelAttendeeList;
    }

    public static Member from(MemberRequest memberRequest, String encodePassword){
        return Member.builder()
                .userId(memberRequest.getUserId())
                .password(encodePassword)
                .nickname(memberRequest.getNickname())
                .email(memberRequest.getEmail())
                .isSocialLogin(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
