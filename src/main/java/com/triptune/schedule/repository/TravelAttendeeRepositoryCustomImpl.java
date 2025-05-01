package com.triptune.schedule.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.member.entity.QMember;
import com.triptune.schedule.entity.QTravelAttendee;
import com.triptune.schedule.enums.AttendeeRole;
import org.springframework.stereotype.Repository;

@Repository
public class TravelAttendeeRepositoryCustomImpl implements TravelAttendeeRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QMember member;
    private final QTravelAttendee travelAttendee;

    public TravelAttendeeRepositoryCustomImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
        this.member = QMember.member;
        this.travelAttendee = QTravelAttendee.travelAttendee;
    }

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
