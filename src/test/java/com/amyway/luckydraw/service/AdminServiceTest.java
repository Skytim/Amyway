package com.amyway.luckydraw.service;

import com.amyway.luckydraw.domain.Activity;
import com.amyway.luckydraw.domain.Prize;
import com.amyway.luckydraw.repository.ActivityRepository;
import com.amyway.luckydraw.repository.DrawRecordRepository;
import com.amyway.luckydraw.repository.PrizeRepository;
import com.amyway.luckydraw.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private PrizeRepository prizeRepository;

    @Mock
    private DrawRecordRepository drawRecordRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void createActivity_Success() {
        // Arrange
        Activity activity = new Activity();
        activity.setName("Test Activity");
        activity.setStartTime(OffsetDateTime.now().plusHours(1).toLocalDateTime());
        activity.setEndTime(OffsetDateTime.now().plusHours(2).toLocalDateTime());

        Prize prize = new Prize();
        prize.setId(1L);
        prize.setName("Gold");
        prize.setProbability(1.0);
        prize.setTotalStock(10);
        prize.setAvailableStock(10);

        List<Long> prizeIds = List.of(1L);

        when(prizeRepository.findAllById(prizeIds)).thenReturn(List.of(prize));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Activity created = adminService.createActivity(activity, prizeIds);

        // Assert
        assertNotNull(created);
        assertEquals("Test Activity", created.getName());
        assertEquals(1, created.getPrizes().size());
        verify(activityRepository).save(activity);
    }

    @Test
    void createActivity_Failure_TimeGap() {
        // Arrange
        Activity activity = new Activity();
        activity.setStartTime(OffsetDateTime.now().toLocalDateTime());
        activity.setEndTime(OffsetDateTime.now().plusMinutes(5).toLocalDateTime()); // < 10 mins

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> adminService.createActivity(activity, List.of()));
        assertTrue(exception.getMessage().contains("至少需間隔 10 分鐘"));
    }

    @Test
    void createActivity_Failure_ProbExceeds100() {
        // Arrange
        Activity activity = new Activity();

        Prize p1 = new Prize();
        p1.setId(1L);
        p1.setProbability(0.6);
        p1.setAvailableStock(10);
        Prize p2 = new Prize();
        p2.setId(2L);
        p2.setProbability(0.5);
        p2.setAvailableStock(10);

        List<Long> prizeIds = List.of(1L, 2L);
        when(prizeRepository.findAllById(prizeIds)).thenReturn(List.of(p1, p2));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.createActivity(activity, prizeIds));
    }

    @Test
    void createActivity_AutoCreateThankYouPrize() {
        // Arrange
        Activity activity = new Activity();
        activity.setName("Auto Fill");

        Prize p1 = new Prize();
        p1.setId(1L);
        p1.setProbability(0.8);
        p1.setAvailableStock(10);

        List<Long> prizeIds = new ArrayList<>(Arrays.asList(1L)); // mutable list for mockito if needed, but impl adds
                                                                  // to activity.prizes

        when(prizeRepository.findAllById(prizeIds)).thenReturn(new ArrayList<>(Arrays.asList(p1)));
        when(prizeRepository.save(any(Prize.class))).thenAnswer(inv -> {
            Prize p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Activity created = adminService.createActivity(activity, prizeIds);

        // Assert
        assertEquals(2, created.getPrizes().size());
        Prize thankYou = created.getPrizes().stream()
                .filter(p -> "銘謝惠顧".equals(p.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(thankYou);
        assertEquals(0.2, thankYou.getProbability(), 0.0001);
    }

    @Test
    void updatePrize_Success_UpdateStock() {
        // Arrange
        Long prizeId = 1L;
        Prize existing = new Prize();
        existing.setId(prizeId);
        existing.setTotalStock(10);
        existing.setAvailableStock(5);
        existing.setProbability(0.5);
        existing.setName("Old Name");

        Prize updateDetails = new Prize();
        updateDetails.setName("New Name");
        updateDetails.setTotalStock(20); // +10
        updateDetails.setTotalStock(20); // +10
        updateDetails.setAvailableStock(null); // Allow logic to calc delta: 5 + (20-10) = 15
        // Impl: if (diff != 0) available += diff.
        // Also: if details.available != null && !equals, set available.
        // Let's test the delta logic.
        updateDetails.setProbability(0.5); // same prob

        when(prizeRepository.findById(prizeId)).thenReturn(Optional.of(existing));
        when(activityRepository.findByPrizesId(prizeId)).thenReturn(List.of()); // No linked activities to validate prob
        when(prizeRepository.save(any(Prize.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Prize updated = adminService.updatePrize(prizeId, updateDetails);

        // Assert
        assertEquals("New Name", updated.getName());
        assertEquals(20, updated.getTotalStock());
        assertEquals(15, updated.getAvailableStock()); // 5 + (20-10) = 15
    }
}
