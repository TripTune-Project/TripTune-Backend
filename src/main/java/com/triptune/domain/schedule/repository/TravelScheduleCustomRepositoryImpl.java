package com.triptune.domain.schedule.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.domain.schedule.entity.QTravelAttendee;
import com.triptune.domain.schedule.entity.QTravelSchedule;
import com.triptune.domain.schedule.entity.TravelSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    public Page<TravelSchedule> findTravelSchedulesByAttendee(Pageable pageable, Long memberId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId))
                .orderBy(travelSchedule.updatedAt.desc(), travelSchedule.createdAt.desc())
                .fetch();


        Integer totalElements = getTotalElementByTravelSchedules(memberId);

        return new PageImpl<>(travelSchedules, pageable, totalElements);
    }

    @Override
    public Integer getTotalElementByTravelSchedules(Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId))
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }
}
