package com.smart_skin.smart_skin_app_backend.services.imp;

import com.smart_skin.smart_skin_app_backend.dto.*;
import com.smart_skin.smart_skin_app_backend.exception.BadRequestException;
import com.smart_skin.smart_skin_app_backend.exception.ResourceNotFoundException;
import com.smart_skin.smart_skin_app_backend.mappers.UserMapper;
import com.smart_skin.smart_skin_app_backend.models.PasswordResetToken;
import com.smart_skin.smart_skin_app_backend.models.User;
import com.smart_skin.smart_skin_app_backend.repos.PasswordResetTokenRepository;
import com.smart_skin.smart_skin_app_backend.repos.UserRepository;
import com.smart_skin.smart_skin_app_backend.security.JwtUtil;
import com.smart_skin.smart_skin_app_backend.services.CloudinaryService;
import com.smart_skin.smart_skin_app_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailServiceImp emailService;

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Un compte avec cet email existe déjà");
        }

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phoneNumber(dto.getPhoneNumber())
                .onboardingCompleted(false)
                .notificationsEnabled(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    public AuthResponseDto login(LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (user.getDeletionRequestedAt() != null) {
            user.setDeletionRequestedAt(null);
            userRepository.save(user);
            log.info("Account deletion cancelled for user {} upon re-login", user.getId());
        }

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    public AuthResponseDto refreshToken(String refreshToken) {
        String userEmail = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!jwtUtil.isTokenValid(refreshToken, user)) {
            throw new BadRequestException("Token de rafraîchissement invalide ou expiré");
        }

        String newAccessToken = jwtUtil.generateToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    public UserResponseDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateProfile(Long userId, UpdateProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getSkinType() != null) user.setSkinType(dto.getSkinType());
        if (dto.getSkinConcerns() != null) user.setSkinConcerns(dto.getSkinConcerns());
        if (dto.getRoutinePreference() != null) user.setRoutinePreference(dto.getRoutinePreference());
        if (dto.getEffortLevel() != null) user.setEffortLevel(dto.getEffortLevel());
        if (dto.getSunExposure() != null) user.setSunExposure(dto.getSunExposure());
        if (dto.getAge() != null) user.setAge(dto.getAge());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getSkinSensitivity() != null) user.setSkinSensitivity(dto.getSkinSensitivity());
        if (dto.getTirednessLevel() != null) user.setTirednessLevel(dto.getTirednessLevel());
        if (dto.getStressLevel() != null) user.setStressLevel(dto.getStressLevel());
        if (dto.getIngredientsToAvoid() != null) user.setIngredientsToAvoid(dto.getIngredientsToAvoid());
        if (dto.getDesiredBenefits() != null) user.setDesiredBenefits(dto.getDesiredBenefits());
        if (dto.getOnboardingName() != null) user.setOnboardingName(dto.getOnboardingName());
        user.setNotificationsEnabled(dto.isNotificationsEnabled());

        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto completeOnboarding(Long userId, OnboardingDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            String[] parts = dto.getName().trim().split(" ", 2);
            user.setFirstName(parts[0]);
            if (parts.length > 1) user.setLastName(parts[1]);
        }
        user.setSkinType(dto.getSkinType());
        user.setSkinConcerns(dto.getSkinConcerns());
        user.setEthnicity(dto.getEthnicity());
        user.setRoutinePreference(dto.getRoutinePreference());
        user.setEffortLevel(dto.getEffortLevel());
        user.setSunExposure(dto.getSunExposure());
        user.setIngredientsToAvoid(dto.getIngredientsToAvoid());
        user.setAge(dto.getAge());
        user.setGender(dto.getGender());
        user.setSkinSensitivity(dto.getSkinSensitivity());
        user.setTirednessLevel(dto.getTirednessLevel());
        user.setStressLevel(dto.getStressLevel());
        user.setDesiredBenefits(dto.getDesiredBenefits());
        user.setOnboardingName(dto.getOnboardingName());
        user.setOnboardingCompleted(true);

        user = userRepository.save(user);
        log.info("Onboarding completed for user: {}", user.getEmail());
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto uploadProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Delete old image if exists
        if (user.getProfileImageUrl() != null) {
            cloudinaryService.deleteImage(user.getProfileImageUrl());
        }

        String imageUrl = cloudinaryService.uploadImage(file, "profile_images/" + userId);
        user.setProfileImageUrl(imageUrl);
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }



    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun compte associé à cet email"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUserId(user.getId());
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        prt.setUsed(false);

        tokenRepository.save(prt);

        emailService.sendEmail(
                user.getEmail(),
                "Reset Password",
                "Votre code de réinitialisation : " + token
        );

        log.info("Reset email sent to {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Code incorrect"));

        if (prt.isUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expiré");
        }

        User user = userRepository.findById(prt.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);
    }

    @Override
    @Transactional
    public void deleteCurrentUser() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }
}
