package com.triptune.domain.mypage.service;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.ChangePasswordException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.mypage.dto.request.MyPagePasswordRequest;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class MyPageService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(String userId, MyPagePasswordRequest passwordRequest){
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        boolean isMatch = passwordEncoder.matches(passwordRequest.getNowPassword(), member.getPassword());

        if(!isMatch){
            throw new ChangePasswordException(ErrorCode.INCORRECT_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
    }
}
