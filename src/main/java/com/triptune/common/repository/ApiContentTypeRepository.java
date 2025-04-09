package com.triptune.common.repository;

import com.triptune.common.entity.ApiContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiContentTypeRepository extends JpaRepository<ApiContentType, Long> {
}
