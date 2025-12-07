package com.amyway.luckydraw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Schema(description = "抽獎記錄查詢請求")
public class DrawRecordQueryRequest {

    @Schema(description = "活動 ID")
    private Long activityId;

    @Schema(description = "用戶 ID")
    private Long userId;

    @Schema(description = "用戶名（模糊查詢）")
    private String userName;

    @Schema(description = "是否中獎")
    private Boolean isWin;

    @Schema(description = "開始時間")
    private OffsetDateTime startTime;

    @Schema(description = "結束時間")
    private OffsetDateTime endTime;

    @Schema(description = "頁碼 (0-based)", defaultValue = "0")
    private Integer page = 0;

    @Schema(description = "每頁大小", defaultValue = "10")
    private Integer size = 10;
}
