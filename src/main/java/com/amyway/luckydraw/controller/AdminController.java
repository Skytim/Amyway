package com.amyway.luckydraw.controller;

import com.amyway.luckydraw.domain.Activity;
import com.amyway.luckydraw.domain.Prize;
import com.amyway.luckydraw.dto.CreateActivityRequest;
import com.amyway.luckydraw.dto.CreatePrizeRequest;
import com.amyway.luckydraw.dto.UpdatePrizeRequest;
import com.amyway.luckydraw.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理 API", description = "活動和獎品管理介面")

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "建立抽獎活動", description = "建立新的抽獎活動")
    @ApiResponse(responseCode = "200", description = "活動建立成功")
    @PostMapping("/activities")
    public Activity createActivity(@RequestBody CreateActivityRequest request) {
        Activity activity = new Activity();
        activity.setName(request.getName());
        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime().toLocalDateTime());
        }
        if (request.getEndTime() != null) {
            activity.setEndTime(request.getEndTime().toLocalDateTime());
        }
        if (request.getMaxDrawsPerUser() != null) {
            activity.setMaxDrawsPerUser(request.getMaxDrawsPerUser());
        }

        return adminService.createActivity(activity, request.getPrizeIds());
    }

    @Operation(summary = "獲取活動列表", description = "獲取所有抽獎活動的列表")
    @ApiResponse(responseCode = "200", description = "成功返回活動列表")
    @GetMapping("/activities")
    public List<Activity> listActivities() {
        return adminService.listActivities();
    }

    @Operation(summary = "建立獎品", description = "為指定活動建立新的獎品")
    @ApiResponse(responseCode = "200", description = "獎品建立成功")
    @PostMapping("/prizes")
    public Prize createPrize(
            @RequestBody CreatePrizeRequest request) {
        Prize prize = new Prize();
        // activityId is passed as separate argument to service
        prize.setName(request.getName());
        prize.setTotalStock(request.getTotalStock());

        prize.setProbability(request.getProbability());

        // Initialize available stock if null (Service also checks this but safe to do
        // here or there)

        return adminService.createPrize(prize);
    }

    @Operation(summary = "更新獎品", description = "更新獎品資訊（庫存、機率等）")
    @ApiResponse(responseCode = "200", description = "獎品更新成功")
    @PutMapping("/prizes/{id}")
    public Prize updatePrize(@PathVariable Long id, @RequestBody @Valid UpdatePrizeRequest request) {
        Prize prize = new Prize();
        // ID is passed separately to service or used to lookup
        // Here we map fields to a temporary object or directly pass DTO to service if
        // service accepted DTO.
        // Since AdminService accepts Prize entity, we map what we have.
        // Notes: AdminService.updatePrize takes (id, Prize details).
        prize.setName(request.getName());
        prize.setTotalStock(request.getTotalStock());
        prize.setAvailableStock(request.getAvailableStock());
        prize.setProbability(request.getProbability());
        return adminService.updatePrize(id, prize);
    }

    @Operation(summary = "刪除獎品", description = "刪除指定獎品")
    @ApiResponse(responseCode = "200", description = "獎品刪除成功")
    @DeleteMapping("/prizes/{id}")
    public ResponseEntity<Void> deletePrize(@PathVariable Long id) {
        adminService.deletePrize(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "獲取活動獎品列表", description = "獲取指定活動的所有獎品")
    @ApiResponse(responseCode = "200", description = "成功返回獎品列表")
    @GetMapping("/activities/{activityId}/prizes")
    public List<Prize> listPrizes(
            @Parameter(description = "活動 ID", required = true) @PathVariable Long activityId) {
        return adminService.listPrizes(activityId);
    }

    @Operation(summary = "驗證活動配置", description = "檢查活動獎品機率和是否為 100%")
    @ApiResponse(responseCode = "200", description = "驗證結果")
    @GetMapping("/activities/{activityId}/validate")
    public ResponseEntity<Map<String, Object>> validateActivityConfig(
            @PathVariable Long activityId) {
        boolean isValid = adminService.validateActivityConfig(activityId);
        return ResponseEntity.ok(Map.of(
                "activityId", activityId,
                "isValid", isValid,
                "message", isValid ? "Configuration is valid" : "Total probability must be 1.0 (100%)"));
    }

    @Operation(summary = "查詢中獎記錄", description = "根據條件查詢中獎記錄")
    @ApiResponse(responseCode = "200", description = "成功返回中獎記錄列表")
    @GetMapping("/draw-records")
    public org.springframework.data.domain.Page<com.amyway.luckydraw.domain.DrawRecord> getDrawRecords(
            @org.springdoc.core.annotations.ParameterObject com.amyway.luckydraw.dto.DrawRecordQueryRequest request) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(request.getPage(), request.getSize());
        return adminService.getDrawRecords(request, pageable);
    }
}
