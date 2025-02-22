package com.triptune.member.dto.response;

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

    public static FindIdResponse of(String userId){
        return new FindIdResponse(userId);
    }
}
