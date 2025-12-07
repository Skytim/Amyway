package com.amyway.luckydraw.repository;

import com.amyway.luckydraw.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByPrizesId(Long prizeId);
}
