package com.triptune.email;

import com.triptune.BaseTest;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.dto.request.VerifyAuthRequest;

public class EmailTest extends BaseTest {
    protected EmailRequest createEmailRequest(String email){
        return EmailRequest.builder()
                .email(email)
                .build();
    }

    protected VerifyAuthRequest createVerifyAuthRequest(String email, String authCode){
        return VerifyAuthRequest.builder()
                .email(email)
                .authCode(authCode)
                .build();

    }
}
