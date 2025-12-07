package com.amyway.luckydraw.service;

import com.amyway.luckydraw.domain.User;
import com.amyway.luckydraw.dto.CreateUserRequest;
import com.amyway.luckydraw.dto.UpdateUserRequest;
import com.amyway.luckydraw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        if (request.getDrawQuota() != null) {
            user.setDrawQuota(request.getDrawQuota());
        }
        return userRepository.save(user);
    }

    public List<User> listUsers(String name) {
        if (name != null && !name.trim().isEmpty()) {
            return userRepository.findByNameContaining(name.trim());
        }
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到 ID 為該值的用戶: " + id));
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = getUser(id);
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getDrawQuota() != null) {
            user.setDrawQuota(request.getDrawQuota());
        }
        return userRepository.save(user);
    }
}
