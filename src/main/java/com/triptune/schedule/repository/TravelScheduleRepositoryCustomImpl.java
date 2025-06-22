package com.triptune.schedule.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.global.util.PageUtils;
import com.triptune.schedule.entity.QTravelAttendee;
import com.triptune.schedule.entity.QTravelSchedule;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TravelScheduleRepositoryCustomImpl implements TravelScheduleRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QTravelSchedule travelSchedule;
    private final QTravelAttendee travelAttendee;


    public TravelScheduleRepositoryCustomImpl(JPAQueryFactory jpaRepository){
        this.jpaQueryFactory = jpaRepository;
        this.travelSchedule = QTravelSchedule.travelSchedule;
        this.travelAttendee = QTravelAttendee.travelAttendee;
    }

    @Override
    public Page<TravelSchedule> findTravelSchedulesByMemberId(Pageable pageable, Long memberId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId))
                .orderBy(orderByTravelScheduleDateDESC())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countTravelSchedulesByMemberId(memberId);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Page<TravelSchedule> findSharedTravelSchedulesByMemberId(Pageable pageable, Long memberId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId)
                        .and(travelSchedule.travelAttendees.size().gt(1)))
                .orderBy(orderByTravelScheduleDateDESC())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countSharedTravelSchedulesByMemberId(memberId);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Integer countTravelSchedulesByMemberId(Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId))
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Integer countSharedTravelSchedulesByMemberId(Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId)
                        .and(travelSchedule.travelAttendees.size().gt(1)))
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Page<TravelSchedule> searchTravelSchedulesByMemberIdAndKeyword(Pageable pageable, String keyword, Long memberId) {
        String orderCaseString = accuracyQuery();

        List<TravelSchedule> content = jpaQueryFactory
                .selectFrom(travelSchedule)
                .leftJoin(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId)
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .orderBy(Expressions.stringTemplate(
                        orderCaseString,
                        travelSchedule.scheduleName, keyword,  keyword + "%", "%" + keyword + "%", "%" + keyword
                        ).asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countTravelSchedulesByMemberIdAndKeyword(keyword, memberId);

        return PageUtils.createPage(content, pageable, totalElements);
    }

    @Override
    public Integer countTravelSchedulesByMemberIdAndKeyword(String keyword, Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId)
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Page<TravelSchedule> searchSharedTravelSchedulesByMemberIdAndKeyword(Pageable pageable, String keyword, Long memberId) {
        String orderCaseString = accuracyQuery();

        List<TravelSchedule> content = jpaQueryFactory
                .selectFrom(travelSchedule)
                .leftJoin(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId)
                        .and(travelSchedule.travelAttendees.size().gt(1))
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .orderBy(Expressions.stringTemplate(
                                orderCaseString,
                                travelSchedule.scheduleName, keyword,  keyword + "%", "%" + keyword + "%", "%" + keyword
                        ).asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countSharedTravelSchedulesByMemberIdAndKeyword(keyword, memberId);

        return PageUtils.createPage(content, pageable, totalElements);
    }

    @Override
    public Integer countSharedTravelSchedulesByMemberIdAndKeyword(String keyword, Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId)
                        .and(travelSchedule.travelAttendees.size().gt(1))
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Page<TravelSchedule> findEnableEditTravelSchedulesByMemberId(Pageable pageable, Long memberId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .leftJoin(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId)
                        .and(travelAttendee.permission.eq(AttendeePermission.ALL)
                                .or(travelAttendee.permission.eq(AttendeePermission.EDIT))))
                .orderBy(orderByTravelScheduleDateDESC())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countEnableEditTravelSchedulesByMemberId(memberId);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Integer countEnableEditTravelSchedulesByMemberId(Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId)
                        .and(travelAttendee.permission.eq(AttendeePermission.ALL)
                                .or(travelAttendee.permission.eq(AttendeePermission.EDIT))))
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }


    private String accuracyQuery(){
        return "CASE WHEN {0} = {1} THEN 0 " +
                "WHEN {0} = {2} THEN 1 " +
                "WHEN {0} = {3} THEN 2 " +
                "WHEN {0} = {3} THEN 3 " +
                "ELSE 4 " +
                "END";
    }

    private OrderSpecifier<LocalDateTime> orderByTravelScheduleDateDESC(){
        return new CaseBuilder()
                .when(travelSchedule.updatedAt.isNull())
                .then(travelSchedule.createdAt)
                .otherwise(travelSchedule.updatedAt)
                .desc();
    }
}
