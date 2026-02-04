package com.triptune.schedule.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.global.util.PageUtils;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.triptune.schedule.entity.QTravelAttendee.travelAttendee;
import static com.triptune.schedule.entity.QTravelSchedule.travelSchedule;

@Repository
@RequiredArgsConstructor
public class TravelScheduleRepositoryCustomImpl implements TravelScheduleRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public Page<TravelSchedule> findTravelSchedules(Pageable pageable, Long memberId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId))
                .orderBy(
                        travelSchedule.updatedAt.desc(),
                        travelSchedule.scheduleId.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countTravelSchedules(memberId);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Page<TravelSchedule> findSharedTravelSchedules(Pageable pageable, Long memberId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(
                        travelAttendee.member.memberId.eq(memberId),
                        travelSchedule.travelAttendees.size().gt(1)
                )
                .orderBy(
                        travelSchedule.updatedAt.desc(),
                        travelSchedule.scheduleId.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countSharedTravelSchedules(memberId);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    @Override
    public Integer countTravelSchedules(Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.scheduleId.countDistinct())
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId))
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Integer countSharedTravelSchedules(Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.scheduleId.countDistinct())
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(
                        travelAttendee.member.memberId.eq(memberId),
                        travelSchedule.travelAttendees.size().gt(1)
                )
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }

    @Override
    public Page<TravelSchedule> searchTravelSchedules(Pageable pageable, String keyword, Long memberId) {
        String orderCaseString = accuracyQuery();

        List<TravelSchedule> content = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(
                        travelAttendee.member.memberId.eq(memberId),
                        travelSchedule.scheduleName.contains(keyword)
                )
                .orderBy(
                        Expressions.stringTemplate(
                                orderCaseString,
                                travelSchedule.scheduleName,
                                keyword,
                                keyword + "%",
                                "%" + keyword + "%",
                                "%" + keyword
                        ).asc(),
                        travelSchedule.scheduleId.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countSearchTravelSchedules(keyword, memberId);

        return PageUtils.createPage(content, pageable, totalElements);
    }

    private Integer countSearchTravelSchedules(String keyword, Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.scheduleId.countDistinct())
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(
                        travelAttendee.member.memberId.eq(memberId),
                        travelSchedule.scheduleName.contains(keyword)
                )
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }



    @Override
    public Page<TravelSchedule> searchSharedTravelSchedules(Pageable pageable, String keyword, Long memberId) {
        String orderCaseString = accuracyQuery();

        List<TravelSchedule> content = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(
                        travelAttendee.member.memberId.eq(memberId),
                        travelSchedule.travelAttendees.size().gt(1),
                        travelSchedule.scheduleName.contains(keyword)
                )
                .orderBy(
                        Expressions.stringTemplate(
                                orderCaseString,
                                travelSchedule.scheduleName,
                                keyword,
                                keyword + "%",
                                "%" + keyword + "%",
                                "%" + keyword
                        ).asc(),
                        travelSchedule.scheduleId.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countSearchSharedTravelSchedules(keyword, memberId);

        return PageUtils.createPage(content, pageable, totalElements);
    }

    private Integer countSearchSharedTravelSchedules(String keyword, Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.scheduleId.countDistinct())
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(
                        travelAttendee.member.memberId.eq(memberId),
                        travelSchedule.travelAttendees.size().gt(1),
                        travelSchedule.scheduleName.contains(keyword)
                )
                .fetchOne();


        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }
    @Override
    public Page<TravelSchedule> findEnableEditTravelSchedules(Pageable pageable, Long memberId) {
        List<TravelSchedule> travelSchedules = jpaQueryFactory
                .selectFrom(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(
                        travelAttendee.member.memberId.eq(memberId),
                        travelAttendee.permission.in(
                                AttendeePermission.ALL,
                                AttendeePermission.EDIT
                        )
                )
                .orderBy(
                        travelSchedule.updatedAt.desc(),
                        travelSchedule.scheduleId.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Integer totalElements = countEnableEditTravelSchedules(memberId);

        return PageUtils.createPage(travelSchedules, pageable, totalElements);
    }

    private Integer countEnableEditTravelSchedules(Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.scheduleId.countDistinct())
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(
                        travelAttendee.member.memberId.eq(memberId),
                        travelAttendee.permission.in(
                                AttendeePermission.ALL,
                                AttendeePermission.EDIT
                        )
                )
                .fetchOne();

        if (totalElements == null) totalElements = 0L;

        return totalElements.intValue();
    }


    private String accuracyQuery(){
        return "CASE " +
                "WHEN {0} = {1} THEN 0 " +
                "WHEN {0} LIKE {2} THEN 1 " +
                "WHEN {0} LIKE {3} THEN 2 " +
                "WHEN {0} LIKE {3} THEN 3 " +
                "ELSE 4 " +
                "END";
    }

}
