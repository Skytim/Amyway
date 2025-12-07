package com.amyway.luckydraw.repository;

import com.amyway.luckydraw.domain.Prize;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Activity a JOIN a.prizes p WHERE a.id = :activityId")
    List<Prize> findByActivityId(@org.springframework.data.repository.query.Param("activityId") Long activityId);

    // Optimistic locking update
    @Modifying
    @Query("UPDATE Prize p SET p.availableStock = p.availableStock - 1 WHERE p.id = :id AND p.availableStock > 0")
    int decrementStock(@Param("id") Long id);
}
