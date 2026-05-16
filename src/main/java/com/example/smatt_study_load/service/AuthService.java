package com.example.smatt_study_load.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smatt_study_load.DTO.AuthResponse;
import com.example.smatt_study_load.DTO.LoginRequest;
import com.example.smatt_study_load.DTO.RegisterStudentRequest;
import com.example.smatt_study_load.DTO.RegisterTeacherRequest;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.enums.Role;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.GroupEntity;
import com.example.smatt_study_load.models.Roles;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.GroupEntityRepository;
import com.example.smatt_study_load.repository.RoleRepository;
import com.example.smatt_study_load.repository.StudentProfileRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final GroupEntityRepository groupEntityRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       RoleRepository roleRepository,
                       GroupEntityRepository groupEntityRepository,
                        StudentProfileRepository studentProfileRepository,
                        TeacherProfileRepository teacherProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.roleRepository=roleRepository;
        this.groupEntityRepository=groupEntityRepository;
        this.studentProfileRepository=studentProfileRepository;
        this.teacherProfileRepository=teacherProfileRepository;
    }
    @Transactional
    public void registerTeacher(RegisterTeacherRequest request){
        String email = requireText(request.getEmail(), "Email обязателен");
        String fullName = requireText(request.getFullName(), "Имя обязательно");
        String password = requireText(request.getPassword(), "Пароль обязателен");
        String position = requireText(request.getPosition(), "Должность обязательна");

        Roles teacherRole = roleRepository.findByName(Role.TEACHER)
                .orElseThrow(() -> new RuntimeException("Роль TEACHER не найдена"));
        Set<Roles> teacherRoles = new HashSet<>();
        teacherRoles.add(teacherRole);

        User user = prepareUserForRegistration(email, fullName, password, teacherRoles);

        studentProfileRepository.findByUser(user).ifPresent(this::removeStudentProfile);

        TeacherProfile teacherProfile = teacherProfileRepository.findByUser(user)
                .orElseGet(TeacherProfile::new);
        teacherProfile.setUser(user);
        teacherProfile.setPosition(position);
        teacherProfileRepository.save(teacherProfile);
    }
    @Transactional
    public void registerStudent(RegisterStudentRequest request) {
        String email = requireText(request.getEmail(), "Email обязателен");
        String fullName = requireText(request.getFullName(), "Имя обязательно");
        String password = requireText(request.getPassword(), "Пароль обязателен");

        Roles studentRole = roleRepository.findByName(Role.STUDENT)
                .orElseThrow(() -> new RuntimeException("Роль STUDENT не найдена"));

        GroupEntity groupEntity = groupEntityRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        Set<Roles> studentRoles = new HashSet<>();
        studentRoles.add(studentRole);

        User user = prepareUserForRegistration(email, fullName, password, studentRoles);

        teacherProfileRepository.findByUser(user).ifPresent(this::removeTeacherProfile);

        StudentProfile studentProfile = studentProfileRepository.findByUser(user)
                .orElseGet(StudentProfile::new);
        studentProfile.setUser(user);
        studentProfile.setGroup(groupEntity);

        studentProfileRepository.save(studentProfile);
    }

    public ResponseEntity<?> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response("Пользователь не найден"));
            }
        if (user.getStatus() != UserStatus.APPROVED || !user.isEnabled()) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response("Аккаунт еще не подтвержден администратором"));
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

    private User prepareUserForRegistration(String email, String fullName, String password, Set<Roles> roles) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null
                && user.getStatus() != UserStatus.REJECTED
                && user.getStatus() != UserStatus.DELETED) {
            throw new RuntimeException("Email уже занят");
        }

        if (user == null) {
            user = new User();
            user.setEmail(email);
        }

        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        user.setStatus(UserStatus.PENDING);
        user.setEnabled(false);

        return userRepository.save(user);
    }

    private void removeStudentProfile(StudentProfile studentProfile) {
        GroupEntity group = studentProfile.getGroup();
        User studentUser = studentProfile.getUser();

        if (group != null && group.getStarosta() != null
                && group.getStarosta().getId() == studentProfile.getId()) {
            group.setStarosta(null);
            groupEntityRepository.save(group);
        }

        if (studentUser != null) {
            studentUser.setStudentProfile(null);
        }

        studentProfileRepository.delete(studentProfile);
    }

    private void removeTeacherProfile(TeacherProfile teacherProfile) {
        User teacherUser = teacherProfile.getUser();

        if (teacherUser != null) {
            teacherUser.setTeacherProfile(null);
        }

        teacherProfileRepository.delete(teacherProfile);
    }

    private String requireText(String value, String message) {
        String text = value != null ? value.trim() : "";

        if (text.isBlank()) {
            throw new RuntimeException(message);
        }

        return text;
    }
}
