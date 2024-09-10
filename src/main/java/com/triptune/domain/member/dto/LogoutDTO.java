package com.triptune.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LogoutDTO {
    private String userId;

    @Builder
    public LogoutDTO(String userId) {
        this.userId = userId;
    }
}
