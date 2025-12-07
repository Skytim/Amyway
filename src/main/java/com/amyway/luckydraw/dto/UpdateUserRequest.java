package com.amyway.luckydraw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新用戶請求")
public class UpdateUserRequest {
    @Schema(description = "用戶名稱")
    private String name;

    @Schema(description = "抽獎配額")
    private Integer drawQuota;
}
