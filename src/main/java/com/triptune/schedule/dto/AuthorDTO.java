package com.triptune.schedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthorDTO {

    private String nickname;
    private String profileUrl;

    @Builder
    public AuthorDTO(String nickname, String profileUrl) {
        this.nickname = nickname;
        this.profileUrl = profileUrl;
    }

    public static AuthorDTO of(String nickname, String profileUrl){
        return new AuthorDTO(nickname, profileUrl);
    }
}
