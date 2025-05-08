package com.triptune.global.security.oauth.userinfo;

import com.triptune.member.enums.SocialType;

import java.util.Map;

public class KaKaoUserInfo implements OAuth2UserInfo{
    private Map<String, Object> attributes;

    public KaKaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getSocialId() {
        return attributes.get("id").toString();
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.KAKAO;
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount.get("email").toString();
    }

}
