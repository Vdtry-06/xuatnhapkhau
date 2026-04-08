package com.xnk.auth.service;

import com.xnk.auth.dto.AuthDTO.*;
import com.xnk.auth.entity.User;
import com.xnk.auth.repository.UserRepository;
import com.xnk.auth.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostConstruct
    public void seedDefaultUsers() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .enabled(true)
                    .build());
        }
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập không tồn tại"));

        if (!user.isEnabled())
            throw new RuntimeException("Tài khoản đã bị vô hiệu hoá");

        if (!passwordEncoder.matches(req.password(), user.getPassword()))
            throw new RuntimeException("Mật khẩu không đúng");

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getAgentId(), user.getSupplierId());
        return new LoginResponse(token, user.getRole(), user.getAgentId(), user.getSupplierId(), user.getUsername());
    }

    /** Admin tạo tài khoản đăng nhập cho agent — name lấy từ Agent Service, không cần truyền vào đây */
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username()))
            throw new RuntimeException("Tên đăng nhập đã tồn tại: " + req.username());

        User saved = userRepository.save(User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .role("AGENT")
                .agentId(req.agentId())
                .supplierId(null)
                .enabled(true)
                .build());

        return toResponse(saved);
    }

    public UserResponse registerSupplier(RegisterSupplierRequest req) {
        if (userRepository.existsByUsername(req.username()))
            throw new RuntimeException("Tên đăng nhập đã tồn tại: " + req.username());

        User saved = userRepository.save(User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .role("SUPPLIER")
                .agentId(null)
                .supplierId(req.supplierId())
                .enabled(true)
                .build());

        return toResponse(saved);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse toggleEnabled(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user id: " + id));
        user.setEnabled(!user.isEnabled());
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole(), u.getAgentId(), u.getSupplierId(), u.isEnabled());
    }
}
