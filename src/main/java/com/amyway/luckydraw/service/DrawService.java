package com.amyway.luckydraw.service;

import com.amyway.luckydraw.domain.Activity;
import com.amyway.luckydraw.domain.DrawRecord;
import com.amyway.luckydraw.domain.Prize;
import com.amyway.luckydraw.dto.DrawResult;
import com.amyway.luckydraw.repository.ActivityRepository;
import com.amyway.luckydraw.repository.DrawRecordRepository;
import com.amyway.luckydraw.repository.PrizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawService {

    private final ActivityRepository activityRepository;
    private final PrizeRepository prizeRepository;
    private final DrawRecordRepository drawRecordRepository;
    private final com.amyway.luckydraw.repository.UserRepository userRepository; // Inject User Repo
    private final jakarta.persistence.EntityManager entityManager; // Inject EntityManager
    private final DrawStrategy drawStrategy;

    @Transactional
    public List<DrawResult> draw(Long userId, Long activityId, Integer count) {
        int drawCount = (count == null || count < 1) ? 1 : count;

        // 0. Check User Validity & Global Quota
        com.amyway.luckydraw.domain.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到該用戶"));

        long totalUserDraws = drawRecordRepository.countByUser_Id(userId);
        if (totalUserDraws + drawCount > user.getDrawQuota()) {
            throw new RuntimeException("用戶已達全域抽獎配額上限 (" + user.getDrawQuota()
                    + ")。無法再抽 " + drawCount + " 次。");
        }

        // 1. Check Activity Validity
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("找不到該活動"));

        if (!activity.isActive()) {
            throw new RuntimeException("活動未啟用");
        }

        // 2. Check User Limits (Per Activity)
        long userActivityDrawCount = drawRecordRepository.countByUser_IdAndActivityId(userId, activityId);
        if (userActivityDrawCount + drawCount > activity.getMaxDrawsPerUser()) {
            throw new RuntimeException(
                    "用戶已達此活動的抽獎次數上限。無法再抽 " + drawCount + " 次。");
        }

        // 3. Get Prizes (Fetch once)
        List<Prize> prizes = prizeRepository.findByActivityId(activityId);
        if (prizes == null || prizes.isEmpty()) {
            throw new RuntimeException("配置錯誤：未找到獎品");
        }

        // Detach prizes to avoid dirty checking overwriting our optimistic lock updates
        // We want to modify 'availableStock' in memory for the batch loop ONLY,
        // relying on 'decrementStock' for the actual DB update.
        for (Prize prize : prizes) {
            entityManager.detach(prize);
        }

        List<DrawResult> results = new java.util.ArrayList<>();

        // Loop for batch execution
        for (int i = 0; i < drawCount; i++) {
            Prize selectedPrize = drawStrategy.draw(prizes);

            if (selectedPrize == null) {
                throw new RuntimeException("活動獎品已送完：無此可用獎品");
            }

            // 4. Stock Decrement (Optimistic Lock)
            if (!"銘謝惠顧".equals(selectedPrize.getName())) {
                int updatedRows = prizeRepository.decrementStock(selectedPrize.getId());
                if (updatedRows > 0) {
                    // Success: Update in-memory object for next iteration in batch
                    if (selectedPrize.getAvailableStock() != null) {
                        selectedPrize.setAvailableStock(selectedPrize.getAvailableStock() - 1);
                    }
                } else {
                    // Failed to secure stock
                    log.info("Failed to secure stock for prize {}, falling back to EMPTY", selectedPrize.getId());
                    selectedPrize = prizes.stream()
                            .filter(p -> "銘謝惠顧".equals(p.getName()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("系統錯誤：未配置銘謝惠顧獎品"));
                }
            }

            // 5. Record Result
            DrawRecord record = new DrawRecord();
            record.setUser(user);
            record.setActivityId(activityId);
            record.setPrizeId(selectedPrize.getId());
            record.setPrizeName(selectedPrize.getName());
            record.setIsWin(!"銘謝惠顧".equals(selectedPrize.getName()));

            drawRecordRepository.save(record);

            results.add(DrawResult.builder()
                    .isWin(record.getIsWin())
                    .prize(selectedPrize)
                    .message(record.getIsWin() ? "恭喜中獎！" : "再接再厲！")
                    .build());
        }

        return results;
    }
}
