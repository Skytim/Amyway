package com.amyway.luckydraw.dto;

import com.amyway.luckydraw.domain.Prize;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DrawResult {
    private boolean isWin;
    private Prize prize;
    private String message;
}
