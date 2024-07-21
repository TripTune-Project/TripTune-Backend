package com.triptune.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class FindDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FindId{
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FindPassword{
        private String userId;
        private String email;
    }
}
