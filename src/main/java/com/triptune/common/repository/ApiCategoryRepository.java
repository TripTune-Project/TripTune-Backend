package com.triptune.common.repository;

import com.triptune.common.entity.ApiCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiCategoryRepository extends JpaRepository<ApiCategory, Long> {
}
