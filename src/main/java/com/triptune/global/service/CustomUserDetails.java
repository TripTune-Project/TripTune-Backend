package com.triptune.global.service;

import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.type.descriptor.java.ObjectJavaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final Member member;
    private final Long memberId;
    private final Map<String, Object> attributes;

    public CustomUserDetails(Member member) {
        this.member = member;
        this.memberId = member.getMemberId();
        this.attributes = new HashMap<>();
    }

    public CustomUserDetails(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.memberId = member.getMemberId();
        this.attributes = attributes;
    }

    public CustomUserDetails(Member member, Long memberId, Map<String, Object> attributes) {
        this.member = member;
        this.memberId = memberId;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getMemberId().toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return member.getNickname();
    }

    public String getRefreshToken(){
        return member.getRefreshToken();
    }

    public Long getMemberId(){
        return memberId;
    }

}
