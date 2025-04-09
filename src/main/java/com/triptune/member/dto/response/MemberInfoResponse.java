package com.triptune.member.dto.response;

import com.triptune.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberInfoResponse {

    private String email;
    private String nickname;
    private String profileImage;

    @Builder
    public MemberInfoResponse(String email, String nickname, String profileImage) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public static MemberInfoResponse from(Member member){
        return MemberInfoResponse.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage().getS3ObjectUrl())
                .build();
    }
}
