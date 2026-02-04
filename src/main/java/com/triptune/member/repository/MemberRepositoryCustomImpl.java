package com.triptune.member.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.member.dto.response.MemberProfileResponse;
import com.triptune.member.entity.QMember;
import com.triptune.profile.entity.QProfileImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static com.triptune.member.entity.QMember.member;
import static com.triptune.profile.entity.QProfileImage.profileImage;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public List<MemberProfileResponse> findMembersProfileByMemberId(Set<Long> memberIds) {
        return jpaQueryFactory
                .select(Projections.constructor(MemberProfileResponse.class,
                                member.memberId,
                                member.nickname,
                                member.profileImage.s3ObjectUrl))
                .from(member)
                .leftJoin(member.profileImage, profileImage)
                .where(member.memberId.in(memberIds))
                .fetch();
    }

}
