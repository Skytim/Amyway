package com.amyway.luckydraw.controller;

import com.amyway.luckydraw.domain.User;
import com.amyway.luckydraw.dto.CreateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用戶管理 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final com.amyway.luckydraw.service.UserService userService;

    @Operation(summary = "建立用戶")
    @PostMapping
    public User createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @Operation(summary = "獲取用戶列表")
    @GetMapping
    public List<User> listUsers(@RequestParam(required = false) String name) {
        return userService.listUsers(name);
    }

    @Operation(summary = "獲取指定用戶詳情")
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @Operation(summary = "更新用戶資訊")
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
            @RequestBody com.amyway.luckydraw.dto.UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }
}
