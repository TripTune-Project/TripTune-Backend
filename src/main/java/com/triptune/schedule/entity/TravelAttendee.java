package com.triptune.schedule.entity;

import com.triptune.common.entity.BaseTimeEntity;
import com.triptune.member.entity.Member;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class TravelAttendee extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendee_id")
    private Long attendeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private TravelSchedule travelSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private AttendeeRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private AttendeePermission permission;

    @Builder
    public TravelAttendee(Long attendeeId, TravelSchedule travelSchedule, Member member, AttendeeRole role, AttendeePermission permission) {
        this.attendeeId = attendeeId;
        this.travelSchedule = travelSchedule;
        this.member = member;
        this.role = role;
        this.permission = permission;
    }

    public static TravelAttendee of(TravelSchedule schedule, Member member){
        return TravelAttendee.builder()
                .travelSchedule(schedule)
                .member(member)
                .role(AttendeeRole.AUTHOR)
                .permission(AttendeePermission.ALL)
                .build();
    }

    public static TravelAttendee of(TravelSchedule schedule, Member member, AttendeePermission permission){
        return TravelAttendee.builder()
                .travelSchedule(schedule)
                .member(member)
                .role(AttendeeRole.GUEST)
                .permission(permission)
                .build();
    }

    public void updatePermission(AttendeePermission permission){
        this.permission = permission;
    }

    public void updateRole(AttendeeRole role){
        this.role = role;
    }

    public void setTravelSchedule(TravelSchedule travelSchedule) {
        this.travelSchedule = travelSchedule;
    }

}
