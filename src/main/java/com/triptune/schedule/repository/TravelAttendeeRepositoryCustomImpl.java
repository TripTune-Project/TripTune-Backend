package com.triptune.schedule.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.member.entity.QMember;
import com.triptune.schedule.entity.QTravelAttendee;
import com.triptune.schedule.enums.AttendeeRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.triptune.member.entity.QMember.member;
import static com.triptune.schedule.entity.QTravelAttendee.travelAttendee;

@Repository
@RequiredArgsConstructor
public class TravelAttendeeRepositoryCustomImpl implements TravelAttendeeRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public String findAuthorNicknameByScheduleId(Long scheduleId) {
        return jpaQueryFactory.select(member.nickname)
                .from(travelAttendee)
                .join(travelAttendee.member, member)
                .where(travelAttendee.travelSchedule.scheduleId.eq(scheduleId)
                        .and(travelAttendee.role.eq(AttendeeRole.AUTHOR)))
                .fetchOne();

    }
}
