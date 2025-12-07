package com.amyway.luckydraw.controller;

import com.amyway.luckydraw.dto.DrawRequest;
import com.amyway.luckydraw.dto.DrawResult;
import com.amyway.luckydraw.service.DrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "抽獎 API", description = "用戶抽獎相關介面")

@RestController
@RequestMapping("/api/draw")
@RequiredArgsConstructor
public class DrawController {

    private final DrawService drawService;

    @Operation(summary = "用戶抽獎", description = "根據活動 ID 和用戶 ID 執行抽獎操作，返回抽獎結果")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "抽獎成功", content = @Content(schema = @Schema(implementation = DrawResult.class))),
            @ApiResponse(responseCode = "400", description = "抽獎失敗（活動不存在、用戶已達抽獎次數上限等）")
    })
    @PostMapping
    public ResponseEntity<?> draw(@RequestBody DrawRequest request) {
        try {
            java.util.List<DrawResult> result = drawService.draw(request.getUserId(), request.getActivityId(),
                    request.getCount());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

}
