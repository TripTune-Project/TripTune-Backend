package com.triptune.profile.controller;

import com.triptune.global.security.SecurityTestUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.s3.S3Service;
import com.triptune.global.security.jwt.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class ProfileImageControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;

    @MockBean private S3Service s3Service;


    @Test
    @DisplayName("프로필 이미지 수정")
    void updateProfileImage() throws Exception{
        // given
        byte[] content = ProfileImageFixture.createByteTypeImage("jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profileImage", "newFileOriginalName.jpg", "image/jpg", content);

        ProfileImage profileImage = profileImageRepository.save(ProfileImageFixture.createProfileImage("memberImage"));
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));

        SecurityTestUtils.mockAuthentication(member);

        // when, then
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/profiles")
                        .file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("프로필 이미지 수정 시 이미지 확장자 예외 발생")
    void updateProfileImage_invalidExtension() throws Exception{
        // given
        byte[] content = ProfileImageFixture.createByteTypeImage("gif");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profileImage", "newFileOriginalName.gif", "image/gif", content);

        ProfileImage profileImage = profileImageRepository.save(ProfileImageFixture.createProfileImage("memberImage"));
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember( "member@email.com", profileImage));

        SecurityTestUtils.mockAuthentication(member);

        // when, then
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/profiles")
                        .file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_EXTENSION.getMessage()));

    }


}