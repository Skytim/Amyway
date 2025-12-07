package com.amyway.luckydraw.dto;

import com.amyway.luckydraw.domain.Prize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "建立獎品請求")
public class CreatePrizeRequest {

    @Schema(description = "獎品名稱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "總庫存", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalStock;

    // availableStock removed as input, defaults to totalStock

    @Schema(description = "中獎機率 (0.0 - 1.0)", requiredMode = Schema.RequiredMode.REQUIRED)
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Probability must be at least 0.0")
    @jakarta.validation.constraints.DecimalMax(value = "1.0", message = "Probability must be at most 1.0")
    private Double probability;
}
