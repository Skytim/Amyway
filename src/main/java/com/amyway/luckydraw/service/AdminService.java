package com.amyway.luckydraw.service;

import com.amyway.luckydraw.domain.Activity;
import com.amyway.luckydraw.domain.Prize;

import com.amyway.luckydraw.domain.DrawRecord;
import com.amyway.luckydraw.dto.DrawRecordQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    Activity createActivity(Activity activity, List<Long> prizeIds);

    List<Activity> listActivities();

    Prize createPrize(Prize prize);

    Prize updatePrize(Long id, Prize prize);

    void deletePrize(Long id);

    List<Prize> listPrizes(Long activityId);

    boolean validateActivityConfig(Long activityId);

    Page<DrawRecord> getDrawRecords(DrawRecordQueryRequest request, Pageable pageable);
}
