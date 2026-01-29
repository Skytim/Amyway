package com.amyway.luckydraw.service.impl;

import com.amyway.luckydraw.domain.Activity;
import com.amyway.luckydraw.domain.Prize;
import com.amyway.luckydraw.repository.ActivityRepository;
import com.amyway.luckydraw.repository.PrizeRepository;
import com.amyway.luckydraw.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amyway.luckydraw.domain.DrawRecord;
import com.amyway.luckydraw.dto.DrawRecordQueryRequest;
import com.amyway.luckydraw.repository.DrawRecordRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ActivityRepository activityRepository;
    private final PrizeRepository prizeRepository;
    private final DrawRecordRepository drawRecordRepository;

    @Override
    @Transactional
    public Activity createActivity(Activity activity, List<Long> prizeIds) {
        // 0. Time Validation: Start and End time must be at least 10 minutes apart
        if (activity.getStartTime() != null && activity.getEndTime() != null) {
            long minutes = java.time.Duration.between(activity.getStartTime(), activity.getEndTime()).toMinutes();
            if (minutes < 10) {
                throw new RuntimeException("活動開始與結束時間至少需間隔 10 分鐘");
            }
        }

        List<Prize> prizes = new java.util.ArrayList<>();
        if (prizeIds != null && !prizeIds.isEmpty()) {
            prizes = prizeRepository.findAllById(prizeIds);

            // 1. Validation: Check if all prizes exist
            if (prizes.size() != prizeIds.stream().distinct().count()) {
                throw new RuntimeException("未找到部分獎品或提供了重複的 ID");
            }

            // 2. Validation: Check for duplicates (if logic requires unique prizes per
            // activity)
            // findAllById returns unique entities. If user sent [1, 1], findAllById returns
            // [Prize1].
            // If we want to strictly prevent duplicate IDs in request:
            if (prizes.size() != prizeIds.size()) {
                // This implies duplicates in input if distinct check passed?
                // Actually findAllById ignores duplicates in input usually.
                // Let's rely on the retrieved list.
            }

            // 3. Validation: Stock check "cannot be exhausted"
            for (Prize p : prizes) {
                if (p.getAvailableStock() <= 0) {
                    throw new RuntimeException("獎品 '" + p.getName() + "' (ID: " + p.getId() + ") 已無庫存");
                }
            }
        }

        // 4. Probability Check & Auto-fill
        BigDecimal totalInfoProbBd = prizes.stream()
                .map(p -> BigDecimal.valueOf(p.getProbability()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double totalInfoProb = totalInfoProbBd.doubleValue();

        if (totalInfoProb > 1.0 + 0.0001) {
            throw new RuntimeException("總機率超過 100%: " + totalInfoProb);
        }

        if (totalInfoProb < 1.0 - 0.0001) {
            // Auto-create "Thank You" prize
            double remainingProb = BigDecimal.ONE.subtract(totalInfoProbBd).doubleValue();
            Prize thankYouPrize = new Prize();
            thankYouPrize.setName("銘謝惠顧");
            thankYouPrize.setProbability(remainingProb);
            thankYouPrize.setTotalStock(999999); // "Infinite" stock concept
            thankYouPrize.setAvailableStock(999999);

            // Persist the new prize
            thankYouPrize = prizeRepository.save(thankYouPrize);

            // Add to the list
            prizes.add(thankYouPrize);
        }

        // Link finalized prizes to activity
        activity.getPrizes().addAll(prizes);

        return activityRepository.save(activity);
    }

    @Override
    public List<Activity> listActivities() {
        return activityRepository.findAll();
    }

    @Override
    @Transactional
    public Prize createPrize(Prize prize) {
        // Initialize available stock
        if (prize.getAvailableStock() == null) {
            prize.setAvailableStock(prize.getTotalStock());
        }

        return prizeRepository.save(prize);
    }

    @Override
    @Transactional
    public Prize updatePrize(Long id, Prize prizeDetails) {
        Prize prize = prizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到獎品: " + id));

        // 1. Validate and Balance affected Activities
        List<Activity> activities = activityRepository.findByPrizesId(id);

        // Double precision epsilon
        double epsilon = 0.0001;

        for (Activity activity : activities) {
            BigDecimal otherPrizesProbSumBd = BigDecimal.ZERO;
            Prize thankYouPrize = null;
            boolean thankYouIsTarget = false;

            for (Prize p : activity.getPrizes()) {
                if (p.getId().equals(id)) {
                    // This is the prize being updated (could be the Thank You prize itself?)
                    if ("銘謝惠顧".equals(p.getName())) {
                        thankYouPrize = p; // It is the target AND the thank you prize
                        thankYouIsTarget = true;
                    }
                    // Don't add to sum yet, use new probability
                } else if ("銘謝惠顧".equals(p.getName())) {
                    thankYouPrize = p;
                } else {
                    otherPrizesProbSumBd = otherPrizesProbSumBd.add(BigDecimal.valueOf(p.getProbability()));
                }
            }

            // Expected usage: User updates a NORMAL prize. "Thank You" is adjusted.
            // If user updates "Thank You" prize directly:
            // logic: check if new "Thank You" prob + others <= 1.0?
            // Or strictly: others + target <= 1.0.

            double newTargetProb = prizeDetails.getProbability();
            BigDecimal newTargetProbBd = BigDecimal.valueOf(newTargetProb);

            if (thankYouIsTarget) {
                // Updating Thank You prize manually
                // Check if other + new > 1.0
                BigDecimal totalBd = otherPrizesProbSumBd.add(newTargetProbBd);
                if (totalBd.doubleValue() > 1.0 + epsilon) {
                    throw new RuntimeException("活動 '" + activity.getName() + "' 總機率超過 100%");
                }
                // No auto-balance needed, user is setting it explicitly
            } else {
                // Updating a Normal Prize
                BigDecimal totalWithoutThankYouBd = otherPrizesProbSumBd.add(newTargetProbBd);
                double totalWithoutThankYou = totalWithoutThankYouBd.doubleValue();

                if (totalWithoutThankYou > 1.0 + epsilon) {
                    throw new RuntimeException("更新失敗。活動 '" + activity.getName() +
                            "' 的機率將超過 100% (總和: " + totalWithoutThankYou + ")");
                }

                // Auto-balance existing Thank You prize
                if (thankYouPrize != null) {
                    double newThankYouProb = BigDecimal.ONE.subtract(totalWithoutThankYouBd).doubleValue();
                    // Avoid negative due to precision
                    if (newThankYouProb < 0)
                        newThankYouProb = 0.0;

                    thankYouPrize.setProbability(newThankYouProb);
                    prizeRepository.save(thankYouPrize);
                }
            }
        }

        // 2. Perform Update
        prize.setName(prizeDetails.getName());
        prize.setProbability(prizeDetails.getProbability());

        int diff = prizeDetails.getTotalStock() - prize.getTotalStock();
        prize.setTotalStock(prizeDetails.getTotalStock());
        if (diff != 0) {
            prize.setAvailableStock(prize.getAvailableStock() + diff);
        }
        if (prizeDetails.getAvailableStock() != null
                && !prizeDetails.getAvailableStock().equals(prize.getAvailableStock())) {
            prize.setAvailableStock(prizeDetails.getAvailableStock());
        }

        return prizeRepository.save(prize);
    }

    @Override
    @Transactional
    public void deletePrize(Long id) {
        // With ManyToMany, deleting a prize might be tricky if it's shared?
        // But current requirement assumes independent creation per activity usually.
        // If shared, we should just remove relation?
        // But deletePrize usually means "Remove this prize definition from system" or
        // "From activity"?
        // API is DELETE /prizes/{id}. Implies global delete.
        prizeRepository.deleteById(id);
    }

    @Override
    public List<Prize> listPrizes(Long activityId) {
        return prizeRepository.findByActivityId(activityId);
    }

    @Override
    public boolean validateActivityConfig(Long activityId) {
        List<Prize> prizes = prizeRepository.findByActivityId(activityId);
        double totalProb = prizes.stream()
                .mapToDouble(Prize::getProbability)
                .sum();

        // Check if sum is close to 1.0 (allow small epsilon for float point errors)
        boolean isValid = Math.abs(totalProb - 1.0) < 0.0001;

        if (!isValid) {
            log.warn("Activity {} configuration invalid: Total probability is {}", activityId, totalProb);
        }

        return isValid;
    }

    @Override
    public Page<DrawRecord> getDrawRecords(DrawRecordQueryRequest request, Pageable pageable) {
        return drawRecordRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getActivityId() != null) {
                predicates.add(cb.equal(root.get("activityId"), request.getActivityId()));
            }

            if (request.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), request.getUserId()));
            }

            if (request.getUserName() != null && !request.getUserName().isEmpty()) {
                // Join User table to filter by name
                predicates.add(cb.like(root.get("user").get("name"), "%" + request.getUserName() + "%"));
            }

            if (request.getIsWin() != null) {
                predicates.add(cb.equal(root.get("isWin"), request.getIsWin()));
            }

            if (request.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("drawTime"), request.getStartTime().toLocalDateTime()));
            }

            if (request.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("drawTime"), request.getEndTime().toLocalDateTime()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }
}
