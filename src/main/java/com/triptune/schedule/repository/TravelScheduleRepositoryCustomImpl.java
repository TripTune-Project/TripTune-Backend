package com.triptune.schedule.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.global.util.PageUtils;
import com.triptune.schedule.entity.QTravelAttendee;
import com.triptune.schedule.entity.QTravelSchedule;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeePermission;
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
    public Page<TravelSchedule> findTravelSchedulesByEmail(Pageable pageable, String email) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.email.eq(email))
                .orderBy(orderByTravelScheduleDateDESC())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countTravelSchedulesByEmail(email);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Page<TravelSchedule> findSharedTravelSchedulesByEmail(Pageable pageable, String email) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.email.eq(email)
                        .and(travelSchedule.travelAttendeeList.size().gt(1)))
                .orderBy(orderByTravelScheduleDateDESC())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countSharedTravelSchedulesByEmail(email);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Integer countTravelSchedulesByEmail(String email) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.email.eq(email))
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Integer countSharedTravelSchedulesByEmail(String email) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.email.eq(email)
                        .and(travelSchedule.travelAttendeeList.size().gt(1)))
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Page<TravelSchedule> searchTravelSchedulesByEmailAndKeyword(Pageable pageable, String keyword, String email) {
        String orderCaseString = accuracyQuery();

        List<TravelSchedule> content = jpaQueryFactory
                .selectFrom(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.email.eq(email)
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .orderBy(Expressions.stringTemplate(
                        orderCaseString,
                        travelSchedule.scheduleName, keyword,  keyword + "%", "%" + keyword + "%", "%" + keyword
                        ).asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countTravelSchedulesByEmailAndKeyword(keyword, email);

        return PageUtils.createPage(content, pageable, totalElements);
    }

    @Override
    public Integer countTravelSchedulesByEmailAndKeyword(String keyword, String email) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.email.eq(email)
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Page<TravelSchedule> searchSharedTravelSchedulesByEmailAndKeyword(Pageable pageable, String keyword, String email) {
        String orderCaseString = accuracyQuery();

        List<TravelSchedule> content = jpaQueryFactory
                .selectFrom(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.email.eq(email)
                        .and(travelSchedule.travelAttendeeList.size().gt(1))
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .orderBy(Expressions.stringTemplate(
                                orderCaseString,
                                travelSchedule.scheduleName, keyword,  keyword + "%", "%" + keyword + "%", "%" + keyword
                        ).asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countSharedTravelSchedulesByEmailAndKeyword(keyword, email);

        return PageUtils.createPage(content, pageable, totalElements);
    }

    @Override
    public Integer countSharedTravelSchedulesByEmailAndKeyword(String keyword, String email) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.email.eq(email)
                        .and(travelSchedule.travelAttendeeList.size().gt(1))
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Page<TravelSchedule> findEnableEditTravelSchedulesByEmail(Pageable pageable, String email) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.email.eq(email)
                        .and(travelAttendee.permission.eq(AttendeePermission.ALL)
                                .or(travelAttendee.permission.eq(AttendeePermission.EDIT))))
                .orderBy(orderByTravelScheduleDateDESC())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countEnableEditTravelSchedulesByEmail(email);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Integer countEnableEditTravelSchedulesByEmail(String email) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.email.eq(email)
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
