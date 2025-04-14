package com.triptune.global.service;

import com.triptune.member.enumclass.SocialType;

import java.util.Map;

public class KaKaoUserInfo implements OAuth2UserInfo{
    private Map<String, Object> attributes;

    public KaKaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getSocialId() {
        return "";
    }

    @Override
    public SocialType getSocialType() {
        return null;
    }

    @Override
    public String getEmail() {
        return "";
    }

    @Override
    public String getNickname() {
        return "";
    }
}
