package com.amyway.luckydraw.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class DrawRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Long activityId;

    private Long prizeId;

    private String prizeName;

    private LocalDateTime drawTime = java.time.LocalDateTime.now();

    private Boolean isWin;
}
