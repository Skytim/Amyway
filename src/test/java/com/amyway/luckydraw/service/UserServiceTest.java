package com.amyway.luckydraw.service;

import com.amyway.luckydraw.domain.User;
import com.amyway.luckydraw.dto.CreateUserRequest;
import com.amyway.luckydraw.dto.UpdateUserRequest;
import com.amyway.luckydraw.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Test User");
        req.setDrawQuota(50);

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User created = userService.createUser(req);

        assertNotNull(created.getId());
        assertEquals("Test User", created.getName());
        assertEquals(50, created.getDrawQuota());
    }

    @Test
    void listUsers_WithSearch() {
        when(userRepository.findByNameContaining("Amy")).thenReturn(List.of(new User()));

        List<User> results = userService.listUsers(" Amy "); // check trim
        verify(userRepository).findByNameContaining("Amy");
        assertEquals(1, results.size());
    }

    @Test
    void updateUser_Success() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old");
        existing.setDrawQuota(10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setName("New");
        // quota null, should not change

        User updated = userService.updateUser(1L, req);

        assertEquals("New", updated.getName());
        assertEquals(10, updated.getDrawQuota());
    }

    @Test
    void getUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.getUser(99L));
    }
}
