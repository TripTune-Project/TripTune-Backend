package com.triptune.global.security.oauth;

import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.security.CustomUserDetails;
import com.triptune.global.security.oauth.exception.OAuth2Exception;
import com.triptune.global.security.oauth.userinfo.KaKaoUserInfo;
import com.triptune.global.security.oauth.userinfo.NaverUserInfo;
import com.triptune.global.security.oauth.userinfo.OAuth2UserInfo;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.global.util.NicknameGenerator;
import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.enums.JoinType;
import com.triptune.member.repository.MemberRepository;
import com.triptune.member.repository.SocialMemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final ProfileImageService profileImageService;
    private final SocialMemberRepository socialMemberRepository;
    private final JwtUtils jwtUtils;
    private final NicknameGenerator nicknameGenerator;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 로그인 시도");

        try {
            // 1. 유저 정보 가져오기
            Map<String, Object> attributes = super.loadUser(userRequest).getAttributes();
            log.debug("소셜 로그인 응답 attribute keys: {}", attributes.keySet());

            // 2. registrationId 가져오기 (third-party id)
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            log.info("OAuth2 제공자: {}", registrationId);

            // 3. 유저 정보 dto 생성
            OAuth2UserInfo oAuth2UserInfo = switch (registrationId){
                case "naver" -> new NaverUserInfo(attributes);
                case "kakao" -> new KaKaoUserInfo(attributes);
                default -> throw new OAuth2Exception(ErrorCode.ILLEGAL_REGISTRATION_ID);
            };

            // 4. 회원가입 및 로그인
            Member member = joinOrLogin(oAuth2UserInfo);
            member.updateRefreshToken(jwtUtils.createRefreshToken(member.getMemberId()));
            return new CustomUserDetails(member, attributes);

        } catch (DataExistException ex){
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(ex.getMessage()), ex.getMessage()
            );
        }

    }

    public Member joinOrLogin(OAuth2UserInfo oAuth2UserInfo){
        // 1. 이미 가입되어 잇는 소셜 회원인지 확인(socialMember 의 id, socialType 체크)
        // 2. 가입되어 있으면 return Member
        log.info("소셜 회원 존재 여부 확인: socialType={}", oAuth2UserInfo.getSocialType());

        return socialMemberRepository.findBySocialIdAndSocialType(oAuth2UserInfo.getSocialId(), oAuth2UserInfo.getSocialType())
                .orElseGet(() -> processSocialLogin(oAuth2UserInfo));
    }

    public Member processSocialLogin(OAuth2UserInfo oAuth2UserInfo){
        // 3. 기존 회원 정보 존재하는지 확인
        // 4. 기존 회원 정보 존재하면 회원통합, 아니면 신규 회원 생성
        return memberRepository.findByEmail(oAuth2UserInfo.getEmail())
                .map(savedMember -> integrateMember(savedMember, oAuth2UserInfo))
                .orElseGet(() -> createMember(oAuth2UserInfo));
    }

    public Member integrateMember(Member savedMember, OAuth2UserInfo oAuth2UserInfo){
        createSocialMember(savedMember, oAuth2UserInfo);
        savedMember.linkSocialAccount();

        log.info("기존 회원과 소셜 계정 통합 완료");
        return savedMember;
    }

    public Member createMember(OAuth2UserInfo oAuth2UserInfo){
        String nickname = nicknameGenerator.createNickname();

        ProfileImage profileImage = profileImageService.saveDefaultProfileImage();
        Member newMember = Member.createSocialMember(
                oAuth2UserInfo.getEmail(),
                nickname,
                profileImage
        );
        memberRepository.save(newMember);

        createSocialMember(newMember, oAuth2UserInfo);

        log.info("신규 회원 생성 등록 완료");

        return newMember;
    }

    public void createSocialMember(Member member, OAuth2UserInfo oAuth2UserInfo){
        SocialMember socialMember = SocialMember.createSocialMember(
                member,
                oAuth2UserInfo.getSocialType(),
                oAuth2UserInfo.getSocialId()
        );

        socialMemberRepository.save(socialMember);
    }

}
