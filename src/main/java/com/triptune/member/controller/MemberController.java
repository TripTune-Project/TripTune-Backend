package com.triptune.member.controller;

import com.triptune.bookmark.enumclass.BookmarkSortType;
import com.triptune.email.dto.EmailRequest;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomNotValidException;
import com.triptune.global.response.ApiResponse;
import com.triptune.global.response.pagination.ApiPageResponse;
import com.triptune.global.util.JwtUtils;
import com.triptune.global.util.SecurityUtils;
import com.triptune.member.dto.request.*;
import com.triptune.member.dto.response.FindIdResponse;
import com.triptune.member.dto.response.LoginResponse;
import com.triptune.member.dto.response.MemberInfoResponse;
import com.triptune.member.dto.response.RefreshTokenResponse;
import com.triptune.member.exception.FailLoginException;
import com.triptune.member.exception.IncorrectPasswordException;
import com.triptune.member.service.MemberService;
import com.triptune.travel.dto.response.PlaceBookmarkResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;
    private final JwtUtils jwtUtils;


    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "회원가입을 요청합니다.")
    public ApiResponse<?> join(@Valid @RequestBody JoinRequest joinRequest){
        if(!joinRequest.getPassword().equals(joinRequest.getRePassword())){
            throw new CustomNotValidException(ErrorCode.INCORRECT_PASSWORD_REPASSWORD);
        }

        memberService.join(joinRequest);

        return ApiResponse.okResponse();
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인을 실행합니다.")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse response = memberService.login(loginRequest);
        return ApiResponse.dataResponse(response);
    }


    @PatchMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃을 실행합니다.")
    public ApiResponse<?> logout(HttpServletRequest request, @Valid @RequestBody LogoutRequest logoutRequest){
        String accessToken = jwtUtils.resolveToken(request);

        memberService.logout(logoutRequest, accessToken);
        return ApiResponse.okResponse();
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token 을 이용해 만료된 Access Token 을 갱신합니다.")
    public ApiResponse<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) throws ExpiredJwtException {
        RefreshTokenResponse response = memberService.refreshToken(refreshTokenRequest);
        return ApiResponse.dataResponse(response);
    }

    @PostMapping("/find-id")
    @Operation(summary = "아이디 찾기", description = "아이디 찾기를 실행합니다.")
    public ApiResponse<FindIdResponse> findId(@Valid @RequestBody FindIdRequest findIdRequest) {
        FindIdResponse response = memberService.findId(findIdRequest);
        return ApiResponse.dataResponse(response);
    }

    @PostMapping("/find-password")
    @Operation(summary = "비밀번호 찾기", description = "비밀번호 찾기를 요청합니다. 비밀번호 변경 화면으로 연결되는 링크가 이메일을 통해서 제공됩니다.")
    public ApiResponse<?> findPassword(@Valid @RequestBody FindPasswordRequest findPasswordRequest) throws MessagingException {
        memberService.findPassword(findPasswordRequest);
        return ApiResponse.okResponse();
    }

    @PatchMapping("/reset-password")
    @Operation(summary = "비밀번호 초기화", description = "이메일 인증 후 비밀번호를 초기화합니다.")
    public ApiResponse<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        if(!resetPasswordRequest.isMatchPassword()){
            throw new FailLoginException(ErrorCode.INCORRECT_PASSWORD_REPASSWORD);
        }

        memberService.resetPassword(resetPasswordRequest);
        return ApiResponse.okResponse();
    }


    @PatchMapping("/change-password")
    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    public ApiResponse<?> changePassword(@Valid @RequestBody ChangePasswordRequest passwordRequest){
        if(!passwordRequest.isMatchNewPassword()){
            throw new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD_REPASSWORD);
        }

        if(passwordRequest.isMatchNowPassword()){
            throw new IncorrectPasswordException(ErrorCode.CORRECT_NOWPASSWORD_NEWPASSWORD);
        }

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        memberService.changePassword(userId, passwordRequest);

        return ApiResponse.okResponse();
    }

    @GetMapping("/info")
    @Operation(summary = "사용자 정보 조회", description = "사용자 정보를 조회합니다.")
    public ApiResponse<MemberInfoResponse> getMemberInfo(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        MemberInfoResponse response = memberService.getMemberInfo(userId);
        return ApiResponse.dataResponse(response);
    }

    @PatchMapping("/change-nickname")
    @Operation(summary = "사용자 닉네임 변경", description = "사용자 닉네임을 변경합니다.")
    public ApiResponse<?> changeNickname(@Valid @RequestBody ChangeNicknameRequest changeNicknameRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        memberService.changeNickname(userId, changeNicknameRequest);
        return ApiResponse.okResponse();

    }

    @PatchMapping("/change-email")
    @Operation(summary = "사용자 이메일 변경", description = "사용자 이메일을 변경합니다.")
    public ApiResponse<?> changeEmail(@Valid @RequestBody EmailRequest emailRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        memberService.changeEmail(userId, emailRequest);
        return ApiResponse.okResponse();
    }

    @GetMapping("/bookmark")
    @Operation(summary = "사용자 북마크 조회", description = "사용자가 등록한 북마크를 조회합니다.")
    public ApiPageResponse<PlaceBookmarkResponse> getMemberBookmarks(@RequestParam(name = "page") int page, @RequestParam(name = "sort") String sort){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        BookmarkSortType sortType = BookmarkSortType.from(sort);
        Page<PlaceBookmarkResponse> response = memberService.getMemberBookmarks(page, userId, sortType);

        return ApiPageResponse.dataResponse(response);
    }

    @PatchMapping("/deactivate")
    @Operation(summary = "회원 탈퇴", description = "회원을 탈퇴합니다.")
    public ApiResponse<Void> deactivateMember(HttpServletRequest request, @Valid @RequestBody DeactivateRequest deactivateRequest){
        String accessToken = jwtUtils.resolveToken(request);
        String userId = SecurityUtils.getCurrentUserId();

        memberService.deactivateMember(accessToken, userId, deactivateRequest);
        return ApiResponse.okResponse();
    }
}
