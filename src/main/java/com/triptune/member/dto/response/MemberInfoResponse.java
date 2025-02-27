package com.triptune.member.dto.response;

import com.triptune.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberInfoResponse {
    private String userId;
    private String nickname;
    private String email;
    private String profileImage;

    @Builder
    public MemberInfoResponse(String userId, String nickname, String email, String profileImage) {
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.profileImage = profileImage;
    }

    public static MemberInfoResponse from(Member member){
        return MemberInfoResponse.builder()
                .userId(member.getUserId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImage(member.getProfileImage().getS3ObjectUrl())
                .build();
    }
}
