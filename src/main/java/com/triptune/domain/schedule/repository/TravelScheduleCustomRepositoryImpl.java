package com.triptune.domain.schedule.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.domain.schedule.entity.QTravelAttendee;
import com.triptune.domain.schedule.entity.QTravelSchedule;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.global.util.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TravelScheduleCustomRepositoryImpl implements TravelScheduleCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;
    private final QTravelSchedule travelSchedule;
    private final QTravelAttendee travelAttendee;


    public TravelScheduleCustomRepositoryImpl(JPAQueryFactory jpaRepository){
        this.jpaQueryFactory = jpaRepository;
        this.travelSchedule = QTravelSchedule.travelSchedule;
        this.travelAttendee = QTravelAttendee.travelAttendee;
    }

    @Override
    public Page<TravelSchedule> findTravelSchedulesByUserId(Pageable pageable, String userId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId))
                .orderBy(travelSchedule.updatedAt.desc(), travelSchedule.createdAt.desc())
                .fetch();


        Integer totalElements = countTravelSchedulesByUserId(userId);

        return PageUtil.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Page<TravelSchedule> findSharedTravelSchedulesByUserId(Pageable pageable, String userId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId)
                        .and(travelSchedule.travelAttendeeList.size().gt(1)))
                .orderBy(travelSchedule.updatedAt.desc(), travelSchedule.createdAt.desc())
                .fetch();


        Integer totalElements = countSharedTravelSchedulesByUserId(userId);

        return PageUtil.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Integer countTravelSchedulesByUserId(String userId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId))
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Integer countSharedTravelSchedulesByUserId(String userId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId)
                        .and(travelSchedule.travelAttendeeList.size().gt(1)))
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }
}
