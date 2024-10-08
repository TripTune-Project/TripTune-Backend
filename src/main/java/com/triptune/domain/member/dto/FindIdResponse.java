package com.triptune.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FindIdResponse {

    private String userId;

    @Builder
    public FindIdResponse(String userId) {
        this.userId = userId;
    }
}
