package com.triptune.global;

import com.triptune.BaseTest;
import com.triptune.global.service.NaverUserInfo;

import java.util.HashMap;
import java.util.Map;

public class SocialMemberTest extends BaseTest {
    protected NaverUserInfo createNaverUserInfo(String email, String nickname){
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("response", Map.of(
                "id", "naverMember",
                "email", email,
                "nickname", nickname
        ));
        return new NaverUserInfo(attributes);
    }
}
