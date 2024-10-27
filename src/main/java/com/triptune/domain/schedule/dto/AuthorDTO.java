package com.triptune.domain.schedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthorDTO {

    private String userId;
    private String profileUrl;

    @Builder
    public AuthorDTO(String userId, String profileUrl) {
        this.userId = userId;
        this.profileUrl = profileUrl;
    }

    public static AuthorDTO of(String userId, String profileUrl){
        return new AuthorDTO(userId, profileUrl);
    }
}
