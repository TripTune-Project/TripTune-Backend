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
                .orderBy(orderByTravelScheduleDateDESC())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countTravelSchedulesByUserId(userId);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Page<TravelSchedule> findSharedTravelSchedulesByUserId(Pageable pageable, String userId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId)
                        .and(travelSchedule.travelAttendeeList.size().gt(1)))
                .orderBy(orderByTravelScheduleDateDESC())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countSharedTravelSchedulesByUserId(userId);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
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

    @Override
    public Page<TravelSchedule> searchTravelSchedulesByUserIdAndKeyword(Pageable pageable, String keyword, String userId) {
        String orderCaseString = accuracyQuery();

        List<TravelSchedule> content = jpaQueryFactory
                .selectFrom(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId)
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .orderBy(Expressions.stringTemplate(
                        orderCaseString,
                        travelSchedule.scheduleName, keyword,  keyword + "%", "%" + keyword + "%", "%" + keyword
                        ).asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countTravelSchedulesByUserIdAndKeyword(keyword, userId);

        return PageUtils.createPage(content, pageable, totalElements);
    }

    @Override
    public Integer countTravelSchedulesByUserIdAndKeyword(String keyword, String userId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId)
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Page<TravelSchedule> searchSharedTravelSchedulesByUserIdAndKeyword(Pageable pageable, String keyword, String userId) {
        String orderCaseString = accuracyQuery();

        List<TravelSchedule> content = jpaQueryFactory
                .selectFrom(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId)
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

        int totalElements = countSharedTravelSchedulesByUserIdAndKeyword(keyword, userId);

        return PageUtils.createPage(content, pageable, totalElements);
    }

    @Override
    public Integer countSharedTravelSchedulesByUserIdAndKeyword(String keyword, String userId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId)
                        .and(travelSchedule.travelAttendeeList.size().gt(1))
                        .and(travelSchedule.scheduleName.contains(keyword)))
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Page<TravelSchedule> findEnableEditTravelSchedulesByUserId(Pageable pageable, String userId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId)
                        .and(travelAttendee.permission.eq(AttendeePermission.ALL)
                                .or(travelAttendee.permission.eq(AttendeePermission.EDIT))))
                .orderBy(orderByTravelScheduleDateDESC())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countEnableEditTravelSchedulesByUserId(userId);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Integer countEnableEditTravelSchedulesByUserId(String userId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.count())
                .from(travelSchedule)
                .leftJoin(travelSchedule.travelAttendeeList, travelAttendee)
                .where(travelAttendee.member.userId.eq(userId)
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
