package com.amyway.luckydraw.service.impl;

import com.amyway.luckydraw.domain.Prize;
import com.amyway.luckydraw.service.DrawStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class WeightedRandomDrawStrategy implements DrawStrategy {

    @Override
    public Prize draw(List<Prize> prizes) {
        if (prizes == null || prizes.isEmpty()) {
            return null;
        }

        // Filter out out-of-stock prizes (except infinite stock ones like 'Empty' if
        // designed that way)
        // For this design, we assume EMPTY has infinite stock or separate logic,
        // but here we filter by probability > 0 and stock > 0

        double totalProbability = prizes.stream()
                .filter(p -> p.getAvailableStock() > 0)
                .mapToDouble(Prize::getProbability)
                .sum();

        // Safety check if total probability is not 1.0 (it should ideally be normalized
        // or handle gaps)
        // The requirement says "Grand total including miss is 100%"

        double randomValue = ThreadLocalRandom.current().nextDouble(); // 0.0 to 1.0
        double cumulativeProbability = 0.0;

        for (Prize prize : prizes) {
            // Skip if no stock
            if (prize.getAvailableStock() <= 0) {
                continue;
            }

            cumulativeProbability += prize.getProbability();
            if (randomValue <= cumulativeProbability) {
                return prize;
            }
        }

        // Fallback to the last item or specific "Miss" item if rounding errors
        // Ideally should find the "EMPTY" prize
        return prizes.stream()
                .filter(p -> "銘謝惠顧".equals(p.getName()))
                .findFirst()
                .orElse(null);
    }
}
