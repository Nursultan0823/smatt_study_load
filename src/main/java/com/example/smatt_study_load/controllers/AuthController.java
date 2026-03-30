package com.example.smatt_study_load.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.smatt_study_load.DTO.GroupShortDto;
import com.example.smatt_study_load.DTO.LoginRequest;
import com.example.smatt_study_load.DTO.RegisterStudentRequest;
import com.example.smatt_study_load.DTO.RegisterTeacherRequest;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.repository.GroupEntityRepository;
import com.example.smatt_study_load.service.AuthService;



@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final GroupEntityRepository groupEntityRepository;
    

    public AuthController(AuthService authService,GroupEntityRepository groupEntityRepository ) {
        this.authService = authService;
        this.groupEntityRepository= groupEntityRepository;
        
    }
    
    @PostMapping("/register/student")
    public ResponseEntity<?> studentRegister(@RequestBody RegisterStudentRequest request) {
        try {
            authService.registerStudent(request);
            return ResponseEntity.ok(new Response("Заявка отправлена. Ожидайте подтверждения администратора."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Ошибка при регистрации"));
        }
    }
 @PostMapping("/register/teacher")
    public ResponseEntity<?> teacherRegister(@RequestBody RegisterTeacherRequest request) {
        try {
            authService.registerTeacher(request);
            return ResponseEntity.ok(new Response("Заявка отправлена. Ожидайте подтверждения администратора."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response("Ошибка при регистрации"));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
    @GetMapping("/getgroup")
    public List<GroupShortDto> getGroupsShort() {
    return groupEntityRepository.findAllGroupShort();}
    
}