package com.triptune.global.service;

import com.triptune.member.enumclass.SocialType;

public interface OAuth2UserInfo {
    String getSocialId();
    SocialType getSocialType();
    String getEmail();
    String getNickname();

}
