package com.triptune.global.security.oauth.userinfo;

import com.triptune.member.enums.SocialType;

public interface OAuth2UserInfo {
    String getSocialId();
    SocialType getSocialType();
    String getEmail();
    String getNickname();

}
