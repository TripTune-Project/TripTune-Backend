package com.triptune.profile.controller;

import com.triptune.global.exception.FileBadRequestException;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.global.s3.S3Service;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.service.ProfileImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileImageControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private ProfileImageService profileImageService;

    @BeforeEach
    void setUp(){
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member@email.com",
                profileImage
        );

        SecurityTestUtils.mockAuthentication(member);
    }

    @Test
    @DisplayName("프로필 이미지 수정")
    void updateProfileImage() throws Exception{
        // given
        byte[] content = ProfileImageFixture.createByteTypeImage("jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "profileImage",
                "newFileOriginalName.jpg",
                "image/jpg",
                content
        );

        // when
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/profiles")
                        .file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        verify(profileImageService).updateProfileImage(eq(1L), any(MultipartFile.class));
    }

    @Test
    @DisplayName("프로필 이미지 수정 시 파일 누락으로 400 반환")
    void updateProfileImage_missingFile() throws Exception {
        // when, then
        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/profiles"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(profileImageService, never()).updateProfileImage(any(), any());
    }

    @Test
    @DisplayName("프로필 이미지 수정 시 이미지 확장자 예외로 400 반환")
    void updateProfileImage_invalidExtension() throws Exception{
        // given
        byte[] content = ProfileImageFixture.createByteTypeImage("gif");
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "profileImage",
                "newFileOriginalName.gif",
                "image/gif",
                content
        );

        willThrow(new FileBadRequestException(ErrorCode.INVALID_EXTENSION))
                .given(profileImageService).updateProfileImage(eq(1L), any(MultipartFile.class));

        // when
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/profiles")
                        .file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_EXTENSION.getMessage()));

        // then
        verify(profileImageService).updateProfileImage(eq(1L), any(MultipartFile.class));

    }


}