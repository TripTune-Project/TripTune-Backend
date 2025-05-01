package com.triptune.global.security.oauth.userinfo;

import com.triptune.member.enums.SocialType;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo{
    Map<String, Object> attributes;

    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getSocialId() {
        return attributes.get("id").toString();
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.NAVER;
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }


}
