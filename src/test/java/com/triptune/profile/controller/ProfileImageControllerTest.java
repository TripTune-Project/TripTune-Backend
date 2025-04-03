package com.triptune.profile.controller;

import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.ProfileImageTest;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.SuccessCode;
import com.triptune.global.service.S3Service;
import com.triptune.global.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
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
    @WithMockUser("member")
    @DisplayName("프로필 이미지 수정")
    void updateProfileImage() throws Exception{
        byte[] content = createTestImage("jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profileImage", "newFileOriginalName.jpg", "image/jpg", content);

        Member member = memberRepository.save(createMember(null, "member"));
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "beforeImage", member));

        member.updateProfileImage(profileImage);

        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/profiles")
                        .file(mockMultipartFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @WithMockUser("member")
    @DisplayName("프로필 이미지 수정 시 이미지 확장자 예외 발생")
    void updateProfileImage_invalidExtensionException() throws Exception{
        byte[] content = createTestImage("gif");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profileImage", "newFileOriginalName.gif", "image/gif", content);

        Member member = memberRepository.save(createMember(null, "member"));
        profileImageRepository.save(createProfileImage(null, "beforeImage", member));

        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/profiles")
                        .file(mockMultipartFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_EXTENSION.getMessage()));

    }

    @Test
    @WithMockUser("member")
    @DisplayName("프로필 이미지 수정 시 이미지 데이터 존재하지 않아 예외 발생")
    void updateProfileImage_profileImageNotFoundException() throws Exception{
        byte[] content = createTestImage("png");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profileImage", "newFileOriginalName.png", "image/png", content);


        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/profiles")
                        .file(mockMultipartFile))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.PROFILE_IMAGE_NOT_FOUND.getMessage()));

    }
}