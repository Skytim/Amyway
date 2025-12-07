package com.amyway.luckydraw.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // activityId removed to support Any-to-Many (One Prize used by multiple
    // Activities or global)
    // Relationship is now managed by Activity -> ManyToMany -> Prize

    private String name;

    private Integer totalStock;

    private Integer availableStock;

    private Double probability; // 0.0 - 1.0

    @Version
    private Integer version; // Optimistic locking
}
