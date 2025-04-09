package com.triptune.global.service;

import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return new CustomUserDetails(member);
    }
}
