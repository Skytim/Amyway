package com.amyway.luckydraw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建用户请求")
public class CreateUserRequest {
    @Schema(description = "用户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "抽奖配额 (默认为10)")
    private Integer drawQuota;
}
