package com.amyway.luckydraw.service;

import com.amyway.luckydraw.domain.Prize;
import com.amyway.luckydraw.service.impl.WeightedRandomDrawStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WeightedRandomDrawStrategyTest {

    private final WeightedRandomDrawStrategy strategy = new WeightedRandomDrawStrategy();

    @Test
    public void testProbabilityDistribution() {
        List<Prize> prizes = new ArrayList<>();
        prizes.add(createPrize(1L, "Gold", 0.1));
        prizes.add(createPrize(2L, "Silver", 0.3));
        prizes.add(createPrize(3L, "Bronze", 0.6));

        Map<String, Integer> counts = new HashMap<>();
        int totalDraws = 10000;

        for (int i = 0; i < totalDraws; i++) {
            Prize p = strategy.draw(prizes);
            counts.put(p.getName(), counts.getOrDefault(p.getName(), 0) + 1);
        }

        System.out.println("Draw Results: " + counts);

        // Allow 2% margin of error
        assertProbability(counts.get("Gold"), totalDraws, 0.1, 0.02);
        assertProbability(counts.get("Silver"), totalDraws, 0.3, 0.02);
        assertProbability(counts.get("Bronze"), totalDraws, 0.6, 0.02);
    }

    private void assertProbability(int count, int total, double expectedProb, double margin) {
        double actualProb = (double) count / total;
        assertTrue(Math.abs(actualProb - expectedProb) < margin, 
                   "Expected " + expectedProb + " but got " + actualProb);
    }

    private Prize createPrize(Long id, String name, double prob) {
        Prize p = new Prize();
        p.setId(id);
        p.setName(name);
        p.setProbability(prob);
        p.setAvailableStock(100000);
        return p;
    }
}
