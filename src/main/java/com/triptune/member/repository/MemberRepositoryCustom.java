package com.triptune.member.repository;

import com.triptune.member.dto.response.MemberProfileResponse;

import java.util.List;
import java.util.Set;

public interface MemberRepositoryCustom {
    List<MemberProfileResponse> findMembersProfileByMemberId(Set<Long> memberIds);
}
