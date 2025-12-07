package com.amyway.luckydraw.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer maxDrawsPerUser;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "activity_prizes", joinColumns = @JoinColumn(name = "activity_id"), inverseJoinColumns = @JoinColumn(name = "prize_id"))
    private final List<Prize> prizes = new ArrayList<>();

    // Helper to check if active
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return (startTime == null || !now.isBefore(startTime)) &&
                (endTime == null || !now.isAfter(endTime));
    }
}
