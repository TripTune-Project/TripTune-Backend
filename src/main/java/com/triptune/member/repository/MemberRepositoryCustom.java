package com.triptune.member.repository;

import com.triptune.member.entity.Member;

import java.util.List;
import java.util.Set;

public interface MemberRepositoryCustom {
    List<Member> findByIds(Set<Long> memberIds);
}
