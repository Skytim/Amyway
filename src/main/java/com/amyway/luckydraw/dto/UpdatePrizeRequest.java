package com.amyway.luckydraw.dto;

import com.amyway.luckydraw.domain.Prize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新獎品請求")
public class UpdatePrizeRequest {

    @Schema(description = "獎品名稱")
    private String name;

    @Schema(description = "總庫存")
    private Integer totalStock;

    @Schema(description = "可用庫存")
    private Integer availableStock;

    @Schema(description = "中獎機率 (0.0 - 1.0)")
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Probability must be at least 0.0")
    @jakarta.validation.constraints.DecimalMax(value = "1.0", message = "Probability must be at most 1.0")
    private Double probability;
}
