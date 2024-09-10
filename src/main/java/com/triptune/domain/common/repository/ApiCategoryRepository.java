package com.triptune.domain.common.repository;

import com.triptune.domain.common.entity.ApiCategory;
import com.triptune.domain.common.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiCategoryRepository extends JpaRepository<ApiCategory, Long> {
}
