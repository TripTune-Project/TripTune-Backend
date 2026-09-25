package com.triptune.schedule.fixture;

import com.triptune.member.entity.Member;
import com.triptune.schedule.dto.request.AttendeePermissionRequest;
import com.triptune.schedule.dto.request.AttendeeRequest;
import com.triptune.schedule.dto.response.AttendeeResponse;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import org.springframework.test.util.ReflectionTestUtils;

public class TravelAttendeeFixture {

    public static TravelAttendee createAuthorAttendee(TravelSchedule schedule, Member member){
        return TravelAttendee.createAuthor(schedule, member);
    }

    public static TravelAttendee createAuthorAttendeeWithId(Long attendeeId, TravelSchedule schedule, Member member){
        TravelAttendee attendee = TravelAttendee.createAuthor(schedule, member);
        ReflectionTestUtils.setField(attendee, "attendeeId", attendeeId);
        return attendee;
    }

    public static TravelAttendee createGuestAttendee(TravelSchedule schedule, Member member, AttendeePermission permission){
        return TravelAttendee.createGuest(schedule, member, permission);
    }

    public static TravelAttendee createGuestAttendeeWithId(Long attendeeId, TravelSchedule schedule, Member member, AttendeePermission permission){
        TravelAttendee attendee = TravelAttendee.createGuest(schedule, member, permission);
        ReflectionTestUtils.setField(attendee, "attendeeId", attendeeId);
        return attendee;
    }


    public static AttendeeRequest createAttendeeRequest(String email, AttendeePermission permission){
        return AttendeeRequest.builder()
                .email(email)
                .permission(permission)
                .build();
    }


    public static AttendeePermissionRequest createAttendeePermissionRequest(AttendeePermission permission) {
        return AttendeePermissionRequest.builder().permission(permission).build();
    }

    public static AttendeeResponse createAttendeeResponse(TravelAttendee attendee, String profileUrl) {
        return AttendeeResponse.of(attendee, profileUrl);
    }
}
