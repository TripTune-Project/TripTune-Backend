package com.triptune.global.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final Long memberId;
    private final String nickname;
    private final String password;
    private final Map<String, Object> attributes;


    public CustomUserDetails(Long memberId, String nickname, String password, Map<String, Object> attributes) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.password = password;
        this.attributes = attributes;
    }

    public CustomUserDetails(Long memberId, String nickname, String password) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.password = password;
        this.attributes = new HashMap<>();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return memberId.toString();
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
        return this.nickname;
    }


    public Long getMemberId(){
        return this.memberId;
    }

}
