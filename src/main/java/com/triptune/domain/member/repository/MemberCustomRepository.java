package com.triptune.domain.member.repository;

import com.triptune.domain.member.dto.response.MemberProfileResponse;

import java.util.List;
import java.util.Set;

public interface MemberCustomRepository {
    List<MemberProfileResponse> findMembersProfileByMemberId(Set<Long> memberIds);
}
