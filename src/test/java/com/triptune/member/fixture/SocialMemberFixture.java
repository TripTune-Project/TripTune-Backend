package com.triptune.member.fixture;

import com.triptune.global.security.oauth.userinfo.KaKaoUserInfo;
import com.triptune.global.security.oauth.userinfo.NaverUserInfo;
import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.enums.SocialType;

import java.util.HashMap;
import java.util.Map;

public class SocialMemberFixture {

    public static SocialMember createSocialMember(Member member, SocialType socialType, String socialId){
        return SocialMember.createSocialMember(
                member,
                socialType,
                socialId
        );
    }

    public static NaverUserInfo createNaverUserInfo(String email){
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("response", Map.of(
                "id", "naverMember",
                "email", email
        ));
        return new NaverUserInfo(attributes);
    }

    public static KaKaoUserInfo createKaKaoUserInfo(String email){
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "kakaoMember");
        attributes.put("kakao_account", Map.of(
                "email", email
        ));
        return new KaKaoUserInfo(attributes);
    }
}
