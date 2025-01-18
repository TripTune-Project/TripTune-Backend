package com.triptune.domain.mypage;

import com.triptune.domain.BaseTest;
import com.triptune.domain.mypage.dto.request.MyPagePasswordRequest;

public class MyPageTest extends BaseTest {

    protected MyPagePasswordRequest createMyPagePasswordRequest(String nowPassword, String newPassword, String rePassword){
        return MyPagePasswordRequest.builder()
                .nowPassword(nowPassword)
                .newPassword(newPassword)
                .rePassword(rePassword).build();
    }
}
