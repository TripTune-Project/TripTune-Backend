package com.triptune.global.enumclass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다. 로그인 후 다시 시도하세요."),
    PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "페이지를 찾을 수 없습니다."),

    // 입력 검증
    INCORRECT_PASSWORD_REPASSWORD(HttpStatus.BAD_REQUEST, "비밀번호와 재입력 비밀번호가 일치하지 않습니다."),
    ALREADY_EXISTED_USERID(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    ALREADY_EXISTED_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    ALREADY_EXISTED_EMAIL(HttpStatus.CONFLICT, "이미 가입되어 있는 이메일입니다."),

    // 사용자
    FAILED_LOGIN(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."),
    INVALID_CHANGE_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 요청입니다. 비밀번호 찾기를 다시 진행해주세요."),


    // 이메일
    EMAIL_VERIFY_FAIL(HttpStatus.BAD_REQUEST, "이메일 인증에 실패했습니다."),

    // 토큰
    INVALID_JWT_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 JWT 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.BAD_REQUEST, "지원되지 않는 JWT 토큰입니다."),
    EMPTY_JWT_CLAIMS(HttpStatus.BAD_REQUEST, "JWT 클레임이 비었습니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT 토큰이 만료되었습니다."),
    MISMATCH_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 JWT 토큰입니다. 로그인 후 이용해주세요."),
    BLACKLIST_TOKEN(HttpStatus.UNAUTHORIZED, "로그아웃 된 사용자입니다. 로그인 후 이용해주세요."),

    // 여행지
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "여행지 데이터가 존재하지 않습니다."),

    // 일정
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "일정 데이터가 존재하지 않습니다."),
    AUTHOR_NOT_FOUND(HttpStatus.NOT_FOUND, "작성자 정보를 찾을 수 없습니다."),
    FORBIDDEN_ACCESS_SCHEDULE(HttpStatus.FORBIDDEN, "해당 일정에 접근 권한이 없는 사용자 입니다."),
    FORBIDDEN_EDIT_SCHEDULE(HttpStatus.FORBIDDEN, "해당 일정에 편집 권한이 없는 사용자 입니다."),
    FORBIDDEN_DELETE_SCHEDULE(HttpStatus.FORBIDDEN, "해당 일정에 삭제 권한이 없는 사용자 입니다."),

    // 일정 참석
    FORBIDDEN_REMOVE_ATTENDEE(HttpStatus.FORBIDDEN, "작성자는 일정에서 나갈 수 없습니다."),
    ALREADY_ATTENDEE(HttpStatus.CONFLICT, "이미 공유되어 있는 사용자입니다."),
    FORBIDDEN_SHARE_ATTENDEE(HttpStatus.FORBIDDEN, "해당 일정에 공유 권한이 없는 사용자 입니다."),
    OVER_ATTENDEE_NUMBER(HttpStatus.CONFLICT, "일정은 최대 5명까지 공유할 수 있습니다."),

    // 채팅
    FORBIDDEN_CHAT_ATTENDEE(HttpStatus.FORBIDDEN, "채팅 권한이 없는 사용자 입니다.");


    private final HttpStatus status;
    private final String message;
}
