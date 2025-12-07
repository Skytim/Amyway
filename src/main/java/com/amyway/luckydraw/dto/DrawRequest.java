package com.amyway.luckydraw.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class DrawRequest {
    @Schema(description = "用戶 ID")
    private Long userId;
    private Long activityId;

    @Schema(description = "抽獎次數", defaultValue = "1")
    private Integer count = 1;
}
