package com.triptune.schedule.dto.response;

import com.triptune.schedule.entity.TravelAttendee;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendeeResponse {
    private Long attendeeId;
    private String nickname;
    private String email;
    private String profileUrl;
    private String role;
    private String permission;

    @Builder
    public AttendeeResponse(Long attendeeId, String nickname, String email, String profileUrl, String role, String permission) {
        this.attendeeId = attendeeId;
        this.nickname = nickname;
        this.email = email;
        this.profileUrl = profileUrl;
        this.role = role;
        this.permission = permission;
    }

    public static AttendeeResponse from(TravelAttendee attendee){
        return AttendeeResponse.builder()
                .attendeeId(attendee.getAttendeeId())
                .nickname(attendee.getMember().getNickname())
                .email(attendee.getMember().getEmail())
                .profileUrl(attendee.getMember().getProfileImage().getS3ObjectUrl())
                .role(attendee.getRole().name())
                .permission(attendee.getPermission().name())
                .build();


    }
}
