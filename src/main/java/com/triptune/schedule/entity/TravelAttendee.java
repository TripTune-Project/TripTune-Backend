package com.triptune.schedule.entity;

import com.triptune.common.entity.BaseTimeEntity;
import com.triptune.member.entity.Member;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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


    private TravelAttendee(Member member, AttendeeRole role, AttendeePermission permission) {
        this.member = member;
        this.role = role;
        this.permission = permission;
    }

    public static TravelAttendee createAuthor(TravelSchedule travelSchedule, Member member){
        TravelAttendee attendee = new TravelAttendee(
                member,
                AttendeeRole.AUTHOR,
                AttendeePermission.ALL
        );

        attendee.assignSchedule(travelSchedule);
        return attendee;
    }

    public static TravelAttendee createGuest(TravelSchedule travelSchedule, Member member, AttendeePermission permission){
        TravelAttendee attendee = new TravelAttendee(
                member,
                AttendeeRole.GUEST,
                permission
        );

        attendee.assignSchedule(travelSchedule);
        return attendee;
    }

    public void assignSchedule(TravelSchedule travelSchedule){
        this.travelSchedule = travelSchedule;
        travelSchedule.addTravelAttendees(this);
    }

    public void updatePermission(AttendeePermission permission){
        this.permission = permission;
    }

    public void updateRole(AttendeeRole role){
        this.role = role;
    }

    public boolean isEnableChat() {
        return permission.isEnableChat();
    }

    public boolean isSameMember(Long memberId) {
        return Objects.equals(this.getMember().getMemberId(), memberId);
    }
}
