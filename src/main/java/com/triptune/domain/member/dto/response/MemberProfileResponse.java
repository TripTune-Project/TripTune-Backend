package com.triptune.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberProfileResponse {

    private Long memberId;
    private String nickname;
    private String profileUrl;

    @Builder
    public MemberProfileResponse(Long memberId, String nickname, String profileUrl) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
    }

    public static MemberProfileResponse of(Long memberId, String nickname, String profileUrl){
        return new MemberProfileResponse(memberId, nickname, profileUrl);
    }
}
