package com.triptune.email;

import com.triptune.BaseTest;
import com.triptune.email.dto.request.VerifyAuthRequest;

public class EmailTest extends BaseTest {
    protected VerifyAuthRequest createVerifyAuthRequest(String email, String authCode){
        return VerifyAuthRequest.builder()
                .email(email)
                .authCode(authCode)
                .build();

    }
}
