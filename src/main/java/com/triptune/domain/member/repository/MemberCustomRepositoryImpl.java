package com.triptune.domain.member.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.triptune.domain.member.dto.response.MemberProfileResponse;
import com.triptune.domain.member.entity.QMember;
import com.triptune.domain.profile.entity.QProfileImage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class MemberCustomRepositoryImpl implements MemberCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final QMember member;
    private final QProfileImage profileImage;

    public MemberCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
        this.member = QMember.member;
        this.profileImage = QProfileImage.profileImage;
    }

    @Override
    public List<MemberProfileResponse> findMembersProfileByMemberId(Set<Long> memberIds) {
        return jpaQueryFactory
                .select(Projections.fields(MemberProfileResponse.class,
                                member.memberId,
                                member.nickname,
                                member.profileImage.s3ObjectUrl))
                .from(member)
                .leftJoin(member.profileImage, profileImage)
                .where(member.memberId.in(memberIds))
                .fetch();
    }

}
