package com.triptune.schedule.dto.response;

import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
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
    private AttendeeRole role;
    private AttendeePermission permission;

    @Builder
    public AttendeeResponse(Long attendeeId, String nickname, String email, String profileUrl, AttendeeRole role, AttendeePermission permission) {
        this.attendeeId = attendeeId;
        this.nickname = nickname;
        this.email = email;
        this.profileUrl = profileUrl;
        this.role = role;
        this.permission = permission;
    }

    public static AttendeeResponse of(TravelAttendee attendee, String profileUrl){
        return AttendeeResponse.builder()
                .attendeeId(attendee.getAttendeeId())
                .nickname(attendee.getMember().getNickname())
                .email(attendee.getMember().getEmail())
                .profileUrl(profileUrl)
                .role(attendee.getRole())
                .permission(attendee.getPermission())
                .build();
    }
}
