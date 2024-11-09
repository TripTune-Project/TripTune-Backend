package com.triptune.domain.schedule.entity;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TravelAttendee {

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

    public boolean isAuthor(){
        return AttendeeRole.AUTHOR.equals(this.role);
    }
}
