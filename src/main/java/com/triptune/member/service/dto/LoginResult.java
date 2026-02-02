package com.triptune.member.service.dto;

public record LoginResult(
        String accessToken,
        String refreshToken,
        String nickname
) { }
