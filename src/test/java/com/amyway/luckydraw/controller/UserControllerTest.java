package com.amyway.luckydraw.controller;

import com.amyway.luckydraw.domain.User;
import com.amyway.luckydraw.dto.CreateUserRequest;
import com.amyway.luckydraw.dto.UpdateUserRequest;
import com.amyway.luckydraw.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserService userService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        public void testUserLifecycle() throws Exception {
                User mockUser = new User();
                mockUser.setId(1L);
                mockUser.setName("Test User");
                mockUser.setDrawQuota(50);

                // Mock Service
                when(userService.createUser(any(CreateUserRequest.class))).thenReturn(mockUser);
                when(userService.getUser(1L)).thenReturn(mockUser);
                when(userService.listUsers(null)).thenReturn(Collections.singletonList(mockUser));

                // 1. Create User
                CreateUserRequest createRequest = new CreateUserRequest();
                createRequest.setName("Test User");
                createRequest.setDrawQuota(50);

                mockMvc.perform(post("/api/users")
                                .header("Authorization", "Bearer admin-secret")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Test User"))
                                .andExpect(jsonPath("$.drawQuota").value(50));

                // 2. Get User
                mockMvc.perform(get("/api/users/1")
                                .header("Authorization", "Bearer mock-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Test User"));

                // 3. List Users
                mockMvc.perform(get("/api/users")
                                .header("Authorization", "Bearer mock-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Test User"));

                // 4. Update User
                User updatedUser = new User();
                updatedUser.setId(1L);
                updatedUser.setName("Updated User");
                updatedUser.setDrawQuota(100);

                when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser);

                UpdateUserRequest updateRequest = new UpdateUserRequest();
                updateRequest.setName("Updated User");
                updateRequest.setDrawQuota(100);

                mockMvc.perform(put("/api/users/1")
                                .header("Authorization", "Bearer mock-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated User"))
                                .andExpect(jsonPath("$.drawQuota").value(100));
        }

        @Test
        public void testListUsersWithFiltering() throws Exception {
                User mockUser = new User();
                mockUser.setId(1L);
                mockUser.setName("Amy");
                mockUser.setDrawQuota(10);

                when(userService.listUsers("Amy")).thenReturn(Collections.singletonList(mockUser));

                mockMvc.perform(get("/api/users?name=Amy")
                                .header("Authorization", "Bearer mock-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Amy"));
        }
}
