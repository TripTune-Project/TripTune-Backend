package com.triptune.profile.controller;

import com.triptune.profile.service.ProfileImageService;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile Image", description = "프로필 이미지 관련 API")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @PatchMapping
    @Operation(summary = "프로필 이미지 변경", description = "프로필 이미지를 변경합니다.")
    public ApiResponse<?> updateProfileImage(@RequestParam(name = "profileImage") MultipartFile profileImageFile){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        profileImageService.updateProfileImage(userId, profileImageFile);
        return ApiResponse.okResponse();
    }
}
