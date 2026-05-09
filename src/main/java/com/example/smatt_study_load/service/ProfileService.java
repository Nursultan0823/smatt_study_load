package com.example.smatt_study_load.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.smatt_study_load.DTO.UpdateProfileRequest;
import com.example.smatt_study_load.DTO.UpdatedProfileDto;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.models.UserAvatar;
import com.example.smatt_study_load.repository.UserAvatarRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    private final UserRepository userRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final JwtService jwtService;

    @Transactional
    public UpdatedProfileDto updateProfile(UpdateProfileRequest request, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        String fullName = request.getFullName() != null ? request.getFullName().trim() : "";
        String email = request.getEmail() != null ? request.getEmail().trim() : "";

        if (fullName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя не может быть пустым");
        }

        if (email.isBlank() || !email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный email");
        }

        if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email уже занят");
        }

        user.setFullName(fullName);
        user.setEmail(email);

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        return new UpdatedProfileDto(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                token,
                userAvatarRepository.existsById(saved.getId())
        );
    }

    @Transactional
    public void updateAvatar(MultipartFile file, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не выбран");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Можно загрузить только изображение");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Максимальный размер фото - 5 МБ");
        }

        try {
            UserAvatar avatar = userAvatarRepository.findById(user.getId())
                    .orElseGet(UserAvatar::new);
            String originalFileName = file.getOriginalFilename();

            avatar.setUser(user);
            avatar.setFileName(
                    originalFileName == null || originalFileName.isBlank() ? "avatar" : originalFileName
            );
            avatar.setContentType(contentType);
            avatar.setData(file.getBytes());

            userAvatarRepository.save(avatar);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось сохранить фото");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getAvatar(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        UserAvatar avatar = userAvatarRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Фото профиля не найдено"));

        if (avatar.getData() == null || avatar.getData().length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фото профиля не найдено");
        }

        MediaType contentType = avatar.getContentType() != null
                ? MediaType.parseMediaType(avatar.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(avatar.getData());
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }
}
