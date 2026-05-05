package com.triptune.schedule.repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.global.util.PageUtils;
import com.triptune.schedule.entity.QTravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.repository.dto.ScheduleInfoQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.triptune.schedule.entity.QTravelAttendee.travelAttendee;
import static com.triptune.schedule.entity.QTravelRoute.travelRoute;
import static com.triptune.schedule.entity.QTravelSchedule.travelSchedule;
import static com.triptune.travel.entity.QTravelImage.travelImage;

@Repository
@RequiredArgsConstructor
public class TravelScheduleRepositoryCustomImpl implements TravelScheduleRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ScheduleInfoQueryDto> findTravelSchedules(Pageable pageable, Long memberId) {
        QTravelAttendee authorAttendee = new QTravelAttendee("authorAttendee");

        List<ScheduleInfoQueryDto> travelSchedules = jpaQueryFactory
                .select(selectScheduleInfo(authorAttendee))
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .on(travelAttendee.member.memberId.eq(memberId))

                .join(travelSchedule.travelAttendees, authorAttendee)
                .on(authorAttendee.role.eq(AttendeeRole.AUTHOR))

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
    public Integer countTravelSchedules(Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.scheduleId.countDistinct())
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(travelAttendee.member.memberId.eq(memberId))
                .fetchOne();

        return totalElements == null ? 0 : totalElements.intValue();
    }



    @Override
    public Page<ScheduleInfoQueryDto> findSharedTravelSchedules(Pageable pageable, Long memberId) {
        QTravelAttendee authorAttendee = new QTravelAttendee("authorAttendee");

        List<ScheduleInfoQueryDto> travelSchedules = jpaQueryFactory
                .select(selectScheduleInfo(authorAttendee))
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .on(travelAttendee.member.memberId.eq(memberId))

                .join(travelSchedule.travelAttendees, authorAttendee)
                .on(authorAttendee.role.eq(AttendeeRole.AUTHOR))
                .where(
                        travelSchedule.travelAttendees.size().goe(2)
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
    public Integer countSharedTravelSchedules(Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.scheduleId.countDistinct())
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(
                        travelAttendee.member.memberId.eq(memberId),
                        travelSchedule.travelAttendees.size().goe(2)
                )
                .fetchOne();

        return totalElements == null ? 0 : totalElements.intValue();
    }

    @Override
    public Page<ScheduleInfoQueryDto> searchTravelSchedules(Pageable pageable, String keyword, Long memberId) {
        QTravelAttendee authorAttendee = new QTravelAttendee("authorAttendee");
        String orderCaseString = accuracyQuery();

        List<ScheduleInfoQueryDto> content = jpaQueryFactory
                .select(selectScheduleInfo(authorAttendee))
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .on(travelAttendee.member.memberId.eq(memberId))

                .join(travelSchedule.travelAttendees, authorAttendee)
                .on(authorAttendee.role.eq(AttendeeRole.AUTHOR))

                .where(travelSchedule.scheduleName.contains(keyword))
                .orderBy(
                        accuracyOrder(keyword),
                        travelSchedule.updatedAt.desc()
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

        return totalElements == null ? 0 : totalElements.intValue();

    }


    @Override
    public Page<ScheduleInfoQueryDto> searchSharedTravelSchedules(Pageable pageable, String keyword, Long memberId) {
        QTravelAttendee authorAttendee = new QTravelAttendee("authorAttendee");
        String orderCaseString = accuracyQuery();

        List<ScheduleInfoQueryDto> content = jpaQueryFactory
                .select(selectScheduleInfo(authorAttendee))
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .on(travelAttendee.member.memberId.eq(memberId))

                .join(travelSchedule.travelAttendees, authorAttendee)
                .on(authorAttendee.role.eq(AttendeeRole.AUTHOR))

                .where(
                        travelSchedule.travelAttendees.size().goe(2),
                        travelSchedule.scheduleName.contains(keyword)
                )
                .orderBy(
                        accuracyOrder(keyword),
                        travelSchedule.updatedAt.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        int totalElements = countSearchSharedTravelSchedules(keyword, memberId);

        return PageUtils.createPage(content, pageable, totalElements);
    }


    private OrderSpecifier<String> accuracyOrder(String keyword){
        return Expressions.stringTemplate(
                accuracyQuery(),
                travelSchedule.scheduleName,
                keyword,
                keyword + "%",
                "%" + keyword + "%",
                "%" + keyword
        ).asc();
    }


    private String accuracyQuery(){
        return "CASE " +
                "WHEN {0} = {1} THEN 0 " +
                "WHEN {0} LIKE {2} THEN 1 " +
                "WHEN {0} LIKE {3} THEN 2 " +
                "WHEN {0} LIKE {4} THEN 3 " +
                "ELSE 4 " +
                "END";
    }


    private Integer countSearchSharedTravelSchedules(String keyword, Long memberId) {
        Long totalElements = jpaQueryFactory
                .select(travelSchedule.scheduleId.countDistinct())
                .from(travelSchedule)
                .join(travelSchedule.travelAttendees, travelAttendee)
                .where(
                        travelAttendee.member.memberId.eq(memberId),
                        travelSchedule.travelAttendees.size().goe(2),
                        travelSchedule.scheduleName.contains(keyword)
                )
                .fetchOne();

        return totalElements == null ? 0 : totalElements.intValue();
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

        return totalElements == null ? 0 : totalElements.intValue();
    }

    private ConstructorExpression<ScheduleInfoQueryDto> selectScheduleInfo(QTravelAttendee authorAttendee){
        return Projections.constructor(ScheduleInfoQueryDto.class,
                        travelSchedule.scheduleId,
                        travelAttendee.role,
                        travelSchedule.scheduleName,
                        travelSchedule.startDate,
                        travelSchedule.endDate,
                        travelSchedule.createdAt,
                        travelSchedule.updatedAt,
                        findThumbnailS3ObjectKey(),
                        authorAttendee.member.nickname,
                        authorAttendee.member.profileImage.s3ObjectKey);
    }

    private JPQLQuery<String> findThumbnailS3ObjectKey(){
        return JPAExpressions
                .select(travelImage.s3ObjectKey)
                .from(travelRoute)
                .leftJoin(travelRoute.travelPlace.travelImages, travelImage)
                .on(travelImage.isThumbnail.isTrue())
                .where(
                        travelRoute.travelSchedule.scheduleId.eq(travelSchedule.scheduleId),
                        travelRoute.routeOrder.eq(1)
                );
    }

}
