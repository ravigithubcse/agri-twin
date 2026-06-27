package com.agritwin.user.service;

import com.agritwin.user.dto.*;
import com.agritwin.user.entity.User;
import com.agritwin.user.entity.UserRole;
import com.agritwin.user.exception.InvalidCredentialsException;
import com.agritwin.user.exception.PhoneAlreadyRegisteredException;
import com.agritwin.user.exception.UserNotFoundException;
import com.agritwin.user.repository.UserRepository;
import com.agritwin.user.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider,
                        RefreshTokenService refreshTokenService,
                        UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw new PhoneAlreadyRegisteredException(request.phone());
        }

        User user = User.builder()
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(UserRole.FARMER)
                .stateCode(request.stateCode())
                .districtCode(request.districtCode())
                .languagePreference(request.languagePreference() != null ? request.languagePreference() : "hi")
                .build();

        user = userRepository.save(user);

        return buildAuthResponse(user, "registration");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.phone())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        return buildAuthResponse(user, "login");
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        UUID userId = refreshTokenService.validateAndRotate(request.refreshToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return buildAuthResponse(user, "refresh");
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toResponse(user);
    }

    private AuthResponse buildAuthResponse(User user, String deviceInfo) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getPhone(), user.getRole().name());
        String refreshToken = refreshTokenService.issue(user.getId(), deviceInfo);

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirySeconds(),
                userMapper.toResponse(user)
        );
    }
}
