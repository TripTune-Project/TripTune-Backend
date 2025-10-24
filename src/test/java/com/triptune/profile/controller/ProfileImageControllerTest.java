package com.triptune.profile.controller;

import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.ProfileImageTest;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.response.enums.SuccessCode;
import com.triptune.global.s3.S3Service;
import com.triptune.global.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("h2")
class ProfileImageControllerTest extends ProfileImageTest {

    @Autowired private WebApplicationContext wac;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;

    @MockBean private S3Service s3Service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("프로필 이미지 수정")
    void updateProfileImage() throws Exception{
        // given
        byte[] content = createTestImage("jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profileImage", "newFileOriginalName.jpg", "image/jpg", content);

        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "beforeImage", member));

        member.updateProfileImage(profileImage);
        mockAuthentication(member);

        // when, then
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/profiles")
                        .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("프로필 이미지 수정 시 이미지 확장자 예외 발생")
    void updateProfileImage_invalidExtension() throws Exception{
        // given
        byte[] content = createTestImage("gif");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profileImage", "newFileOriginalName.gif", "image/gif", content);

        Member member = memberRepository.save(createMember(null, "member@email.com"));
        profileImageRepository.save(createProfileImage(null, "beforeImage", member));

        mockAuthentication(member);

        // when, then
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/profiles")
                        .file(mockMultipartFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_EXTENSION.getMessage()));

    }

    @Test
    @DisplayName("프로필 이미지 수정 시 이미지 데이터 존재하지 않아 예외 발생")
    void updateProfileImage_profileImageNotFound() throws Exception{
        // given
        byte[] content = createTestImage("png");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profileImage", "newFileOriginalName.png", "image/png", content);

        Member member = memberRepository.save(createMember(null, "member@email.com"));
        mockAuthentication(member);

        // when, then
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/profiles")
                        .file(mockMultipartFile))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PROFILE_IMAGE_NOT_FOUND.getMessage()));

    }
}