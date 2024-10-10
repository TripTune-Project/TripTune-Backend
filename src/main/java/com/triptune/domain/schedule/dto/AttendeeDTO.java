package com.triptune.domain.schedule.dto;

import com.triptune.domain.schedule.entity.TravelAttendee;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendeeDTO {
    private Long attendeeId;
    private String userId;
    private String role;
    private String permission;

    @Builder
    public AttendeeDTO(Long attendeeId, String userId, String role, String permission) {
        this.attendeeId = attendeeId;
        this.userId = userId;
        this.role = role;
        this.permission = permission;
    }

    public static AttendeeDTO entityToDTO(TravelAttendee attendee){
        return AttendeeDTO.builder()
                .attendeeId(attendee.getAttendeeId())
                .userId(attendee.getMember().getUserId())
                .role(attendee.getRole().name())
                .permission(attendee.getPermission().name())
                .build();


    }
}
