package com.amyway.luckydraw.service;

import com.amyway.luckydraw.domain.Activity;
import com.amyway.luckydraw.domain.Prize;
import com.amyway.luckydraw.repository.ActivityRepository;
import com.amyway.luckydraw.repository.DrawRecordRepository;
import com.amyway.luckydraw.repository.PrizeRepository;
import com.amyway.luckydraw.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(properties = "spring.data.redis.repositories.enabled=false")
public class DrawConcurrencyTest {

    @Autowired
    private DrawService drawService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private PrizeRepository prizeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testConcurrencyStock() throws InterruptedException {
        // Setup
        Activity activity = new Activity();
        activity.setName("Concurrent Activity");
        activity.setMaxDrawsPerUser(100);
        activityRepository.save(activity);

        Prize prize = new Prize();
        prize.setName("Limited Stock Prize");
        prize.setTotalStock(10);
        prize.setAvailableStock(10);
        prize.setProbability(1.0);

        activity.getPrizes().add(prize);
        activityRepository.save(activity);

        // Re-fetch prize to get accurate ID and avoid lazy loading issues without
        // Transactional
        // We find the prize that belongs to this activity (or just the one we created)
        // Since we created it, we can query by name or just grab it from repo if we
        // handle IDs

        // Simpler way: Find the prize by name to be sure
        List<Prize> allPrizes = prizeRepository.findAll();
        Long prizeId = allPrizes.stream()
                .filter(p -> "Limited Stock Prize".equals(p.getName()))
                .map(Prize::getId)
                .findFirst()
                .orElseThrow();

        // NOTE: We don't update the 'prize' object reference here to avoid using valid
        // attached entity?
        // Actually, we just need the count check later.

        int threadCount = 20;
        List<Long> userIds = new ArrayList<>();

        // Create users for test
        for (int i = 0; i < threadCount; i++) {
            com.amyway.luckydraw.domain.User user = new com.amyway.luckydraw.domain.User();
            user.setName("user" + i);
            user.setDrawQuota(100);
            userRepository.save(user);
            userIds.add(user.getId());
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger winCount = new AtomicInteger();

        // Execute
        for (int i = 0; i < threadCount; i++) {
            final Long userId = userIds.get(i);
            executor.execute(() -> {
                try {
                    var results = drawService.draw(userId, activity.getId(), 1);
                    if (!results.isEmpty() && results.get(0).isWin()) {
                        winCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Log failure but don't fail test thread (assert count at end)
                    // System.out.println("Draw exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // Assert
        Prize updatedPrize = prizeRepository.findById(prizeId).orElseThrow();
        System.out.println("Wins: " + winCount.get());
        System.out.println("Remaining Stock: " + updatedPrize.getAvailableStock());

        Assertions.assertTrue(winCount.get() <= 10, "Wins (" + winCount.get() + ") exceeded stock (10)!");
        Assertions.assertEquals(10 - winCount.get(), updatedPrize.getAvailableStock(), "Stock mismatch!");
    }
}
