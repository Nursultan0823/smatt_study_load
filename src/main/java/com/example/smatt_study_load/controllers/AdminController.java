package com.example.smatt_study_load.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.smatt_study_load.DTO.AddDisciplineRequest;
import com.example.smatt_study_load.DTO.GroupDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.UserDto;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.service.AdminService;

import java.util.List;


@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final AdminService adminService;

    public AdminController(UserRepository userRepository,AdminService adminService) {
        this.userRepository = userRepository;
        this.adminService=adminService;
    }

    @GetMapping("/pending")
    public List<UserDto> getPendingUsers() {
        return adminService.getPendingUser();
    }
@PutMapping("/approve/{id}")
public ResponseEntity<?> approveUser(@PathVariable int id) {
    User user = userRepository.findById(id).orElse(null);

    if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Response("Пользователь не найден"));
    }

    try {
        user.setStatus(UserStatus.APPROVED);
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(new Response("Пользователь подтвержден"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("Ошибка при подтверждении пользователя"));
    }
}
    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectUser(@PathVariable int id) {
         User user = userRepository.findById(id).orElse(null);

    if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Response("Пользователь не найден"));
    }

    try {
        user.setStatus(UserStatus.REJECTED);
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok("");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("Пользователь не найден"));
    }
    
    }
      @PostMapping("/addgroup")
      public ResponseEntity<?> AddGroup(@RequestBody GroupDTO groupDTO){
        return adminService.AddGroupEntity(groupDTO);
      }
      @PostMapping("/adddiscipline")
      public ResponseEntity<?> postMethodName(@RequestBody AddDisciplineRequest request) {
          return adminService.AddDiscipline(request);
      }
       @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
       return adminService.getCurrentUser(authentication);
    }
      
}