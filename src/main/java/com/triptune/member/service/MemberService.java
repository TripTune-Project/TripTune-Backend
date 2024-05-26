package com.triptune.member.service;

import com.triptune.member.dto.MemberDTO;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public void join(MemberDTO.Request memberDTO) {
        Member member = Member.builder()
                .userId(memberDTO.getUserId())
                .password(null)
                .nickname(memberDTO.getNickname())
                .email(memberDTO.getEmail())
                .isSocialLogin(false)
                .build();

        memberRepository.save(member);
    }

}
