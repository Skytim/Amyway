package com.amyway.luckydraw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "建立活動請求")
public class CreateActivityRequest {

    @Schema(description = "活動名稱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "活動開始時間")
    private java.time.OffsetDateTime startTime;

    @Schema(description = "活動結束時間")
    private java.time.OffsetDateTime endTime;

    @Schema(description = "每位用戶最大抽獎次數", defaultValue = "1")
    private Integer maxDrawsPerUser;

    @Schema(description = "活動獎品 ID 列表")
    private final List<Long> prizeIds = new ArrayList<>();
}
