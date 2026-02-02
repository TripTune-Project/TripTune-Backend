package com.triptune.email.fixture;

import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.dto.request.VerifyAuthRequest;

public class EmailFixture  {
    public static EmailRequest createEmailRequest(String email){
        return EmailRequest.builder()
                .email(email)
                .build();
    }

    public static VerifyAuthRequest createVerifyAuthRequest(String email, String authCode){
        return VerifyAuthRequest.builder()
                .email(email)
                .authCode(authCode)
                .build();

    }
}
