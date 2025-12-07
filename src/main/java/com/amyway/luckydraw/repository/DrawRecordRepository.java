package com.amyway.luckydraw.repository;

import com.amyway.luckydraw.domain.DrawRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DrawRecordRepository extends JpaRepository<DrawRecord, Long>, JpaSpecificationExecutor<DrawRecord> {
    long countByUser_IdAndActivityId(Long userId, Long activityId);

    long countByUser_Id(Long userId);
}
