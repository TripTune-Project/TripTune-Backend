package com.triptune.member.dto;

public record LoginResult(
        String accessToken,
        String refreshToken,
        String nickname
) { }
