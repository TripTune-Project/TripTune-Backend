package com.triptune.global;

import com.triptune.BaseTest;
import com.triptune.global.security.oauth.userinfo.KaKaoUserInfo;
import com.triptune.global.security.oauth.userinfo.NaverUserInfo;

import java.util.HashMap;
import java.util.Map;

public class SocialMemberTest extends BaseTest {
    protected NaverUserInfo createNaverUserInfo(String email){
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("response", Map.of(
                "id", "naverMember",
                "email", email
        ));
        return new NaverUserInfo(attributes);
    }

    protected KaKaoUserInfo createKaKaoUserInfo(String email){
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "kakaoMember");
        attributes.put("kakao_account", Map.of(
                "email", email
        ));
        return new KaKaoUserInfo(attributes);
    }
}
