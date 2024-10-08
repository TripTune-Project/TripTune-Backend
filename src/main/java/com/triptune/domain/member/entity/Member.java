package com.triptune.domain.member.entity;

import com.triptune.domain.schedule.entity.TravelAttendee;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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

    @Column(name = "file_id")
    private Long fileId;

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<TravelAttendee> travelAttendeeList;

    @Builder
    public Member(Long memberId, String userId, String password, String refreshToken, boolean isSocialLogin, String nickname, String email, LocalDateTime createdAt, LocalDateTime updatedAt, Long fileId, List<TravelAttendee> travelAttendeeList) {
        this.memberId = memberId;
        this.userId = userId;
        this.password = password;
        this.refreshToken = refreshToken;
        this.isSocialLogin = isSocialLogin;
        this.nickname = nickname;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.fileId = fileId;
        this.travelAttendeeList = travelAttendeeList;
    }
}
