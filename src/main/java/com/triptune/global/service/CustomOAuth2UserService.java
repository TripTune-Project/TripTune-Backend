package com.triptune.global.service;

import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.OAuth2Exception;
import com.triptune.global.util.JwtUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.repository.MemberRepository;
import com.triptune.member.repository.SocialMemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final ProfileImageService profileImageService;
    private final SocialMemberRepository socialMemberRepository;
    private final JwtUtils jwtUtils;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 유저 정보 가져오기
        Map<String, Object> attributes = super.loadUser(userRequest).getAttributes();

        // 2. registrationId 가져오기 (third-party id)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 4. 유저 정보 dto 생성
        OAuth2UserInfo oAuth2UserInfo = switch (registrationId){
            case "naver" -> new NaverUserInfo(attributes);
            case "kakao" -> new KaKaoUserInfo(attributes);
            default -> throw new OAuth2Exception(ErrorCode.ILLEGAL_REGISTRATION_ID);
        };

        // 5. 회원가입 및 로그인
        Member member = getOrCreateSocialMember(oAuth2UserInfo);
        member.updateRefreshToken(jwtUtils.createRefreshToken(member.getEmail()));
        return new CustomUserDetails(member, attributes);
    }

    public Member getOrCreateSocialMember(OAuth2UserInfo oAuth2UserInfo){
        // 1. 이미 가입되어 잇는 소셜 회원인지 확인(socialMember 의 id, socialType 체크)
        // 2. 가입되어 있으면 return Social Member
        return socialMemberRepository.findBySocialIdAndSocialType(oAuth2UserInfo.getSocialId(), oAuth2UserInfo.getSocialType())
                .orElseGet(() -> createSocialMember(oAuth2UserInfo));
    }

    public Member createSocialMember(OAuth2UserInfo oAuth2UserInfo){
        // 3. Member 생성해서 save
        validateUniqueEmail(oAuth2UserInfo.getEmail());
        validateUniqueNickname(oAuth2UserInfo.getNickname());

        ProfileImage profileImage = profileImageService.saveDefaultProfileImage();
        Member member = Member.from(profileImage, oAuth2UserInfo);
        Member savedMember = memberRepository.save(member);

        // 4. SocialMember 객체 생성해서 Member 연결 후 save, return
        SocialMember socialMember = SocialMember.from(savedMember, oAuth2UserInfo);
        socialMemberRepository.save(socialMember);

        return savedMember;
    }

    public void validateUniqueEmail(String email){
        if(memberRepository.existsByEmail(email)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
        }
    }

    public void validateUniqueNickname(String nickname){
        if(memberRepository.existsByNickname(nickname)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_NICKNAME);
        }
    }
}
