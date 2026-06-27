package com.agritwin.user.service;

import com.agritwin.user.dto.*;
import com.agritwin.user.entity.AccountStatus;
import com.agritwin.user.entity.SubscriptionTier;
import com.agritwin.user.entity.User;
import com.agritwin.user.entity.UserRole;
import com.agritwin.user.exception.InvalidCredentialsException;
import com.agritwin.user.exception.PhoneAlreadyRegisteredException;
import com.agritwin.user.exception.UserNotFoundException;
import com.agritwin.user.repository.UserRepository;
import com.agritwin.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .phone("9876543210")
                .passwordHash("hashed-password")
                .fullName("Ramesh Kumar")
                .role(UserRole.FARMER)
                .tier(SubscriptionTier.FREE)
                .languagePreference("hi")
                .literacyFlag(true)
                .accountStatus(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("register() creates a new user when phone is not already taken")
    void register_createsNewUser_whenPhoneNotTaken() {
        RegisterRequest request = new RegisterRequest("9876543210", "SecurePass123", "Ramesh Kumar", "MH", "PUN", "hi");

        when(userRepository.existsByPhone("9876543210")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("access-token");
        when(jwtTokenProvider.getAccessTokenExpirySeconds()).thenReturn(900L);
        when(refreshTokenService.issue(any(), anyString())).thenReturn("refresh-token");
        when(userMapper.toResponse(sampleUser)).thenReturn(
                new UserResponse(sampleUser.getId(), "9876543210", "Ramesh Kumar", UserRole.FARMER,
                        SubscriptionTier.FREE, "MH", "PUN", "hi", sampleUser.getCreatedAt()));

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().phone()).isEqualTo("9876543210");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register() throws PhoneAlreadyRegisteredException when phone is taken")
    void register_throws_whenPhoneAlreadyTaken() {
        RegisterRequest request = new RegisterRequest("9876543210", "SecurePass123", "Ramesh Kumar", null, null, null);
        when(userRepository.existsByPhone("9876543210")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(PhoneAlreadyRegisteredException.class)
                .hasMessageContaining("9876543210");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login() succeeds with correct credentials")
    void login_succeeds_withCorrectCredentials() {
        LoginRequest request = new LoginRequest("9876543210", "SecurePass123");

        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("SecurePass123", "hashed-password")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("access-token");
        when(jwtTokenProvider.getAccessTokenExpirySeconds()).thenReturn(900L);
        when(refreshTokenService.issue(any(), anyString())).thenReturn("refresh-token");
        when(userMapper.toResponse(sampleUser)).thenReturn(
                new UserResponse(sampleUser.getId(), "9876543210", "Ramesh Kumar", UserRole.FARMER,
                        SubscriptionTier.FREE, null, null, "hi", sampleUser.getCreatedAt()));

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(userRepository).save(argThat(u -> u.getLastLogin() != null));
    }

    @Test
    @DisplayName("login() throws InvalidCredentialsException when phone not found")
    void login_throws_whenPhoneNotFound() {
        LoginRequest request = new LoginRequest("9999999999", "anything");
        when(userRepository.findByPhone("9999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login() throws InvalidCredentialsException when password doesn't match")
    void login_throws_whenPasswordIncorrect() {
        LoginRequest request = new LoginRequest("9876543210", "wrong-password");
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getProfile() throws UserNotFoundException when user does not exist")
    void getProfile_throws_whenUserNotFound() {
        UUID randomId = UUID.randomUUID();
        when(userRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getProfile(randomId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
