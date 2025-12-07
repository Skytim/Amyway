package com.amyway.luckydraw.service;

import com.amyway.luckydraw.domain.Activity;
import com.amyway.luckydraw.domain.Prize;
import com.amyway.luckydraw.domain.User;
import com.amyway.luckydraw.dto.DrawResult;
import com.amyway.luckydraw.repository.ActivityRepository;
import com.amyway.luckydraw.repository.DrawRecordRepository;
import com.amyway.luckydraw.repository.PrizeRepository;
import com.amyway.luckydraw.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {

    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private PrizeRepository prizeRepository;
    @Mock
    private DrawRecordRepository drawRecordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private DrawStrategy drawStrategy;

    @InjectMocks
    private DrawService drawService;

    @Test
    void draw_Success_Win() {
        // Arrange
        Long userId = 1L;
        Long activityId = 100L;

        User user = new User();
        user.setId(userId);
        user.setDrawQuota(10);

        Activity activity = new Activity();
        activity.setId(activityId);
        activity.setMaxDrawsPerUser(5);

        Prize prize = new Prize();
        prize.setId(50L);
        prize.setName("Gold");
        prize.setAvailableStock(5);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(drawRecordRepository.countByUser_Id(userId)).thenReturn(0L); // global count

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(drawRecordRepository.countByUser_IdAndActivityId(userId, activityId)).thenReturn(0L); // activity count

        when(prizeRepository.findByActivityId(activityId)).thenReturn(new ArrayList<>(List.of(prize)));

        // Mock Strategy
        when(drawStrategy.draw(anyList())).thenReturn(prize);

        // Mock Decrement Success
        when(prizeRepository.decrementStock(prize.getId())).thenReturn(1);

        // Act
        List<DrawResult> results = drawService.draw(userId, activityId, 1);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        DrawResult result = results.get(0);
        assertTrue(result.isWin());
        assertEquals("Gold", result.getPrize().getName());
        verify(drawRecordRepository).save(any());
    }

    @Test
    void draw_Failure_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> drawService.draw(1L, 100L, 1));
    }

    @Test
    void draw_Failure_ActivityInactive() {
        User user = new User();
        user.setDrawQuota(10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Activity activity = new Activity();
        when(activityRepository.findById(100L)).thenReturn(Optional.of(activity));

        assertThrows(RuntimeException.class, () -> drawService.draw(1L, 100L, 1));
    }

    @Test
    void draw_Failure_GlobalQuotaExceeded() {
        User user = new User();
        user.setDrawQuota(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(drawRecordRepository.countByUser_Id(1L)).thenReturn(1L);

        Exception e = assertThrows(RuntimeException.class, () -> drawService.draw(1L, 100L, 1));
        assertTrue(e.getMessage().contains("全域抽獎配額上限"));
    }

    @Test
    void draw_Fallback_StockExhaustedConcurrent() {
        // Validation passed... but decrement fails
        Long userId = 1L;
        Long activityId = 100L;
        User user = new User();
        user.setDrawQuota(10);
        Activity activity = new Activity();
        activity.setMaxDrawsPerUser(10);

        Prize gold = new Prize();
        gold.setId(50L);
        gold.setName("Gold");
        Prize empty = new Prize();
        empty.setId(99L);
        empty.setName("銘謝惠顧");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(prizeRepository.findByActivityId(activityId)).thenReturn(new ArrayList<>(List.of(gold, empty)));

        when(drawStrategy.draw(anyList())).thenReturn(gold); // Strategy picks Gold
        when(prizeRepository.decrementStock(gold.getId())).thenReturn(0); // DB says NO STOCK (Concurrent)

        // Act
        List<DrawResult> results = drawService.draw(userId, activityId, 1);

        // Assert
        // Should fallback to empty
        assertEquals(1, results.size());
        assertFalse(results.get(0).isWin());
        assertEquals("銘謝惠顧", results.get(0).getPrize().getName());
    }
}
