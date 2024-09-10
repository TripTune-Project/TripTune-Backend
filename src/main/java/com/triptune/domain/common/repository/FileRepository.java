package com.triptune.domain.common.repository;

import com.triptune.domain.common.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
