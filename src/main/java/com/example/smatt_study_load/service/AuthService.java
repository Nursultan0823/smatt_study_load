package com.example.smatt_study_load.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.smatt_study_load.DTO.AuthResponse;
import com.example.smatt_study_load.DTO.LoginRequest;
import com.example.smatt_study_load.DTO.RegisterRequest;
import com.example.smatt_study_load.enums.Role;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public ResponseEntity<?> registerStudent(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email уже занят");
        }

        try{
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setStatus(UserStatus.PENDING);
        user.setEnabled(false);

        userRepository.save(user);

        return ResponseEntity.ok("Заявка отправлена. Ожидайте подтверждения администратора.");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при регистрации");
        }
    }

    public ResponseEntity<?> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Пользователь не найден");
            }
        if (user.getStatus() != UserStatus.APPROVED || !user.isEnabled()) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Аккаунт еще не подтвержден администратором");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(user);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body( new AuthResponse(token));
    }
}