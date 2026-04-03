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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email уже занят");
        }
        Roles teacherRole = roleRepository.findByName(Role.TEACHER)
                .orElseThrow(() -> new RuntimeException("Роль STUDENT не найдена"));
                Set<Roles> teacherRoles = new HashSet<>();
        teacherRoles.add(teacherRole);
            User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(teacherRoles);
        user.setStatus(UserStatus.PENDING);
        user.setEnabled(false);

        User savedUser = userRepository.save(user);
        TeacherProfile teacherProfile =new TeacherProfile();
        teacherProfile.setUser(savedUser);
        teacherProfile.setPosition(request.getPosition());
        teacherProfileRepository.save(teacherProfile);
    }
    @Transactional
    public void registerStudent(RegisterStudentRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email уже занят");
        }

        Roles studentRole = roleRepository.findByName(Role.STUDENT)
                .orElseThrow(() -> new RuntimeException("Роль STUDENT не найдена"));

        GroupEntity groupEntity = groupEntityRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        Set<Roles> studentRoles = new HashSet<>();
        studentRoles.add(studentRole);

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(studentRoles);
        user.setStatus(UserStatus.PENDING);
        user.setEnabled(false);

        User savedUser = userRepository.save(user);

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setUser(savedUser);
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
}